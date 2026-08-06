import React, { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { whatsappUrl } from './StorefrontHeader';

function safeEmbed(location) {
  const configured = String(location?.googleMapsEmbedUrl || '').trim();
  if (configured.startsWith('https://www.google.com/maps/embed') || configured.startsWith('https://maps.google.com/maps')) {
    return configured;
  }
  const query = location?.latitude != null && location?.longitude != null
    ? `${location.latitude},${location.longitude}`
    : [location?.address, location?.district, location?.province, location?.department].filter(Boolean).join(', ');
  return query ? `https://maps.google.com/maps?q=${encodeURIComponent(query)}&z=16&output=embed` : '';
}

export default function StoreLocationSection({ location, compact = false }) {
  const [selectedImage, setSelectedImage] = useState(null);
  const mapUrl = useMemo(() => safeEmbed(location), [location]);

  if (!location) return <section className="location-empty">
    <h2>📍 Próximamente publicaremos nuestra ubicación</h2>
    <p>El administrador todavía debe registrar la dirección y las fotografías del negocio.</p>
  </section>;

  const photos = (location.images || []).filter(image => image.visible !== false);
  const visiblePhotos = compact ? photos.slice(0, 4) : photos;
  const phone = location.whatsappNumber || location.phone;

  return <section className={compact ? 'store-location-section compact' : 'store-location-section'}>
    <div className="store-location-heading">
      <div>
        <span className={location.openNow ? 'open-chip' : 'closed-chip'}>
          {location.openNow ? '● Abierto ahora' : '● Fuera de horario'}
        </span>
        <h2>Visítanos en {location.name}</h2>
        <p>{location.address}{location.district ? `, ${location.district}` : ''}</p>
      </div>
      {compact && <Link className="text-link" to="/ubicacion">Ver ubicación completa →</Link>}
    </div>

    <div className="store-location-grid">
      <div className="store-map-card">
        {mapUrl
          ? <iframe title={`Mapa de ${location.name}`} src={mapUrl} loading="lazy" allowFullScreen referrerPolicy="strict-origin-when-cross-origin" />
          : <div className="map-placeholder">🗺️<strong>Mapa por configurar</strong></div>}
        <div className="store-map-actions">
          <a className="primary-location-action" href={location.directionsUrl || location.googleMapsUrl} target="_blank" rel="noreferrer">📍 Cómo llegar</a>
          {phone && <a href={whatsappUrl(phone, 'Hola, necesito información para llegar al local')} target="_blank" rel="noreferrer">💬 Consultar</a>}
        </div>
      </div>

      <div className="store-location-details">
        <div className="location-detail-card"><span>🕒</span><div><b>Horario</b><p>{location.todaySchedule || 'Por configurar'}</p></div></div>
        <div className="location-detail-card"><span>🏪</span><div><b>Referencia</b><p>{location.referenceText || 'Consulta por WhatsApp para una referencia exacta.'}</p></div></div>
        <div className="location-detail-card"><span>🚗</span><div><b>Estacionamiento</b><p>{location.parkingInformation || 'Información por confirmar.'}</p></div></div>
        {location.phone && <div className="location-detail-card"><span>☎️</span><div><b>Contacto</b><p>{location.phone}</p></div></div>}
      </div>
    </div>

    {visiblePhotos.length > 0 && <div className="business-gallery">
      {visiblePhotos.map((image, index) => <button type="button" key={image.id || image.imageUrl} onClick={() => setSelectedImage(image)} className={index === 0 ? 'gallery-main' : ''}>
        <img src={image.imageUrl} alt={image.altText || image.title || `Fotografía de ${location.name}`} />
        {(image.title || image.imageType === 'FACADE') && <span>{image.title || 'Fachada del negocio'}</span>}
      </button>)}
    </div>}

    {selectedImage && <div className="gallery-lightbox" role="dialog" aria-modal="true" onClick={() => setSelectedImage(null)}>
      <button type="button" aria-label="Cerrar" onClick={() => setSelectedImage(null)}>×</button>
      <img src={selectedImage.imageUrl} alt={selectedImage.altText || selectedImage.title || 'Fotografía del negocio'} onClick={event => event.stopPropagation()} />
      {(selectedImage.title || selectedImage.description) && <div onClick={event => event.stopPropagation()}>
        <b>{selectedImage.title}</b><p>{selectedImage.description}</p>
      </div>}
    </div>}
  </section>;
}
