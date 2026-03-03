const API_BASE_URL = "http://localhost:8081";

export const getExample = async () => {
  const response = await fetch(`${API_BASE_URL}/el-endpoint`);
  return response.json();
};