const API_BASE_URL = "http://localhost:8081/api/v1";

export async function login(email, password) {
  const res = await fetch(`${API_BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  const data = await res.json();
  if (!res.ok || data?.success === false) {
    throw new Error(data?.message || `Login failed (${res.status})`);
  }
  return data; // { success, data:{token,...}, timestamp }
}

export async function getUsers(token) {
  const res = await fetch(`${API_BASE_URL}/users`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  const data = await res.json().catch(() => null);
  if (!res.ok) {
    throw new Error(
      (data && JSON.stringify(data)) || `Get users failed (${res.status})`
    );
  }
  return data; // ApiResponse<List<UserDto.Response>> (según tu backend)
}