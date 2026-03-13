import Layout from '../components/Layout'

export default function Perfil() {
  // TODO: conectar con los datos reales del backend (usuario logueado)
  const usuario = {
    nombre: 'Juan Mamani',
    ci: '12345678',
    correo: 'juan.mamani@example.com',
    telefono: '+591 70000000',
    ciudad: 'La Paz',
  }

  return (
    <Layout title="👤 Mi perfil">

      {/* Encabezado */}
      <div className="bg-white border border-gray-200 rounded-2xl p-5 mb-6 flex items-center gap-4">
        <div className="w-14 h-14 rounded-full bg-teal flex items-center justify-center text-white font-black text-lg flex-shrink-0">
          JM
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-black text-navy truncate">{usuario.nombre}</p>
          <p className="text-xs text-gray-400 mt-1">CI: {usuario.ci}</p>
          <p className="text-xs text-gray-400">{usuario.correo}</p>
        </div>
        <span className="hidden sm:inline-flex px-3 py-1.5 rounded-full bg-teal-light text-teal text-[11px] font-bold">
          Cuenta verificada ✓
        </span>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">

        {/* Datos personales */}
        <div className="lg:col-span-2 bg-white border border-gray-200 rounded-2xl p-5">
          <h2 className="text-sm font-black text-navy mb-4">Datos personales</h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1.5">
                Nombre completo
              </label>
              <input
                type="text"
                value={usuario.nombre}
                readOnly
                className="w-full px-3 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-sm text-navy"
              />
            </div>
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1.5">
                Cédula de Identidad
              </label>
              <input
                type="text"
                value={usuario.ci}
                readOnly
                className="w-full px-3 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-sm text-navy"
              />
            </div>
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1.5">
                Correo electrónico
              </label>
              <input
                type="email"
                value={usuario.correo}
                readOnly
                className="w-full px-3 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-sm text-navy"
              />
            </div>
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1.5">
                Teléfono
              </label>
              <input
                type="tel"
                value={usuario.telefono}
                readOnly
                className="w-full px-3 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-sm text-navy"
              />
            </div>
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wide text-gray-400 mb-1.5">
                Ciudad
              </label>
              <input
                type="text"
                value={usuario.ciudad}
                readOnly
                className="w-full px-3 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-sm text-navy"
              />
            </div>
          </div>

          <p className="text-[11px] text-gray-400 mt-4">
            Estos datos provienen de tu registro y de las instituciones con las que realizas trámites.
          </p>
        </div>

        {/* Seguridad y preferencias */}
        <div className="flex flex-col gap-4">
          <div className="bg-white border border-gray-200 rounded-2xl p-5">
            <h3 className="text-sm font-black text-navy mb-3">Seguridad de la cuenta</h3>
            <ul className="flex flex-col gap-2">
              <li className="flex items-center justify-between text-xs text-gray-600">
                <span>Contraseña</span>
                <button className="text-teal text-[11px] font-bold hover:underline">
                  Cambiar
                </button>
              </li>
              <li className="flex items-center justify-between text-xs text-gray-600">
                <span>Biometría facial</span>
                <span className="px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700 text-[10px] font-bold">
                  Activa
                </span>
              </li>
            </ul>
          </div>

          <div className="bg-white border border-gray-200 rounded-2xl p-5">
            <h3 className="text-sm font-black text-navy mb-3">Preferencias</h3>
            <div className="flex flex-col gap-3 text-xs text-gray-600">
              <label className="flex items-center justify-between gap-3">
                <span>Notificaciones de estado de trámites</span>
                <input type="checkbox" defaultChecked className="w-4 h-4 accent-teal" />
              </label>
              <label className="flex items-center justify-between gap-3">
                <span>Recordatorios de vencimiento de documentos</span>
                <input type="checkbox" defaultChecked className="w-4 h-4 accent-teal" />
              </label>
              <label className="flex items-center justify-between gap-3">
                <span>Boletín de novedades</span>
                <input type="checkbox" className="w-4 h-4 accent-teal" />
              </label>
            </div>
          </div>
        </div>
      </div>

    </Layout>
  )
}


