import { createContext, useContext, useMemo, useState, type ReactNode } from "react";

export type Credentials = {
  userId: string;
  login: string;
  password: string;
};

type AuthContextValue = {
  credentials: Credentials | null;
  authHeader: string | null;
  signIn: (credentials: Credentials) => void;
  signOut: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [credentials, setCredentials] = useState<Credentials | null>(null);

  const value = useMemo<AuthContextValue>(
    () => ({
      credentials,
      authHeader: credentials
        ? `Basic ${btoa(`${credentials.login}:${credentials.password}`)}`
        : null,
      signIn: setCredentials,
      signOut: () => setCredentials(null),
    }),
    [credentials],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
