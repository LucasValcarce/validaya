import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/Login'
import Home  from './pages/Home'
import Docs  from './pages/Docs'
import Tramites from './pages/Tramites'
import TramiteFlow from './pages/TramiteFlow'
import Historial from './pages/Historial'
import Tickets from './pages/Tickets'
import Perfil from './pages/Perfil'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/"       element={<Navigate to="/login" replace />} />
        <Route path="/login"  element={<Login />} />
        <Route path="/home"   element={<Home />} />
        <Route path="/docs"   element={<Docs />} />
        <Route path="/historial" element={<Historial />} />
        <Route path="/tickets" element={<Tickets />} />
        <Route path="/perfil" element={<Perfil />} />
        <Route path="/tramites" element={<Tramites />} />
        <Route path="/tramite-flow" element={<TramiteFlow />} />
      </Routes>
    </BrowserRouter>
  )
}