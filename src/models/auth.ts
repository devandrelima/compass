export interface LoginRequest {
    email: string;
    senha: string;
}

export interface LoginResponse {
    mensagem: string;
    usuario: Usuario;
    token: string;
}

export interface RegisterRequest {
    nome: string;
    email: string;
    senha: string;
}

export interface RegisterResponse {
    mensagem: string;
}

export interface Usuario {
    id: number;
    nome: string;
    email: string;
    role: string;
}