import { api } from "@/utils/api";
import {
    LoginRequest,
    LoginResponse,
    RegisterRequest,
    RegisterResponse,
} from "@/models/auth";

export const loginUser = async (data: LoginRequest): Promise<LoginResponse> => {
    try {
        const response = await api.post("/usuarios/login", { usuario: data });
        return response.data;
    }
    catch (err: any) {
        throw new Error(err.response?.data?.error || "Erro ao fazer login");
    }
};

export const registerUser = async (data: RegisterRequest): Promise<RegisterResponse> => {
    try {
        const response = await api.post("/usuarios", { usuario: data });
        return response.data;
    } catch (err: any) {
        throw new Error(err.response?.data?.error || "Erro ao registrar usuário");
    }
};

