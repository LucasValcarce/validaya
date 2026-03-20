import { useEffect, useRef } from 'react'
import QRCode from 'qrcode'

export default function QrImage({ data, size = 200, className = '' }) {
  if (!data) return null

  // Caso 1: es una imagen PNG en base64 (Stereum en producción)
  // Los PNG en base64 empiezan con "iVBOR"
  if (data.startsWith('iVBOR') || data.startsWith('data:image')) {
    const src = data.startsWith('data:') ? data : `data:image/png;base64,${data}`
    return <img src={src} alt="QR de pago" className={className} />
  }

  // Caso 2: es texto o JSON codificado en base64 (modo tester)
  // Decodificamos el base64 para obtener el payload real
  let payload = data
  try {
    payload = atob(data)
  } catch {
    // Si falla atob, usamos el string tal cual
    payload = data
  }

  // Generamos el QR con la API externa usando el payload decodificado
  const url = `https://api.qrserver.com/v1/create-qr-code/?size=${size}x${size}&data=${encodeURIComponent(payload)}`
  return <img src={url} alt="QR de pago" className={className} />
}