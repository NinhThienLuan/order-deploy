import { useState, useEffect } from "react";
import { NavLink, useLocation } from "react-router-dom";
import {
  Package, Images, Sliders, Tag, ShoppingCart, ClipboardList,
  AlertTriangle, BarChart3, Star, Ticket, Settings, LogOut, BookOpen,
  Monitor,
} from "lucide-react";
import { useAuth } from '@/features/authentication/services/AuthContext';
import "./AdminSidebar.css";

// ── Menu definitions ─────────────────────────────────────────────

const ADMIN_MENU = [
  {
    type: "folder",
    name: "Products",
    icon: <Package size={20} />,
    items: [
      { name: "Master Products",      path: "/admin/catalog",          icon: <Package size={18} /> },
      { name: "Categories",           path: "/admin/categories",       icon: <Tag size={18} /> },
    ],
  },
  {
    type: "folder",
    name: "Orders",
    icon: <ShoppingCart size={20} />,
    items: [
      { name: "All Orders",   path: "/admin/orders",        icon: <ClipboardList size={18} /> },
      { name: "Store Orders", path: "/admin/stores/orders", icon: <ShoppingCart size={18} /> },
      { name: "POS Terminal", path: "/admin/pos",           icon: <Monitor size={18} /> },
    ],
  },
  {
    type: "folder",
    name: "Finance",
    icon: <BarChart3 size={20} />,
    items: [
      { name: "Revenue Stats",         path: "#", icon: <BarChart3 size={18} /> },
      { name: "Transaction History",   path: "#", icon: <ClipboardList size={18} /> },
      { name: "Promotions & Coupons",  path: "#", icon: <Ticket size={18} /> },
    ],
  },
  {
    type: "item",
    name: "System Settings",
    path: "/admin/system-configuration",
    icon: <Settings size={20} />,
  },
];

const MANAGER_MENU = [
  {
    type: "folder",
    name: "Menu Management",
    icon: <BookOpen size={20} />,
    items: [
      { name: "Store Menu",    path: "/admin/catalog",       icon: <BookOpen size={18} /> },
      { name: "Out of Stock",  path: "/admin/out-of-stock",  icon: <AlertTriangle size={18} /> },
    ],
  },
  {
    type: "folder",
    name: "Orders",
    icon: <ShoppingCart size={20} />,
    items: [
      { name: "Store Orders", path: "/admin/stores/orders", icon: <ClipboardList size={18} /> },
      { name: "POS Terminal", path: "/admin/pos",           icon: <Monitor size={18} /> },
    ],
  },
  {
    type: "folder",
    name: "Reports",
    icon: <BarChart3 size={20} />,
    items: [
      { name: "Sales Summary",     path: "#", icon: <BarChart3 size={18} /> },
      { name: "Customer Reviews",  path: "#", icon: <Star size={18} /> },
    ],
  },
];

// ── Component ────────────────────────────────────────────────────

const AdminSidebar = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const isAdmin = user?.role === "FRANCHISE_ADMIN";
  const menuStructure = isAdmin ? ADMIN_MENU : MANAGER_MENU;
  const roleLabel = isAdmin ? "Franchise Admin" : "Store Manager";
  const avatarLetter = (user?.name?.[0] ?? (isAdmin ? "A" : "M")).toUpperCase();

  const [openFolders, setOpenFolders] = useState({});

  // Auto-open the folder that contains the currently active route
  useEffect(() => {
    const updates = {};
    menuStructure.forEach(item => {
      if (item.type === "folder") {
        const hasActive = item.items.some(
          sub => sub.path !== "#" && location.pathname.startsWith(sub.path)
        );
        if (hasActive) updates[item.name] = true;
      }
    });
    if (Object.keys(updates).length > 0) {
      setOpenFolders(prev => ({ ...prev, ...updates }));
    }
  }, [location.pathname]); // eslint-disable-line react-hooks/exhaustive-deps

  const toggleFolder = (name) =>
    setOpenFolders(prev => ({ ...prev, [name]: !prev[name] }));

  return (
    <aside className="admin-sidebar">
      {/* Logo */}
      <div className="logo-section">
        <div className="logo-container">
          <div className="logo-icon">{avatarLetter}</div>
          <div className="logo-text">
            <h1>E-Coffee</h1>
            <p>{roleLabel}</p>
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
                          className={({ isActive }) => `nav-item nav-subitem ${isActive ? "active" : ""}`}
                        >
                          <div className="icon">{subItem.icon}</div>
                          <span>{subItem.name}</span>
                        </NavLink>
                      ))}
                    </div>
                  )}
                </div>
              );
            }
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
          })}
        </div>
      </nav>

      {/* User Section */}
      <div className="user-section">
        <div className="user-info">
          <div className="user-avatar">{avatarLetter}</div>
          <div className="user-details">
            <div className="user-name">{user?.name ?? roleLabel}</div>
          </div>
        </div>
        <button
          className="logout-btn"
          onClick={() => {
            if (window.confirm("Are you sure you want to log out?")) {
              logout();
            }
          }}
        >
          <LogOut size={18} />
          <span>Log Out</span>
        </button>
      </div>
    </aside>
  );
};

export default AdminSidebar;
