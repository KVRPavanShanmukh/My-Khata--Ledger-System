import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';

function Header() {
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    // Only logout when clicking Logout button, not OmniBank logo
    localStorage.removeItem('authToken');
    navigate('/');
  };

  // Don't show header navigation on auth pages or admin dashboard
  const hideNav =
    ['/', '/signup', '/admin'].includes(location.pathname) ||
    location.pathname.startsWith('/admin-dashboard');

  return (
    <header className="header">
      <div className="header-container">
        {/* Logo should just navigate to /user, not logout */}
        <Link to="/user" className="logo">OmniBank</Link>
        {!hideNav && (
          <nav>
            <ul>
              <li>
                <Link to="/user">Home</Link>
              </li>
              <li>
                <Link to="/user/add-customer">Add Customer</Link>
              </li>
              <li>
                <Link to="/user/add-transaction">Add Transaction</Link>
              </li>
              <li>
                <Link to="/user/ledger">Ledger</Link>
              </li>
              <li>
                <Link to="/user/profile">Profile</Link>
              </li>
              <li>
                <button onClick={handleLogout} className="logout-btn">Logout</button>
              </li>
            </ul>
          </nav>
        )}
      </div>
    </header>
  );
}

export default Header;
