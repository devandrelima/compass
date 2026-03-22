"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { LoginRequest } from "@/models/auth";
import { useAuth } from "@/context/AuthContext";
import { toast } from "sonner";
import { loginUser } from "@/services/authService";

export default function LoginPage() {
    const router = useRouter();
    const { login, logout } = useAuth();

    const [form, setForm] = useState<LoginRequest>({ email: "", senha: "" });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        logout();
    }, []);

    async function handleLogin(e: any) {
        e.preventDefault();
        setLoading(true);
        setError("");

        try {
            const data = await loginUser(form);
            login(data.usuario, data.token);

            toast.success(`${data.usuario.nome}, bem-vindo ao sistema!`);
            router.push("/home");

        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="w-screen h-screen flex items-center justify-center bg-gray-100">
            <form
                onSubmit={handleLogin}
                className="bg-white p-6 rounded-xl shadow-md w-96 flex flex-col gap-4"
            >
                <h1 className="text-2xl font-bold text-center">Login</h1>

                <input
                    type="email"
                    placeholder="Email"
                    value={form.email}
                    onChange={(e) => setForm({ ...form, email: e.target.value })}
                    className="border p-2 rounded"
                    required
                />

                <input
                    type="password"
                    placeholder="Senha"
                    value={form.senha}
                    onChange={(e) => setForm({ ...form, senha: e.target.value })}
                    className="border p-2 rounded"
                    required
                />

                <button
                    type="submit"
                    disabled={loading}
                    className="bg-blue-500 text-white py-2 rounded hover:bg-blue-600 transition"
                >
                    {loading ? "Entrando..." : "Entrar"}
                </button>

                <a href="/register" className="text-blue-500 hover:underline text-center">
                    Não tem uma conta? Registre-se
                </a>

                {error && <p className="text-red-500 text-center">{error}</p>}
            </form>
        </div>
    );
}
