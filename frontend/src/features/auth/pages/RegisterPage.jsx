import { useState } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./LoginPage.module.css";

// TODO: replace with real API call when backend is ready
export default function RegisterPage() {
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (formData.password !== formData.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setLoading(true);
    await new Promise((r) => setTimeout(r, 800)); // simulate network
    setLoading(false);
    navigate("/login");
  };

  return (
    <div className={styles.page}>

      <div className={styles.card}>
        <div className={styles.cardHeader}>
          <h2 className={styles.title}>Register</h2>
          <p className={styles.subtitle}>Create your E-Coffee account to get started.</p>
        </div>

        <form className={styles.form} onSubmit={handleSubmit}>
          <div className={styles.fieldRow}>
            <div className={styles.field}>
              <label htmlFor="firstName" className={styles.label}>First Name</label>
              <input id="firstName" name="firstName" type="text" placeholder="John"
                value={formData.firstName} onChange={handleChange} required disabled={loading} className={styles.input} />
            </div>
            <div className={styles.field}>
              <label htmlFor="lastName" className={styles.label}>Last Name</label>
              <input id="lastName" name="lastName" type="text" placeholder="Doe"
                value={formData.lastName} onChange={handleChange} required disabled={loading} className={styles.input} />
            </div>
          </div>

          <div className={styles.field}>
            <label htmlFor="email" className={styles.label}>Email Address</label>
            <input id="email" name="email" type="email" placeholder="your@email.com"
              value={formData.email} onChange={handleChange} required disabled={loading} className={styles.input} />
          </div>

          <div className={styles.field}>
            <label htmlFor="phone" className={styles.label}>Phone Number</label>
            <input id="phone" name="phone" type="tel" placeholder="0901 234 567"
              value={formData.phone} onChange={handleChange} disabled={loading} className={styles.input} />
          </div>

          <div className={styles.field}>
            <label htmlFor="password" className={styles.label}>Password</label>
            <input id="password" name="password" type="password" placeholder="••••••••"
              value={formData.password} onChange={handleChange} required disabled={loading} className={styles.input} />
          </div>

          <div className={styles.field}>
            <label htmlFor="confirmPassword" className={styles.label}>Confirm Password</label>
            <input id="confirmPassword" name="confirmPassword" type="password" placeholder="••••••••"
              value={formData.confirmPassword} onChange={handleChange} required disabled={loading} className={styles.input} />
          </div>

          {error && <p className={styles.error}>{error}</p>}

          <button type="submit" disabled={loading} className={styles.submitBtn}>
            {loading ? "CREATING ACCOUNT..." : "CREATE ACCOUNT"}
          </button>
        </form>

        <div className={styles.footer}>
          Already have an account?{" "}
          <button className={styles.linkBtn} onClick={() => navigate("/login")}>
            Sign in
          </button>
        </div>
      </div>

      <p className={styles.pageFooter}>
        E-Coffee •{" "}
        <span className={styles.pageFooterAccent}>Franchise Management System</span>
      </p>
    </div>
  );
}
