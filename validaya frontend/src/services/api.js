const API_BASE_URL = "http://localhost:8081";

export const getExample = async (email, password) => {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      email: 'admin@validaya.com',
      password: 'pass123'
    })
  });
  
  return response.json();
};