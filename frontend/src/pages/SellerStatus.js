import { useEffect, useState } from "react";
import axios from "axios";
import "./SellerStatus.css";

export default function SellerStatus() {
  const [userInfo, setUserInfo] = useState(null);
  const [editMode, setEditMode] = useState(false);
  const [formData, setFormData] = useState({ name: "", password: "" });
  const [status, setStatus] = useState(null);
  const token = localStorage.getItem("token");

  useEffect(() => {
    if (!token) return;

    // Fetch user profile info including status
    axios
      .get("http://localhost:8070/auth/userinfo", {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((res) => {
        setUserInfo(res.data);
        setFormData({ name: res.data.name, password: "" });
        setStatus(res.data.status?.toLowerCase()); // Use status from userInfo
      })
      .catch((err) => {
        console.error(err);
        alert("Failed to fetch user info");
      });
  }, [token]);

  const handleEdit = () => setEditMode(true);

  const handleChange = (e) =>
    setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSave = () => {
    if (!formData.name.trim() || !formData.password.trim()) {
      alert("Name and password cannot be empty.");
      return;
    }

    axios
      .put("http://localhost:8070/auth/profile", formData, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then(() => {
        alert("Profile updated successfully!");
        setUserInfo({ ...userInfo, name: formData.name });
        setFormData({ ...formData, password: "" });
        setEditMode(false);
      })
      .catch((err) => {
        console.error(err);
        alert("Failed to update profile");
      });
  };

  if (!userInfo || !status) return <p>Loading...</p>;

  return (
    <div className="seller-status-container">
      <div className="profile-card">
        <h2>My Profile (Seller)</h2>

        <div className="profile-field">
          <label>Name:</label>
          {editMode ? (
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
            />
          ) : (
            <p>{userInfo.name}</p>
          )}
        </div>

        <div className="profile-field">
          <label>Email:</label>
          <p>{userInfo.email}</p>
        </div>

        <div className="profile-field">
          <label>Role:</label>
          <p>{userInfo.roles}</p>
        </div>

        <div className="profile-field">
          <label>Password:</label>
          {editMode ? (
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
            />
          ) : (
            <p>********</p>
          )}
        </div>

        <div className="profile-field">
          <label>Approval Status:</label>
          <p
            className={`status-box ${
              status === "approved"
                ? "status-approved"
                : status === "pending"
                ? "status-pending"
                : ""
            }`}
          >
            {status === "approved"
              ? "Approved"
              : status === "pending"
              ? "Pending Approval"
              : "Unknown"}
          </p>
        </div>

        {editMode ? (
          <button onClick={handleSave}>Save</button>
        ) : (
          <button onClick={handleEdit}>Edit Profile</button>
        )}
      </div>
    </div>
  );
}
