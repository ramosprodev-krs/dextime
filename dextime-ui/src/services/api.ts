import axios from 'axios';

export const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
});

// Interceptor de Requisição: Injeta o token antes da chamada ir para o backend
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  
  return config;
});

// Interceptor de Resposta: Lida com erros globais
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      console.warn('Acesso não autorizado ou sessão expirada.');
    }
    return Promise.reject(error);
  }
);