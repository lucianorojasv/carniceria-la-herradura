import React, { useEffect, useMemo, useState } from 'react';
import { api, money } from '../services/api';

export default function PublicCatalog() {
  const [data, setData] = useState(null);
  const [category, setCategory] = useState('all');
  const [error, setError] = useState('');

  useEffect(() => {
    api('/public/catalog')
      .then(setData)
      .catch(requestError => setError(requestError.message));
  }, []);

  const products = useMemo(() => {
    if (!data) return [];
    return category === 'all'
      ? data.products
      : data.products.filter(product => String(product.category.id) === category);
  }, [data, category]);

  if (error) return <div className="public-loading">No se pudo cargar el catálogo: {error}</div>;
  if (!data) return <div className="public-loading">Cargando catálogo...</div>;

  const phone = (data.business.phone || '938149352').replace(/\D/g, '');
  const whatsapp = text => `https://wa.me/51${phone.replace(/^51/, '')}?text=${encodeURIComponent(text)}`;

  return <div className="catalog-page">
    <header className="catalog-hero">
      <img className="cover" src="/cover.png" alt="Carnicería La Herradura" />
      <div className="hero-overlay">
        <img src="/logo.png" alt="Logo Carnicería La Herradura" />
        <div>
          <h1>{data.business.businessName}</h1>
          <p>{data.business.welcomeMessage}</p>
          <a href={whatsapp('Hola, deseo realizar un pedido')} target="_blank" rel="noreferrer">
            📲 Pedir por WhatsApp
          </a>
        </div>
      </div>
    </header>

    <section className="catalog-content">
      <div className="category-tabs">
        <button className={category === 'all' ? 'active' : ''} onClick={() => setCategory('all')}>Todos</button>
        {data.categories.map(item => <button
          className={category === String(item.id) ? 'active' : ''}
          onClick={() => setCategory(String(item.id))}
          key={item.id}
        >{item.name}</button>)}
      </div>

      <div className="product-grid">
        {products.map(product => <article className="product-card" key={product.id}>
          <div className="product-image">
            {product.imageUrl
              ? <img src={product.imageUrl} alt={product.name} />
              : <span>🥩</span>}
          </div>
          <small>{product.category.name}</small>
          <h3>{product.name}</h3>
          <p>{product.description}</p>
          <div>
            <b>{money(product.pricePerUnit)} {product.unit === 'KG' ? '/ kg' : ''}</b>
            <a href={whatsapp(`Hola, deseo pedir ${product.name}`)} target="_blank" rel="noreferrer">Pedir</a>
          </div>
        </article>)}
      </div>

      {data.promotions.length > 0 && <>
        <h2 className="section-title">🔥 Promociones y combos</h2>
        <div className="promo-grid catalog-promos">
          {data.promotions.map(promotion => <article className="promo-card" key={promotion.id}>
            <div className="promo-media">
              {promotion.imageUrl
                ? <img src={promotion.imageUrl} alt={promotion.name} />
                : <span>🔥</span>}
            </div>
            <div className="promo-card-body">
              <h3>{promotion.name}</h3>
              <p>{promotion.description}</p>
              <b>{promotion.promotionalPrice
                ? money(promotion.promotionalPrice)
                : 'Consulta el precio'}</b>
              <a
                href={whatsapp(`Hola, deseo la promoción ${promotion.name}`)}
                target="_blank"
                rel="noreferrer"
              >Solicitar por WhatsApp</a>
            </div>
          </article>)}
        </div>
      </>}

      <div className="delivery-info">
        <h2>🛵 Delivery programado</h2>
        <p>Consulta disponibilidad y costo según tu zona. También puedes recoger tu pedido sin esperar.</p>
        {data.deliveryZones.map(zone => <span key={zone.id}>{zone.name}: {money(zone.fee)}</span>)}
      </div>
    </section>

    <a className="floating-wa" href={whatsapp('Hola, deseo hacer un pedido')} target="_blank" rel="noreferrer">
      WhatsApp
    </a>
  </div>;
}
