import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';

function Login({ setUser }) {
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState('');
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [isOtpSent, setIsOtpSent] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handlePhonePasswordSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');
    setLoading(true);

    if (!/^\d{10}$/.test(phone)) {
      setError('A valid 10-digit phone number is required');
      setLoading(false);
      return;
    }

    if (!email.trim() || !/^\S+@\S+\.\S+$/.test(email)) {
      setError('A valid email address is required');
      setLoading(false);
      return;
    }

    if (!password.trim()) {
      setError('Password is required');
      setLoading(false);
      return;
    }

    try {
      await axios.post('http://localhost:1014/api/auth/login', {
        mobile: phone,
        email: email,
        password: password,
      });

      setIsOtpSent(true);
      setSuccessMessage('OTP sent to your email.');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Check phone, email, and password.');
    } finally {
      setLoading(false);
    }
  };

  const handleOtpSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');
    setLoading(true);

    if (!otp.trim() || otp.length !== 6) {
      setError('Enter a valid 6-digit OTP');
      setLoading(false);
      return;
    }

    try {
      const response = await axios.post('http://localhost:1014/api/auth/login/verify', {
        mobile: phone,
        code: otp
      });

      setSuccessMessage('Login successful! Redirecting to dashboard...');
      if (setUser) {
        setUser(response.data);
      }
      setTimeout(() => {
        navigate('/user');
      }, 1000);
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid or expired OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleResendOtp = async () => {
    setError('');
    setSuccessMessage('');
    setLoading(true);

    try {
      await axios.post('http://localhost:1014/api/auth/login', {
        mobile: phone,
        email: email,
        password: password,
      });

      setSuccessMessage('OTP resent to your email.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to resend OTP.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h2>Login to OmniBank</h2>
      {error && <p className="error-message">{error}</p>}
      {successMessage && <p className="success-message">{successMessage}</p>}

      {!isOtpSent ? (
        <form onSubmit={handlePhonePasswordSubmit}>
          <div className="form-group">
            <label>Phone Number</label>
            <input
              type="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder="Enter 10-digit phone number"
              required
              pattern="[0-9]{10}"
              maxLength={10}
              disabled={loading}
            />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your registered email"
              required
              disabled={loading}
            />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
              disabled={loading}
            />
          </div>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Sending...' : 'Send OTP'}
          </button>
        </form>
      ) : (
        <form onSubmit={handleOtpSubmit}>
          <div className="form-group">
            <label>Enter OTP</label>
            <input
              type="text"
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              placeholder="Enter the 6-digit OTP"
              maxLength={6}
              required
              pattern="[0-9]{6}"
              disabled={loading}
            />
          </div>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Verifying...' : 'Verify OTP'}
          </button>
          <button
            type="button"
            className="btn-secondary"
            onClick={handleResendOtp}
            disabled={loading}
          >
            {loading ? 'Resending...' : 'Resend OTP'}
          </button>
          <button
            type="button"
            className="btn-secondary"
            onClick={() => setIsOtpSent(false)}
            disabled={loading}
          >
            Back
          </button>
        </form>
      )}

      <div className="auth-links">
        <p>New user? <Link to="/signup">Sign up here</Link></p>
        <p>Admin? <Link to="/admin">Admin Login</Link></p>
      </div>
    </div>
  );
}

export default Login;