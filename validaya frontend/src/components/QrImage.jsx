import { useEffect, useRef } from 'react'
import QRCode from 'qrcode'

export default function QrImage({ data, size = 200, className = '' }) {
  const canvasRef = useRef(null)

  useEffect(() => {
    if (!data || !canvasRef.current) return
    QRCode.toCanvas(canvasRef.current, data, {
      width: size,
      margin: 1,
      color: { dark: '#000000', light: '#ffffff' },
    })
  }, [data, size])

  if (!data) return null
  return <canvas ref={canvasRef} className={className} />
}