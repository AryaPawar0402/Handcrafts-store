import { jwtDecode } from 'jwt-decode';

// Check if user is logged in
export const isLoggedIn = () => !!localStorage.getItem("token");

// Get raw token
export const getToken = () => localStorage.getItem("token");

// Logout function
export const logoutUser = () => {
  localStorage.removeItem("token");
  window.location.href = "/login";
};

// Extract roles from JWT token
export const getUserRoles = () => {
  const token = getToken();
  if (!token) return [];

  try {
    const decoded = jwtDecode(token);
    return decoded.roles || [];
  } catch (error) {
    console.error("Error decoding token:", error);
    return [];
  }
};
