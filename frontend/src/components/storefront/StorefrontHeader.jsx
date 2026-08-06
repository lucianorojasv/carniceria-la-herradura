import React, { useState } from 'react';
import { Link } from 'react-router-dom';

export function normalizePeruPhone(phone) {
  const digits = String(phone || '').replace(/\D/g, '');
  if (!digits) return '';
  return digits.startsWith('51') ? digits : `51${digits}`;
}

export function whatsappUrl(phone, text) {
  const digits = normalizePeruPhone(phone);
  return digits ? `https://wa.me/${digits}?text=${encodeURIComponent(text || '')}` : '';
}

export default function StorefrontHeader({ business, cartCount = 0, onOpenCart }) {
  const [open, setOpen] = useState(false);
  const phone = business?.phone || '';
  const whatsapp = whatsappUrl(phone, 'Hola, deseo realizar un pedido');

  return <header className="storefront-header">
    <div className="storefront-nav-shell">
      <Link className="storefront-brand" to="/catalogo" onClick={() => setOpen(false)}>
        <img src="/logo.png" alt="Logo Carnicería La Herradura" />
        <span><small>Carnicería</small>{business?.businessName || 'La Herradura'}</span>
      </Link>

      <button className="storefront-menu-button" type="button" onClick={() => setOpen(value => !value)} aria-expanded={open} aria-label="Abrir menú">
        ☰
      </button>

      <nav className={open ? 'storefront-nav open' : 'storefront-nav'} aria-label="Navegación principal">
        <Link to="/catalogo" onClick={() => setOpen(false)}>Inicio</Link>
        <a href="/catalogo#catalogo" onClick={() => setOpen(false)}>Catálogo</a>
        <a href="/catalogo#promociones" onClick={() => setOpen(false)}>Promociones</a>
        <Link to="/ubicacion" onClick={() => setOpen(false)}>Cómo llegar</Link>
        <Link to="/pedido" onClick={() => setOpen(false)}>Consultar pedido</Link>
        <a href="/catalogo#contacto" onClick={() => setOpen(false)}>Contacto</a>
      </nav>

      <div className="storefront-actions">
        {onOpenCart && <button type="button" className="storefront-cart-button" onClick={onOpenCart} aria-label={`Abrir carrito con ${cartCount} productos`}>
          🛒 <span>{cartCount}</span>
        </button>}
        {whatsapp && <a className="storefront-whatsapp" href={whatsapp} target="_blank" rel="noreferrer">
          WhatsApp
        </a>}
      </div>
    </div>
  </header>;
}
