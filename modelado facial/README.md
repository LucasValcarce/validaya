# 🔐 API de Reconocimiento Facial
### Microservicio Python — Integración con Backend Java

> Sistema de verificación de identidad biométrica para instituciones gubernamentales.  
> Desarrollado en Python (Flask + DeepFace/ArcFace) para integrarse con backend Java mediante API REST.

---

## 📋 Tabla de Contenidos

1. [¿Qué hace este servicio?](#-qué-hace-este-servicio)
2. [Arquitectura general](#-arquitectura-general)
3. [Requisitos e instalación](#-requisitos-e-instalación)
4. [Estructura del proyecto](#-estructura-del-proyecto)
5. [Endpoints disponibles](#-endpoints-disponibles)
6. [Guía de integración Java](#-guía-de-integración-java)
7. [Flujo completo de un trámite](#-flujo-completo-de-un-trámite)
8. [Códigos de respuesta HTTP](#-códigos-de-respuesta-http)
9. [Ajuste del umbral de seguridad](#-ajuste-del-umbral-de-seguridad)
10. [Manejo de errores](#-manejo-de-errores)
11. [Recomendaciones de seguridad](#-recomendaciones-de-seguridad)

---

## 🤔 ¿Qué hace este servicio?

Este microservicio expone una API REST que permite a tu backend Java:

1. **Registrar** el rostro de referencia de un ciudadano (foto del documento / cédula).
2. **Verificar** si la persona frente a la cámara es quien dice ser.
3. Recibir como respuesta un **`boolean`** claro: `match: true` o `match: false`.

El servicio corre de forma independiente en Python y Java lo consume como cualquier otra API externa.

---

## 🏗️ Arquitectura General

```
┌─────────────────────────────────────────────────────────┐
│                    FRONTEND / WEB                        │
│           (Captura de foto del ciudadano)                │
└─────────────────────┬───────────────────────────────────┘
                      │  MultipartFile / Base64
                      ▼
┌─────────────────────────────────────────────────────────┐
│                  BACKEND JAVA (Spring)                   │
│                                                          │
│   TramiteController  →  FaceRecognitionClient           │
│                                ↓                         │
│              POST /api/verify  (HTTP JSON)               │
└─────────────────────┬───────────────────────────────────┘
                      │
              Red interna / localhost
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│             MICROSERVICIO PYTHON (Flask)                  │
│                   puerto 5000                            │
│                                                          │
│   app.py  →  face_service.py  →  DeepFace (ArcFace)    │
│                      ↓                                   │
│              face_db/  (embeddings en disco)             │
└─────────────────────────────────────────────────────────┘
```

**Flujo resumido:**
```
Java envía imagen en Base64  →  Python verifica identidad  →  Java recibe { "match": true/false }
```

---

## ⚙️ Requisitos e Instalación

### Requisitos del sistema
- Python 3.9 o superior
- pip actualizado

### Instalación de dependencias

```bash
# Clonar o copiar el proyecto
cd facial_recognition_api

# (Recomendado) Crear entorno virtual
python -m venv venv
venv\Scripts\activate        # Windows
source venv/bin/activate     # Linux/Mac

# Instalar dependencias
pip install -r requirements.txt
```

### Arrancar el servicio

```bash
# Desarrollo
python app.py

# Producción (Linux con Gunicorn)
gunicorn -w 4 -b 0.0.0.0:5000 app:app
```

> ⚠️ La **primera vez** que arranque, DeepFace descargará automáticamente los pesos del modelo ArcFace (~100 MB). Requiere conexión a internet solo en ese momento.

Cuando veas esto en consola, el servicio está listo:

```
Running on http://0.0.0.0:5000
```

---

## 📁 Estructura del Proyecto

```
facial_recognition_api/
│
├── app.py              ← Servidor Flask: define y expone los endpoints REST
├── face_service.py     ← Motor de reconocimiento facial (DeepFace/ArcFace)
├── utils.py            ← Decodificación de imágenes (Base64 / multipart)
├── requirements.txt    ← Dependencias Python
├── .gitignore          ← Excluye face_db/ del repositorio
├── README.md           ← Este archivo
│
└── face_db/            ← 🔒 Embeddings faciales persistidos en disco
    ├── CIU-00123.pkl   │   (se crea automáticamente, NO subir a Git)
    └── CIU-00456.pkl   ←   Cada archivo = un ciudadano registrado
```

### ¿Qué es `face_db/`?
Es la base de datos local donde se almacenan los **embeddings faciales** (representación matemática del rostro) de cada ciudadano registrado. No contiene fotos, solo vectores numéricos.

---

## 🔌 Endpoints Disponibles

### Base URL
```
http://localhost:5000
```

---

### `GET /health`
Verifica que el servicio está activo. Llamar antes de cualquier operación crítica.

**Respuesta `200 OK`:**
```json
{
  "status": "ok",
  "service": "facial-recognition-api"
}
```

---

### `POST /api/register`
Registra el rostro de referencia de un ciudadano. **Llamar una sola vez al crear el expediente.**

Se puede llamar varias veces con el mismo `person_id` para agregar más fotos de referencia (mejora la precisión).

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "person_id": "CIU-00123",
  "image_base64": "<imagen en Base64>"
}
```

| Campo          | Tipo   | Requerido | Descripción                                      |
|----------------|--------|-----------|--------------------------------------------------|
| `person_id`    | String | ✅ Sí     | ID único del ciudadano o expediente              |
| `image_base64` | String | ✅ Sí     | Foto de referencia (cédula/documento) en Base64  |

**Respuesta exitosa `201 Created`:**
```json
{
  "success": true,
  "person_id": "CIU-00123",
  "total_encodings": 1,
  "message": "Rostro registrado correctamente para CIU-00123"
}
```

**Respuesta de error `422`** (sin rostro en la imagen):
```json
{
  "success": false,
  "error": "No se detectó ningún rostro en la imagen"
}
```

---

### `POST /api/verify` ⭐ Endpoint principal
Verifica si la persona en la imagen coincide con el registro almacenado.

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "person_id": "CIU-00123",
  "image_base64": "<foto capturada en tiempo real en Base64>"
}
```

| Campo          | Tipo   | Requerido | Descripción                                        |
|----------------|--------|-----------|----------------------------------------------------|
| `person_id`    | String | ✅ Sí     | ID del ciudadano a verificar                       |
| `image_base64` | String | ✅ Sí     | Foto capturada en el momento del trámite en Base64 |

**Respuesta `200 OK`:**
```json
{
  "success": true,
  "match": true,
  "confidence": 0.87,
  "distance": 0.13,
  "threshold": 0.40,
  "person_id": "CIU-00123",
  "timestamp": "2026-03-11T14:30:00Z",
  "message": "Identidad verificada"
}
```

| Campo        | Tipo    | Descripción                                                    |
|--------------|---------|----------------------------------------------------------------|
| `match`      | Boolean | ✅ **Campo principal.** `true` = persona correcta              |
| `confidence` | Float   | Nivel de confianza de 0.0 a 1.0 (1.0 = idéntico)             |
| `distance`   | Float   | Distancia coseno entre rostros (menor = más parecidos)         |
| `threshold`  | Float   | Umbral configurado actualmente                                 |
| `timestamp`  | String  | Fecha y hora UTC de la verificación (útil para auditoría)     |
| `message`    | String  | Descripción legible del resultado                              |

---

### `DELETE /api/delete/{person_id}`
Elimina el registro facial de un ciudadano del sistema.

**Ejemplo:**
```
DELETE http://localhost:5000/api/delete/CIU-00123
```

**Respuesta `200 OK`:**
```json
{
  "success": true,
  "person_id": "CIU-00123",
  "message": "Registro eliminado"
}
```

**Respuesta `404`** (ID no encontrado):
```json
{
  "success": false,
  "error": "person_id=CIU-00123 no encontrado"
}
```

---

### `GET /api/registered`
Lista todos los IDs de ciudadanos actualmente registrados. Útil para auditoría.

**Respuesta `200 OK`:**
```json
{
  "success": true,
  "registered_ids": ["CIU-00123", "CIU-00456", "CIU-00789"],
  "total": 3
}
```

---
