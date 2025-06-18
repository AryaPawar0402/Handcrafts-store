import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Navbar from "./components/Navbar";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Profile from "./pages/Profile";
import PendingSellers from "./pages/PendingSellers";
import ApprovedSellers from "./pages/ApprovedSellers";
import SellerStatus from "./pages/SellerStatus";

function App() {
  return (
    <Router>
      <Navbar />
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/pending" element={<PendingSellers />} />
        <Route path="/approved" element={<ApprovedSellers />} />
        <Route path="/seller-profile" element={<SellerStatus />} />
      </Routes>
    </Router>
  );
}

export default App;
