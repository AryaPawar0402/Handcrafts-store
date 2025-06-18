import { Link, useNavigate } from "react-router-dom";
import { getToken, getUserRoles } from "../utils/auth";
import { useEffect } from "react";
import "./Navbar.css";

export default function Navbar() {
  const navigate = useNavigate();

  const token = getToken();
  const roles = getUserRoles(); // ["ROLE_ADMIN", "ROLE_USER"]

  useEffect(() => {
    // You can add any needed effects here if required
  }, [token, roles]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/login");
  };

  return (
    <nav className="navbar">
      {/* Left side: general links or logo */}
      <ul className="navbar-left">
        <li><Link to="/">Home</Link></li>
      </ul>

      {/* Right side: auth related links */}
      <ul className="navbar-right">
        {!token && (
          <>
            <li><Link to="/login">Login</Link></li>
            <li><Link to="/register">Register</Link></li>
          </>
        )}

        {token && (
          <>
            <li><Link to="/profile">Profile</Link></li>

            {roles.includes("ROLE_SELLER") && (
              <li><Link to="/seller-profile">Seller Status</Link></li>
            )}

            {roles.includes("ROLE_ADMIN") && (
              <>
                <li><Link to="/pending">Pending Sellers</Link></li>
                <li><Link to="/approved">Approved Sellers</Link></li>
              </>
            )}

            <li>
              <button className="logout-button" onClick={handleLogout}>
                Logout
              </button>
            </li>
          </>
        )}
      </ul>
    </nav>
  );
}
