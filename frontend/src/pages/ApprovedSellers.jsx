import { useEffect, useState, useCallback } from "react";
import axios from "axios";
import "./ApprovedSellers.css";

export default function ApprovedSellers() {
  const [approvedSellers, setApprovedSellers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [authorized, setAuthorized] = useState(false);

  const token = localStorage.getItem("token");

  const fetchApprovedSellers = useCallback(async () => {
    try {
      const res = await axios.get(
        "http://localhost:8070/auth/users/approved-sellers",
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setApprovedSellers(res.data);
    } catch (err) {
      alert("Failed to fetch approved sellers");
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    const fetchUserAndData = async () => {
      try {
        const res = await axios.get("http://localhost:8070/auth/userinfo", {
          headers: { Authorization: `Bearer ${token}` },
        });

        const role = res.data.roles;
        if (role.includes("ROLE_ADMIN") || role.includes("ROLE_SELLER")) {
          setAuthorized(true);
          fetchApprovedSellers();
        } else {
          alert("Access denied");
        }
      } catch (err) {
        console.error(err);
        alert("Failed to verify user role or session expired.");
      }
    };

    fetchUserAndData();
  }, [fetchApprovedSellers, token]);

  if (!authorized) return <p>Checking permissions...</p>;

  return (
    <div className="approved-container">
      <h2>Approved Sellers</h2>
      {loading ? (
        <p>Loading...</p>
      ) : approvedSellers.length === 0 ? (
        <p>No approved sellers found.</p>
      ) : (
        <ul className="seller-list">
          {approvedSellers.map((seller) => (
            <li key={seller.id} className="seller-item">
              <strong>{seller.name}</strong> ({seller.email}) – {seller.roles}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
