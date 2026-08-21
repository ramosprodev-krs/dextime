import React, { createContext, useState, useEffect, useContext, type ReactNode } from 'react';
import { api } from '../services/api';
// --- Tipagens ---
interface LoginRequest {
  email: string;
  password: string;
}

interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  password: string;
  role?: string;
}

interface AuthResponse {
  token: string;
}

interface AuthContextData {
  isAuthenticated: boolean;
  loading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
}

// --- Criação do Contexto ---
const AuthContext = createContext<AuthContextData>({} as AuthContextData);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);

  // Verifica o cache local (localStorage) assim que o app é aberto
  useEffect(() => {
    const token = localStorage.getItem('token');
    
    if (token) {
      // Se tiver token no cache, já injeta na API e loga o usuário direto
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      setIsAuthenticated(true);
    }
    
    setLoading(false);
  }, []);

  const login = async (credentials: LoginRequest) => {
    try {
      const response = await api.post<AuthResponse>('/auth/login', credentials);
      const { token } = response.data;

      // Salva no cache para manter a sessão
      localStorage.setItem('token', token);
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      setIsAuthenticated(true);
    } catch (error) {
      console.error('Erro ao fazer login:', error);
      throw error;
    }
  };

  const register = async (data: RegisterRequest) => {
    try {
      const response = await api.post<AuthResponse>('/auth/register', data);
      const { token } = response.data;

      localStorage.setItem('token', token);
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      setIsAuthenticated(true);
    } catch (error) {
      console.error('Erro ao registrar:', error);
      throw error;
    }
  };

  const logout = () => {
    // Remove apenas a autorização do cabeçalho da API e o estado local.
    // O cache (localStorage) é mantido para evitar a necessidade de login constante.
    delete api.defaults.headers.common['Authorization'];
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

// --- Hook Customizado ---
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de um AuthProvider');
  }
  return context;
};