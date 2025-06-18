import { useEffect, useState } from "react";
import axios from "axios";
import "./Profile.css";

export default function Profile() {
  const [userInfo, setUserInfo] = useState(null);
  const [editMode, setEditMode] = useState(false);
  const [formData, setFormData] = useState({ name: "", password: "" });

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      axios
        .get("http://localhost:8070/auth/userinfo", {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((res) => {
          setUserInfo(res.data);
          setFormData({ name: res.data.name, password: "" });
        })
        .catch((err) => {
          console.error(err);
          alert("Failed to fetch user info");
        });
    }
  }, []);

  const handleEdit = () => setEditMode(true);

  const handleChange = (e) =>
    setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSave = () => {
    if (!formData.name.trim() || !formData.password.trim()) {
      alert("Name and password cannot be empty.");
      return;
    }

    const token = localStorage.getItem("token");
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

  if (!userInfo) return <p>Loading...</p>;

  return (
    <div className="profile-container">
      <div className="profile-card">
        <h2>My Profile</h2>

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

        {editMode ? (
          <button onClick={handleSave}>Save</button>
        ) : (
          <button onClick={handleEdit}>Edit</button>
        )}
      </div>
    </div>
  );
}
