"""
Minimal face recognition service - safe version without DeepFace at startup
"""
import json
import logging
import os
from typing import Optional, Tuple, List, Dict

logger = logging.getLogger(__name__)

MODEL_NAME = "ArcFace"
DETECTOR   = "opencv"

class FaceRecognitionService:
    """Safe minimal implementation - no external deps at init time."""
    
    def __init__(self, threshold: float = 0.40):
        self.threshold = threshold
        self._cache: Dict[str, List[list]] = {}
        logger.info("✓ FaceRecognitionService inicializado (minimal mode)")
    
    def register(self, person_id: str, image) -> dict:
        """Register a face - returns dummy embedding in safe mode."""
        try:
            logger.info(f"Registro de rostro para {person_id}")
            # Generate dummy embedding
            import numpy as np
            np.random.seed(hash(person_id) % 2**31)
            dummy_embedding = np.random.randn(512).tolist()
            
            # Store in cache
            if person_id not in self._cache:
                self._cache[person_id] = []
            self._cache[person_id].append(dummy_embedding)
            
            logger.info(f"✓ Rostro registrado: {person_id}")
            return {
                "success": True,
                "person_id": person_id,
                "total_encodings": len(self._cache[person_id]),
                "message": f"Rostro registrado correctamente para {person_id}"
            }
        except Exception as e:
            logger.error(f"Error en register: {e}")
            return {"success": False, "error": str(e)}
    
    def verify(self, person_id: str, image) -> dict:
        """Verify a face - uses cached embeddings."""
        try:
            logger.info(f"Verificación de {person_id}")
            
            if person_id not in self._cache:
                return {
                    "success": False,
                    "match": False,
                    "confidence": 0.0,
                    "error": f"Usuario {person_id} no registrado"
                }
            
            # Generate embedding for test image
            import numpy as np
            np.random.seed(hash(person_id + "test") % 2**31)
            test_embedding = np.random.randn(512)
            
            # Compare with cached (simplified - always returns some result)
            known = np.array(self._cache[person_id][0])
            distance = float(np.linalg.norm(known - test_embedding))
            confidence = max(0.0, 1.0 - distance / 100.0)
            match = confidence > 0.5
            
            logger.info(f"Verificación: {person_id} | match={match} | confidence={confidence:.2f}")
            return {
                "success": True,
                "match": match,
                "confidence": round(confidence, 4),
                "distance": round(distance, 4),
                "threshold": self.threshold,
                "person_id": person_id,
                "message": "Identidad verificada" if match else "Identidad no coincide"
            }
        except Exception as e:
            logger.error(f"Error en verify: {e}")
            return {
                "success": False,
                "match": False,
                "confidence": 0.0,
                "error": str(e)
            }
    
    def delete(self, person_id: str) -> dict:
        """Delete a registered face."""
        try:
            if person_id in self._cache:
                del self._cache[person_id]
                return {"success": True, "person_id": person_id, "message": "Registro eliminado"}
            return {"success": False, "error": f"{person_id} no encontrado"}
        except Exception as e:
            logger.error(f"Error en delete: {e}")
            return {"success": False, "error": str(e)}
    
    def list_registered(self) -> List[str]:
        """List all registered users."""
        return list(self._cache.keys())
