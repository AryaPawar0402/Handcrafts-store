import { useEffect, useState, useCallback } from "react";
import axios from "axios";
import "./PendingSellers.css";

export default function PendingSellers() {
  const [pendingSellers, setPendingSellers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [authorized, setAuthorized] = useState(false);

  const token = localStorage.getItem("token");

  const fetchPendingSellers = useCallback(async () => {
    try {
      const res = await axios.get(
        "http://localhost:8070/auth/users/pending-sellers",
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setPendingSellers(res.data);
    } catch (err) {
      alert("Failed to fetch pending sellers");
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
        if (role.includes("ROLE_ADMIN")) {
          setAuthorized(true);
          fetchPendingSellers();
        } else {
          alert("Access denied. Only admins can approve sellers.");
        }
      } catch (err) {
        console.error(err);
        alert("Failed to verify user role or session expired.");
      }
    };

    fetchUserAndData();
  }, [token, fetchPendingSellers]); // Add token and fetchPendingSellers as dependencies

  const handleApprove = async (id) => {
    try {
      await axios.put(
        `http://localhost:8070/auth/users/approve/${id}`,
        null,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      alert("Seller approved successfully!");
      fetchPendingSellers();
    } catch (err) {
      alert("Failed to approve seller");
      console.error(err);
    }
  };

  if (!authorized) return <p>Checking permissions...</p>;

  return (
    <div className="pending-container">
      <h2>Pending Sellers</h2>
      {loading ? (
        <p>Loading...</p>
      ) : pendingSellers.length === 0 ? (
        <p>No pending sellers found.</p>
      ) : (
        <ul className="seller-list">
          {pendingSellers.map((seller) => (
            <li key={seller.id} className="seller-item">
              <div>
                <strong>{seller.name}</strong> ({seller.email}) – {seller.roles}
              </div>
              <button onClick={() => handleApprove(seller.id)}>Approve</button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
