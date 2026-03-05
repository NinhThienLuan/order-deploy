import { useState } from "react";
import { NavLink } from "react-router-dom";
import { Zap, Users, Car, LogOut, BarChart3, Settings, FileText, MessageSquare, MapPin, Ticket } from "lucide-react";
import "./AdminSideBar.css";

const AdminSidebar = () => {
  const [openFolders, setOpenFolders] = useState({});

  const toggleFolder = (folderName) => {
    setOpenFolders(prev => ({
      ...prev,
      [folderName]: !prev[folderName]
    }));
  };

  const menuStructure = [
    {
      type: "folder",
      name: "Quản lý Suppiler",
      icon: <Zap size={20} />,
      items: [
        { name: "Sản Phẩm", path: "##", icon: <MapPin size={18} /> },
        { name: "Danh Mục", path: "/admin/WarehouseCategory", icon: <Zap size={18} /> },
      ]
    },
    {
      type: "folder",
      name: "Quản lý Indgredient",
      icon: <Users size={20} />,
      items: [
        { name: "Nguyên liệu", path: "/admin/ingredient", icon: <Users size={18} /> },
      ]
    },
    {
      type: "folder",
      name: "Báo cáo & Đánh giá",
      icon: <BarChart3 size={20} />,
      items: [
        { name: "Quản lý báo cáo", path: "###", icon: <BarChart3 size={18} /> },
        { name: "Đánh giá người dùng", path: "###", icon: <FileText size={18} /> },
      ]
    },
    {
      type: "folder",
      name: "Tài chính",
      icon: <BarChart3 size={20} />,
      items: [
        { name: "Thống kê doanh thu", path: "###", icon: <BarChart3 size={18} /> },
        { name: "Lịch sử giao dịch", path: "###", icon: <FileText size={18} /> },
        { name: "Mã giảm giá", path: "###", icon: <Ticket size={18} /> },
      ]
    },
    {
      type: "item",
      name: "Cấu hình hệ thống",
      path: "/admin/system-configuration",
      icon: <Settings size={20} />
    },
  ];

  return (
    <aside className="admin-sidebar">
      {/* Logo Section */}
      <div className="logo-section">
        <div className="logo-container">
          <div className="logo-icon">S</div>
          <div className="logo-text">
            <h1>Admin</h1>
            <p>Quản trị hệ thống</p>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="navigation">
        <div className="nav-items">
          {menuStructure.map((item, index) => {
            if (item.type === "folder") {
              const isOpen = openFolders[item.name];
              return (
                <div key={index} className="nav-folder">
                  <div className="nav-folder-header" onClick={() => toggleFolder(item.name)}>
                    <div className="folder-left">
                      <div className="icon">{item.icon}</div>
                      <span>{item.name}</span>
                    </div>
                    <div className="folder-arrow">{isOpen ? "▼" : "▶"}</div>
                  </div>
                  {isOpen && (
                    <div className="nav-folder-items">
                      {item.items.map(subItem => (
                        <NavLink
                          key={subItem.path}
                          to={subItem.path}
                          className={({ isActive }) => `nav-item nav-subitem ${isActive ? "item" : ""}`}
                        >
                          <div className="icon">{subItem.icon}</div>
                          <span>{subItem.name}</span>
                        </NavLink>
                      ))}
                    </div>
                  )}
                </div>
              );
            } else {
              return (
                <NavLink
                  key={item.path}
                  to={item.path}
                  className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}
                >
                  <div className="icon">{item.icon}</div>
                  <span>{item.name}</span>
                </NavLink>
              );
            }
          })}
        </div>
      </nav>

      {/* Dashboard Static Section */}
      

      {/* User Section */}
      <div className="user-section">
        <div className="user-info">
          <div className="user-avatar">A</div>
          <div className="user-details">
            <div className="user-name">Quản trị viên</div>
          </div>
        </div>
        <button
          className="logout-btn"
          onClick={() => {
            if (window.confirm("Bạn có chắc chắn muốn đăng xuất?")) {
              localStorage.removeItem("token");
              localStorage.removeItem("user_role");
              window.location.href = "/login";
            }
          }}
        >
          <LogOut size={18} />
          <span>Đăng xuất</span>
        </button>
      </div>
    </aside>
  );
};

export default AdminSidebar;
