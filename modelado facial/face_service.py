import json
import logging
import threading
from datetime import datetime
from typing import Optional, Tuple, List, Dict

import cv2
import numpy as np
from deepface import DeepFace

from db import get_connection, release_connection

logger = logging.getLogger(__name__)

MODEL_NAME = "ArcFace"
DETECTOR   = "opencv"


class FaceRecognitionService:
    
    def __init__(self, threshold: float = 0.40):
        self.threshold = threshold
        self._lock     = threading.Lock()
        self._cache: Dict[str, List[list]] = {}
        self._load_cache_from_db()
        self._warmup()

    # ──────────────────────────────────────────
    #  Registro
    # ──────────────────────────────────────────
    def register(self, person_id: str, image: np.ndarray) -> dict:
        
        existing_hashes = self._load_hashes_from_db(person_id)
        if existing_hashes is None:
            return {
                "success": False,
                "error": f"No existe usuario con identification='{person_id}' en la base de datos"
            }

        embedding, error = self._extract_embedding(image)
        if error:
            return {"success": False, "error": error}

        existing_hashes.append(embedding)

        save_error = self._save_hashes_to_db(person_id, existing_hashes)
        if save_error:
            return {"success": False, "error": save_error}

        with self._lock:
            self._cache[person_id] = existing_hashes

        logger.info(f"Embedding registrado: identification={person_id} | total={len(existing_hashes)}")
        return {
            "success": True,
            "person_id": person_id,
            "total_encodings": len(existing_hashes),
            "message": f"Rostro registrado correctamente para {person_id}"
        }

   
    def verify(self, person_id: str, image: np.ndarray) -> dict:
       
        with self._lock:
            known_embeddings = self._cache.get(person_id)

        if known_embeddings is None:
            known_embeddings = self._load_hashes_from_db(person_id)
            if known_embeddings is None:
                return {
                    "success": False,
                    "match": False,
                    "confidence": 0.0,
                    "person_id": person_id,
                    "error": f"No existe usuario con identification='{person_id}'"
                }
            with self._lock:
                self._cache[person_id] = known_embeddings

        if len(known_embeddings) == 0:
            return {
                "success": False,
                "match": False,
                "confidence": 0.0,
                "person_id": person_id,
                "error": f"El usuario '{person_id}' no tiene rostro registrado (face_hash vacio)"
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
        confidence   = round(max(0.0, 1.0 - min_distance), 4)
        match        = bool(min_distance <= self.threshold)

        logger.info(
            f"Verificacion: identification={person_id} | "
            f"match={match} | confidence={confidence:.2f} | distance={min_distance:.4f}"
        )

        return {
            "success":    True,
            "match":      match,            # <- BOOLEAN que lee Java
            "confidence": confidence,
            "distance":   round(min_distance, 4),
            "threshold":  self.threshold,
            "person_id":  person_id,
            "timestamp":  datetime.utcnow().isoformat() + "Z",
            "message":    "Identidad verificada" if match else "Identidad no coincide"
        }

    def delete(self, person_id: str) -> dict:
        """Limpia el face_hash del usuario (no borra el registro del usuario)."""
        conn = None
        try:
            conn = get_connection()
            cur  = conn.cursor()
            cur.execute(
                "UPDATE users SET face_hash = NULL WHERE identification = %s",
                (person_id,)
            )
            if cur.rowcount == 0:
                conn.rollback()
                return {"success": False, "error": f"identification='{person_id}' no encontrado"}

            conn.commit()
            cur.close()
            with self._lock:
                self._cache.pop(person_id, None)
            return {"success": True, "person_id": person_id, "message": "face_hash eliminado correctamente"}

        except Exception as e:
            if conn:
                conn.rollback()
            logger.error(f"Error al eliminar face_hash: {e}")
            return {"success": False, "error": "Error de base de datos al eliminar"}
        finally:
            if conn:
                release_connection(conn)

    def list_registered(self) -> List[str]:
        """Lista todos los identification que tienen face_hash registrado."""
        conn = None
        try:
            conn = get_connection()
            cur  = conn.cursor()
            cur.execute(
                "SELECT identification FROM users "
                "WHERE face_hash IS NOT NULL AND face_hash != '[]'"
            )
            rows = cur.fetchall()
            cur.close()
            return [row[0] for row in rows]
        except Exception as e:
            logger.error(f"Error al listar registros: {e}")
            return []
        finally:
            if conn:
                release_connection(conn)

  
    def _load_hashes_from_db(self, person_id: str) -> Optional[List[list]]:
        conn = None
        try:
            conn = get_connection()
            cur  = conn.cursor()
            cur.execute(
                "SELECT face_hash FROM users WHERE identification = %s",
                (person_id,)
            )
            row = cur.fetchone()
            cur.close()

            if row is None:
                return None       

            face_hash = row[0]
            if not face_hash:
                return []         # Usuario existe pero sin face_hash aun

            data = json.loads(face_hash)

            # Soporte retrocompatible: embedding unico vs lista de embeddings
            if data and isinstance(data[0], list):
                return data       # [[emb1], [emb2], ...]
            return [data]         # [emb] -> [[emb]]

        except Exception as e:
            logger.error(f"Error al leer face_hash de BD: {e}")
            return None
        finally:
            if conn:
                release_connection(conn)

    def _save_hashes_to_db(self, person_id: str, embeddings: List[list]) -> Optional[str]:
        """
        Guarda la lista de embeddings en face_hash.
        Retorna None si fue exitoso, o un mensaje de error.
        """
        conn = None
        try:
            conn = get_connection()
            cur  = conn.cursor()
            cur.execute(
                "UPDATE users SET face_hash = %s WHERE identification = %s",
                (json.dumps(embeddings), person_id)
            )
            if cur.rowcount == 0:
                conn.rollback()
                return f"No se encontro usuario con identification='{person_id}'"

            conn.commit()
            cur.close()
            return None

        except Exception as e:
            if conn:
                conn.rollback()
            logger.error(f"Error al guardar face_hash en BD: {e}")
            return "Error de base de datos al guardar embedding"
        finally:
            if conn:
                release_connection(conn)

    def _load_cache_from_db(self):
        """Precarga todos los embeddings en memoria al arrancar."""
        conn = None
        try:
            conn = get_connection()
            cur  = conn.cursor()
            cur.execute(
                "SELECT identification, face_hash FROM users "
                "WHERE face_hash IS NOT NULL AND face_hash != '[]'"
            )
            rows = cur.fetchall()
            cur.close()

            for identification, face_hash in rows:
                try:
                    data = json.loads(face_hash)
                    if data and isinstance(data[0], list):
                        self._cache[identification] = data
                    else:
                        self._cache[identification] = [data]
                except Exception:
                    pass

            logger.info(f"Cache cargada: {len(self._cache)} usuarios con face_hash")

        except Exception as e:
            logger.warning(f"No se pudo precargar cache desde BD: {e}")
        finally:
            if conn:
                release_connection(conn)

    def _extract_embedding(self, image: np.ndarray) -> Tuple[Optional[list], Optional[str]]:
        bgr_image = self._ensure_bgr(image)
        try:
            result = DeepFace.represent(
                img_path          = bgr_image,
                model_name        = MODEL_NAME,
                detector_backend  = DETECTOR,
                enforce_detection = True,
                align             = True,
            )
        except ValueError as e:
            msg = str(e).lower()
            if "face" in msg or "detected" in msg:
                return None, "No se detecto ningun rostro en la imagen"
            return None, f"Error de deteccion: {str(e)}"
        except Exception as e:
            return None, f"Error al procesar la imagen: {str(e)}"

        if not result:
            return None, "No se detecto ningun rostro en la imagen"

        if len(result) > 1:
            result = [max(result, key=lambda r: r.get("facial_area", {}).get("w", 0))]

        return result[0]["embedding"], None

   
    def _warmup(self):
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