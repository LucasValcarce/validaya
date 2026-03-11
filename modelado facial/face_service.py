import numpy as np
import pickle
import threading
from datetime import datetime
from pathlib import Path
from typing import Optional, Tuple, List, Dict

import cv2
from deepface import DeepFace

MODEL_NAME = "ArcFace"    # Alternativas: "Facenet512", "VGG-Face"
DETECTOR   = "opencv"     # Alternativas: "ssd", "retinaface" (más preciso, más lento)

class FaceRecognitionService:
    
    def __init__(self, threshold: float = 0.40, storage_path: str = "face_db"):
        self.threshold    = threshold
        self.storage_path = Path(storage_path)
        self.storage_path.mkdir(parents=True, exist_ok=True)
        self._lock  = threading.Lock()
        self._cache: Dict = {}
        self._load_all_from_disk()
        self._warmup()

    def register(self, person_id: str, image: np.ndarray) -> dict:
        embedding, error = self._extract_embedding(image)
        if error:
            return {"success": False, "error": error}

        with self._lock:
            existing = self._cache.get(person_id, [])
            existing.append(embedding)
            self._cache[person_id] = existing
            self._save_to_disk(person_id, existing)

        return {
            "success": True,
            "person_id": person_id,
            "total_encodings": len(existing),
            "message": f"Rostro registrado correctamente para {person_id}"
        }

    def verify(self, person_id: str, image: np.ndarray) -> dict:
        with self._lock:
            known_embeddings = self._cache.get(person_id)

        if not known_embeddings:
            return {
                "success": False,
                "match": False,
                "confidence": 0.0,
                "person_id": person_id,
                "error": f"No existe registro facial para person_id={person_id}"
            }

        test_embedding, error = self._extract_embedding(image)
        if error:
            return {
                "success": True,
                "match": False,
                "confidence": 0.0,
                "person_id": person_id,
                "message": error
            }

        distances = [
            self._cosine_distance(np.array(known), np.array(test_embedding))
            for known in known_embeddings
        ]
        min_distance = float(min(distances))

        confidence = round(max(0.0, 1.0 - min_distance), 4)

        match = bool(min_distance <= self.threshold)

        return {
            "success":    True,
            "match":      match,            # ← BOOLEAN que lee Java
            "confidence": confidence,
            "distance":   round(min_distance, 4),
            "threshold":  self.threshold,
            "person_id":  person_id,
            "timestamp":  datetime.utcnow().isoformat() + "Z",
            "message":    "Identidad verificada" if match else "Identidad no coincide"
        }

    def delete(self, person_id: str) -> dict:
        with self._lock:
            if person_id not in self._cache:
                return {"success": False, "error": f"person_id={person_id} no encontrado"}
            del self._cache[person_id]
            fp = self.storage_path / f"{person_id}.pkl"
            if fp.exists():
                fp.unlink()
        return {"success": True, "person_id": person_id, "message": "Registro eliminado"}

    def list_registered(self) -> List[str]:
        with self._lock:
            return list(self._cache.keys())

  
    def _extract_embedding(self, image: np.ndarray) -> Tuple[Optional[list], Optional[str]]:
      
        bgr_image = self._ensure_bgr(image)
        try:
            result = DeepFace.represent(
                img_path         = bgr_image,
                model_name       = MODEL_NAME,
                detector_backend = DETECTOR,
                enforce_detection = True,
                align            = True,
            )
        except ValueError as e:
            msg = str(e).lower()
            if "face" in msg or "detected" in msg:
                return None, "No se detectó ningún rostro en la imagen"
            return None, f"Error de detección: {str(e)}"
        except Exception as e:
            return None, f"Error al procesar la imagen: {str(e)}"

        if not result:
            return None, "No se detectó ningún rostro en la imagen"

        # Si hay múltiples rostros, tomar el de mayor área
        if len(result) > 1:
            result = [max(result, key=lambda r: r.get("facial_area", {}).get("w", 0))]

        return result[0]["embedding"], None

    def _save_to_disk(self, person_id: str, embeddings: list):
        fp = self.storage_path / f"{person_id}.pkl"
        with open(fp, "wb") as f:
            pickle.dump(embeddings, f)

    def _load_all_from_disk(self):
        for pkl_file in self.storage_path.glob("*.pkl"):
            try:
                with open(pkl_file, "rb") as f:
                    self._cache[pkl_file.stem] = pickle.load(f)
            except Exception:
                pass

   
    def _warmup(self):
        """Pre-carga el modelo al arrancar para evitar latencia en la primera llamada."""
        try:
            dummy = np.zeros((112, 112, 3), dtype=np.uint8)
            DeepFace.represent(
                img_path=dummy, model_name=MODEL_NAME,
                detector_backend=DETECTOR, enforce_detection=False,
            )
        except Exception:
            pass

    @staticmethod
    def _ensure_bgr(image: np.ndarray) -> np.ndarray:
        """Convierte imagen a BGR para OpenCV/DeepFace."""
        if image.ndim == 2:
            return cv2.cvtColor(image, cv2.COLOR_GRAY2BGR)
        if image.shape[2] == 4:
            return cv2.cvtColor(image, cv2.COLOR_RGBA2BGR)
        return cv2.cvtColor(image, cv2.COLOR_RGB2BGR)

    @staticmethod
    def _cosine_distance(a: np.ndarray, b: np.ndarray) -> float:
        norm_a, norm_b = np.linalg.norm(a), np.linalg.norm(b)
        if norm_a == 0 or norm_b == 0:
            return 1.0
        return float(1.0 - np.dot(a, b) / (norm_a * norm_b))