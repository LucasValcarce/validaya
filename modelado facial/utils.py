import base64
import io
from typing import Optional, Tuple
import numpy as np
from PIL import Image

def decode_image_from_request(request) -> Tuple[Optional[np.ndarray], Optional[str]]:
    if "image" in request.files:
        file = request.files["image"]
        if file.filename == "":
            return None, "Archivo de imagen vacío"
        return _file_to_array(file)

    body = request.get_json(silent=True) or {}
    b64_string = body.get("image_base64") or request.form.get("image_base64")

    if b64_string:
        return _base64_to_array(b64_string)

    return None, "Se requiere 'image' (multipart) o 'image_base64' (JSON)"


def _file_to_array(file) -> Tuple[Optional[np.ndarray], Optional[str]]:
    """Convierte un FileStorage de Flask en numpy array RGB."""
    try:
        pil_image = Image.open(file.stream).convert("RGB")
        return np.array(pil_image), None
    except Exception as e:
        return None, f"No se pudo leer el archivo de imagen: {str(e)}"


def _base64_to_array(b64_string: str) -> Tuple[Optional[np.ndarray], Optional[str]]:
    """Decodifica una imagen en base64 y la convierte en numpy array RGB."""
    try:
        # Remover prefijo data URI si existe (data:image/jpeg;base64,...)
        if "," in b64_string:
            b64_string = b64_string.split(",", 1)[1]

        image_bytes = base64.b64decode(b64_string)
        pil_image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
        return np.array(pil_image), None
    except Exception as e:
        return None, f"No se pudo decodificar la imagen base64: {str(e)}"


def validate_request(required_fields: list, data: dict) -> Optional[str]:
    """
    Valida que todos los campos requeridos estén presentes.
    Retorna mensaje de error o None si todo está bien.
    """
    missing = [f for f in required_fields if not data.get(f)]
    if missing:
        return f"Campos requeridos faltantes: {', '.join(missing)}"
    return None