import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import StorefrontHeader from '../components/storefront/StorefrontHeader';
import StoreLocationSection from '../components/storefront/StoreLocationSection';

export default function StoreLocationPage() {
  const [locations, setLocations] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api('/public/store-locations').then(setLocations).catch(requestError => setError(requestError.message));
  }, []);

  const main = locations?.find(location => location.main) || locations?.[0];
  const business = main ? { businessName: main.name, phone: main.whatsappNumber || main.phone } : null;

  return <div className="storefront-page">
    <StorefrontHeader business={business} />
    <main className="location-public-page">
      <div className="location-public-hero">
        <span>📍 Encuéntranos fácilmente</span>
        <h1>Cómo llegar a Carnicería La Herradura</h1>
        <p>Revisa el mapa, identifica nuestra fachada y abre la ruta desde tu ubicación.</p>
      </div>
      {error && <div className="public-error">No pudimos cargar la ubicación: {error}</div>}
      {!locations && !error && <div className="public-loading slim">Cargando ubicación...</div>}
      {locations && locations.length === 0 && <section className="location-empty"><h2>📍 Ubicación en configuración</h2><p>Pronto publicaremos el mapa y las fotografías del negocio.</p></section>}
      {main && <StoreLocationSection location={main} />}
      {locations?.filter(location => location.id !== main?.id).map(location => <StoreLocationSection key={location.id} location={location} />)}
    </main>
  </div>;
}
