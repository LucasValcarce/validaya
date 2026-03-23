import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/Login'
import Home  from './pages/Home'
import Docs  from './pages/Docs'
import Tramites from './pages/Tramites'
import TramiteFlow from './pages/TramiteFlow'
import Historial from './pages/Historial'
import Tickets from './pages/Tickets'
import Perfil from './pages/Perfil'
import Landing from './pages/Landing'
import { Analytics } from '@vercel/analytics/react'
import { getToken } from './services/authService'


function PrivateRoute({ children }) {
  return getToken() ? children : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/"     element={<Landing />} />
        <Route path="/login" element={<Login />} />

        <Route path="/home"         element={<PrivateRoute><Home /></PrivateRoute>} />
        <Route path="/docs"         element={<PrivateRoute><Docs /></PrivateRoute>} />
        <Route path="/historial"    element={<PrivateRoute><Historial /></PrivateRoute>} />
        <Route path="/tickets"      element={<PrivateRoute><Tickets /></PrivateRoute>} />
        <Route path="/perfil"       element={<PrivateRoute><Perfil /></PrivateRoute>} />
        <Route path="/tramites"     element={<PrivateRoute><Tramites /></PrivateRoute>} />
        <Route path="/tramite-flow" element={<PrivateRoute><TramiteFlow /></PrivateRoute>} />
      </Routes>
      <Analytics />
    </BrowserRouter>
  )
}