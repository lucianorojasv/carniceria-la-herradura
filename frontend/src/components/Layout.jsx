import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const items = [
  ['📊', 'Dashboard', '/dashboard'],
  ['🧾', 'Pedidos', '/pedidos'],
  ['🥩', 'Productos', '/productos'],
  ['👥', 'Clientes', '/clientes'],
  ['🛵', 'Delivery', '/delivery'],
  ['🔥', 'Promociones', '/promociones'],
  ['📍', 'Locales', '/locales'],
  ['🤖', 'Asistente', '/asistente'],
  ['⚙️', 'Configuración', '/configuracion']
];

export default function Layout({ children }) {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  return <div className="app-shell">
    <aside className={open ? 'sidebar open' : 'sidebar'}>
      <div className="brand"><img src="/logo.png" alt="Logo" /><div><strong>La Herradura</strong><small>Panel de ventas</small></div></div>
      <nav>{items.map(([icon, name, path]) => <NavLink key={path} to={path} onClick={() => setOpen(false)} className={({ isActive }) => isActive ? 'active' : ''}><span>{icon}</span>{name}</NavLink>)}</nav>
      <a className="catalog-link" href="/catalogo" target="_blank" rel="noreferrer">🌐 Ver tienda pública</a>
      <a className="catalog-link secondary" href="/ubicacion" target="_blank" rel="noreferrer">📍 Ver ubicación pública</a>
      <button className="logout" onClick={logout}>Cerrar sesión</button>
    </aside>
    <main><header className="topbar"><button className="menu-button" onClick={() => setOpen(!open)}>☰</button><div><b>{user?.fullName}</b><small>{user?.role}</small></div></header><section className="content">{children}</section></main>
  </div>;
}
