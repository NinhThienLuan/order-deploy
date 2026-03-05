import { Outlet } from "react-router-dom";
import WarehouseAdminSideBar from "./AdminSideBar";
import "./AdminPage.css";

const AdminLayout = () => {
  return (
    <div className="admin-layout">
      {/* --- SIDEBAR CỐ ĐỊNH --- */}
      <aside className="admin-sidebar admin-sidebar-fixed">
        <WarehouseAdminSideBar />
      </aside>

      {/* --- CONTENT CHÍNH --- */}
      <main className="admin-content">
        <Outlet />
      </main>
    </div>
  );
};

export default AdminLayout;
