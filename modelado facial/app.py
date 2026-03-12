from flask import Flask, request, jsonify
from flask_cors import CORS
import logging
import os
from face_service import FaceRecognitionService
from utils import decode_image_from_request, validate_request

app = Flask(__name__)
CORS(app)

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s"
)
logger = logging.getLogger(__name__)

face_service = FaceRecognitionService(threshold=0.40)   # UMBRAL

@app.route("/health", methods=["GET"])
def health_check():
    return jsonify({"status": "ok", "service": "facial-recognition-api"}), 200

@app.route("/api/register", methods=["POST"])
def register_face():
    try:
        person_id = request.form.get("person_id") or (request.json or {}).get("person_id")
        if not person_id:
            return jsonify({"success": False, "error": "person_id es requerido"}), 400

        image_array, error = decode_image_from_request(request)
        if error:
            return jsonify({"success": False, "error": error}), 400

        result = face_service.register(person_id, image_array)

        if result["success"]:
            logger.info(f"Rostro registrado: person_id={person_id}")
            return jsonify(result), 201
        else:
            logger.warning(f"Fallo registro: person_id={person_id} — {result['error']}")
            return jsonify(result), 422

    except Exception as e:
        logger.error(f"Error en /register: {e}", exc_info=True)
        return jsonify({"success": False, "error": "Error interno del servidor"}), 500


@app.route("/api/verify", methods=["POST"])
def verify_face():
    try:
        person_id = request.form.get("person_id") or (request.json or {}).get("person_id")
        if not person_id:
            return jsonify({"success": False, "match": False, "error": "person_id es requerido"}), 400

        image_array, error = decode_image_from_request(request)
        if error:
            return jsonify({"success": False, "match": False, "error": error}), 400

        result = face_service.verify(person_id, image_array)

        logger.info(
            f"Verificación: person_id={person_id} | "
            f"match={result.get('match')} | "
            f"confidence={result.get('confidence', 0):.2f}"
        )
        return jsonify(result), 200

    except Exception as e:
        logger.error(f"Error en /verify: {e}", exc_info=True)
        return jsonify({"success": False, "match": False, "error": "Error interno del servidor"}), 500


@app.route("/api/delete/<person_id>", methods=["DELETE"])
def delete_face(person_id):
    try:
        result = face_service.delete(person_id)
        if result["success"]:
            logger.info(f"Registro eliminado: person_id={person_id}")
            return jsonify(result), 200
        return jsonify(result), 404
    except Exception as e:
        logger.error(f"Error en /delete: {e}", exc_info=True)
        return jsonify({"success": False, "error": "Error interno del servidor"}), 500


@app.route("/api/registered", methods=["GET"])
def list_registered():
    ids = face_service.list_registered()
    return jsonify({"success": True, "registered_ids": ids, "total": len(ids)}), 200


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    debug = os.environ.get("FLASK_ENV", "production") == "development"
    logger.info(f"Iniciando API en puerto {port} | debug={debug}")
    app.run(host="0.0.0.0", port=port, debug=debug)