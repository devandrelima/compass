"use client";

import * as React from "react";
import { createContext, useContext, useEffect, useState } from "react";
import { Usuario } from "@/models/auth";

interface AuthContextType {
  usuario: Usuario | null;
  token: string | null;
  login: (usuario: Usuario, token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

type Props = {
  children: React.ReactNode;
};

export const AuthProvider: React.FC<Props> = ({ children }) => {
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    if (typeof window !== "undefined") {
      const savedToken = localStorage.getItem("token");
      const savedUser = localStorage.getItem("usuario");

      if (savedToken && savedUser) {
        try {
          setToken(savedToken);
          setUsuario(JSON.parse(savedUser));
        } catch {
          localStorage.removeItem("usuario");
        }
      }
    }
  }, []);

  const login = (usuario: Usuario, token: string) => {
    setUsuario(usuario);
    setToken(token);
    localStorage.setItem("token", token);
    localStorage.setItem("usuario", JSON.stringify(usuario));
  };

  const logout = () => {
    setUsuario(null);
    setToken(null);
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
  };

  return React.createElement(
    AuthContext.Provider,
    { value: { usuario, token, login, logout } },
    children
  );
};

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth deve ser usado dentro de AuthProvider");
  }
  return context;
}