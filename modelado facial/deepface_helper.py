#!/usr/bin/env python3
"""
Helper script to extract facial embedding using DeepFace.
Designed to be called as a subprocess to isolate DeepFace crashes.
"""
import os
import sys
import json
import base64
import io

# Disable GPU
os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0'
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'
os.environ['CUDA_VISIBLE_DEVICES'] = ''

try:
    import numpy as np
    from PIL import Image
    from deepface import DeepFace
except Exception as e:
    print(json.dumps({"success": False, "error": f"Import failed: {e}"}))
    sys.exit(1)

def extract_embedding(image_base64: str) -> dict:
    """Extract facial embedding from a base64 encoded image."""
    try:
        # Decode base64
        if "," in image_base64:
            image_base64 = image_base64.split(",", 1)[1]
        
        image_bytes = base64.b64decode(image_base64)
        pil_image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
        image_array = np.array(pil_image)
        
        # Ensure BGR format
        if image_array.ndim == 2:
            import cv2
            image_array = cv2.cvtColor(image_array, cv2.COLOR_GRAY2BGR)
        elif image_array.shape[2] == 4:
            import cv2
            image_array = cv2.cvtColor(image_array, cv2.COLOR_RGBA2BGR)
        elif image_array.shape[2] == 3:
            import cv2
            image_array = cv2.cvtColor(image_array, cv2.COLOR_RGB2BGR)
        
        # Extract embedding
        result = DeepFace.represent(
            img_path=image_array,
            model_name="ArcFace",
            detector_backend="opencv",
            enforce_detection=True,
            align=True,
        )
        
        if not result:
            return {"success": False, "error": "No face detected"}
        
        if len(result) > 1:
            result = [max(result, key=lambda r: r.get("facial_area", {}).get("w", 0))]
        
        embedding = result[0]["embedding"]
        return {"success": True, "embedding": embedding}
        
    except ValueError as e:
        msg = str(e).lower()
        if "face" in msg or "detected" in msg:
            return {"success": False, "error": "No face detected in image"}
        return {"success": False, "error": f"Detection error: {str(e)}"}
    except Exception as e:
        return {"success": False, "error": f"Error: {str(e)}"}

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({"success": False, "error": "No image provided"}))
        sys.exit(1)
    
    image_base64 = sys.argv[1]
    result = extract_embedding(image_base64)
    print(json.dumps(result))
