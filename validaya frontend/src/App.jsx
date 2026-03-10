import { useState } from "react";
import { login, getUsers } from "./services/api";

export default function App() {
  const [email, setEmail] = useState("admin@validaya.com");
  const [password, setPassword] = useState("pass123");
  const [token, setToken] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  const handleLogin = async () => {
    setError("");
    setResult(null);
    try {
      const res = await login(email, password);
      const t = res.data.token;
      setToken(t);
      setResult(res);
      // opcional: guardar para otras pantallas
      localStorage.setItem("token", t);
    } catch (e) {
      setError(e.message);
    }
  };

  const handleGetUsers = async () => {
    setError("");
    setResult(null);
    try {
      const t = token || localStorage.getItem("token");
      if (!t) throw new Error("No hay token. Primero haz login.");
      const res = await getUsers(t);
      setResult(res);
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <div style={{ padding: 20 }}>
      <h1>Validaya – Prueba conexión</h1>

      <div style={{ display: "grid", gap: 8, maxWidth: 360 }}>
        <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="email" />
        <input value={password} onChange={(e) => setPassword(e.target.value)} placeholder="password" type="password" />

        <button onClick={handleLogin}>1) Login</button>
        <button onClick={handleGetUsers}>2) Get Users (ADMIN)</button>
      </div>

      {error && <pre style={{ marginTop: 16, color: "crimson" }}>{error}</pre>}
      {result && <pre style={{ marginTop: 16 }}>{JSON.stringify(result, null, 2)}</pre>}
    </div>
  );
}