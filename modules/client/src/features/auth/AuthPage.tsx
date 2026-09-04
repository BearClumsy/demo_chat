import { useState, type FormEvent } from "react";
import { useAuth } from "../../app/AuthContext";
import "./AuthPage.css";

type Mode = "login" | "signup";

export default function AuthPage() {
  const { signIn } = useAuth();
  const [mode, setMode] = useState<Mode>("login");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [login, setLogin] = useState("");
  const [password, setPassword] = useState("");
  const [userId, setUserId] = useState("");

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");

  function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    // HTTP Basic has no dedicated login endpoint — credentials are only verified against the
    // first real API call. There's also no "who am I" lookup by login, so the user id has to be
    // supplied directly here.
    signIn({ userId, login, password });
  }

  async function handleSignup(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      const response = await fetch("/api/users", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ firstName, lastName, email, phone: phone || undefined, login, password }),
      });
      if (!response.ok) {
        throw new Error(
          response.status === 409 ? "That email or login is already taken" : `Signup failed (${response.status})`,
        );
      }
      const created = await response.json();
      signIn({ userId: created.id, login, password });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Signup failed");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      {mode === "login" ? (
        <form className="auth-card" onSubmit={handleLogin}>
          <h1>Log in</h1>

          <label className="auth-field">
            <span>Login</span>
            <input value={login} onChange={(event) => setLogin(event.target.value)} required />
          </label>

          <label className="auth-field">
            <span>Password</span>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              minLength={8}
            />
          </label>

          <label className="auth-field">
            <span>Your user ID</span>
            <input
              value={userId}
              onChange={(event) => setUserId(event.target.value)}
              placeholder="UUID from when you signed up"
              required
            />
            <small className="auth-hint">
              There's no lookup-by-login endpoint yet, so paste in the id you got at signup.
            </small>
          </label>

          <button type="submit" className="auth-submit">
            Log in
          </button>

          {error && <p className="auth-message">{error}</p>}

          <p className="auth-toggle">
            Need an account?{" "}
            <button type="button" className="auth-link" onClick={() => { setMode("signup"); setError(null); }}>
              Sign up
            </button>
          </p>
        </form>
      ) : (
        <form className="auth-card" onSubmit={handleSignup}>
          <h1>Create an account</h1>

          <label className="auth-field">
            <span>First name</span>
            <input value={firstName} onChange={(event) => setFirstName(event.target.value)} required />
          </label>

          <label className="auth-field">
            <span>Last name</span>
            <input value={lastName} onChange={(event) => setLastName(event.target.value)} required />
          </label>

          <label className="auth-field">
            <span>Email</span>
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="you@example.com"
              required
            />
          </label>

          <label className="auth-field">
            <span>Phone (optional)</span>
            <input value={phone} onChange={(event) => setPhone(event.target.value)} />
          </label>

          <label className="auth-field">
            <span>Login</span>
            <input value={login} onChange={(event) => setLogin(event.target.value)} required />
          </label>

          <label className="auth-field">
            <span>Password</span>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              minLength={8}
            />
          </label>

          <button type="submit" className="auth-submit" disabled={isSubmitting}>
            {isSubmitting ? "Signing up…" : "Sign up"}
          </button>

          {error && <p className="auth-message">{error}</p>}

          <p className="auth-toggle">
            Already have an account?{" "}
            <button type="button" className="auth-link" onClick={() => { setMode("login"); setError(null); }}>
              Log in
            </button>
          </p>
        </form>
      )}
    </div>
  );
}
