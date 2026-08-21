import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { AuthProvider } from './contexts/AuthContext'; // Importe o Provider

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <AuthProvider> {/* <--- ISSO É OBRIGATÓRIO PARA O useAuth() FUNCIONAR */}
      <App />
    </AuthProvider>
  </React.StrictMode>
);