import { useState } from "react";
import { useNavigate } from "react-router-dom";
import styles from "@/features/authentication/pages/LoginPage.module.css";

// TODO: replace with real API call when backend is ready
export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [sent, setSent] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    await new Promise((r) => setTimeout(r, 800)); // simulate network
    setLoading(false);
    setSent(true);
  };

  return (
    <div className={styles.page}>

      <div className={styles.card}>
        {sent ? (
          <div className={styles.successBox}>
            <div className={styles.successIcon}>📬</div>
            <h2 className={styles.successTitle}>Check your email</h2>
            <p className={styles.successText}>
              We sent a password reset link to <strong>{email}</strong>.
            </p>
            <button className={styles.linkBtn} onClick={() => navigate("/login")}>
              Back to Sign In
            </button>
          </div>
        ) : (
          <>
            <div className={styles.cardHeader}>
              <h2 className={styles.title}>Forgot Password</h2>
              <p className={styles.subtitle}>Enter your email to receive a password reset link.</p>
            </div>

            <form className={styles.form} onSubmit={handleSubmit}>
              <div className={styles.field}>
                <label htmlFor="email" className={styles.label}>Email Address</label>
                <input
                  id="email" type="email" placeholder="your@email.com"
                  value={email} onChange={(e) => setEmail(e.target.value)}
                  required disabled={loading} className={styles.input}
                />
              </div>

              {error && <p className={styles.error}>{error}</p>}

              <button type="submit" disabled={loading} className={styles.submitBtn}>
                {loading ? "SENDING..." : "SEND RESET LINK"}
              </button>
            </form>

            <div className={styles.footer}>
              Remember your password?{" "}
              <button className={styles.linkBtn} onClick={() => navigate("/login")}>
                Back to Sign In
              </button>
            </div>
          </>
        )}
      </div>

      <p className={styles.pageFooter}>
        E-Coffee •{" "}
        <span className={styles.pageFooterAccent}>Franchise Management System</span>
      </p>
    </div>
  );
}
