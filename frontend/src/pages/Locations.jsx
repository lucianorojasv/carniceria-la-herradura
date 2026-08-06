import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { ErrorBox, PageHeader } from '../components/UI';

const emptyLocation = {
  name: 'Carnicería La Herradura', address: '', district: '', province: '', department: '',
  latitude: '', longitude: '', googlePlaceId: '', googleMapsUrl: '', googleMapsEmbedUrl: '',
  phone: '', whatsappNumber: '', referenceText: '', parkingInformation: '', main: true, active: true
};

const imageTypes = [
  ['COVER', 'Portada'], ['FACADE', 'Fachada'], ['INTERIOR', 'Interior'], ['COUNTER', 'Mostrador'],
  ['PRODUCT_DISPLAY', 'Vitrina'], ['PARKING', 'Estacionamiento'], ['REFERENCE', 'Referencia'], ['GALLERY', 'Galería']
];


function extractEmbedUrl(value) {
  const text = String(value || '').trim();
  const match = text.match(/src=["']([^"']+)["']/i);
  return (match ? match[1] : text).replaceAll('&amp;', '&');
}

function normalize(location) {
  return {
    ...emptyLocation,
    ...location,
    latitude: location?.latitude ?? '',
    longitude: location?.longitude ?? ''
  };
}

export default function Locations() {
  const [locations, setLocations] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [form, setForm] = useState(emptyLocation);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadMeta, setUploadMeta] = useState({ imageType: 'FACADE', title: '', altText: '' });

  useEffect(() => { load(); }, []);

  async function load(preferredId) {
    try {
      const result = await api('/store-locations');
      setLocations(result);
      const id = preferredId || selectedId || result[0]?.id || null;
      setSelectedId(id);
      setForm(id ? normalize(result.find(location => location.id === id)) : emptyLocation);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  function select(location) {
    setSelectedId(location.id);
    setForm(normalize(location));
    setMessage('');
    setError('');
  }

  function newLocation() {
    setSelectedId(null);
    setForm({ ...emptyLocation, main: locations.length === 0 });
    setMessage('');
    setError('');
  }

  async function save(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    setMessage('');
    try {
      const payload = {
        ...form,
        latitude: form.latitude === '' ? null : Number(form.latitude),
        longitude: form.longitude === '' ? null : Number(form.longitude)
      };
      const result = await api(selectedId ? `/store-locations/${selectedId}` : '/store-locations', {
        method: selectedId ? 'PUT' : 'POST',
        body: JSON.stringify(payload)
      });
      setSelectedId(result.id);
      setMessage('Local guardado correctamente');
      await load(result.id);
    } catch (saveError) {
      setError(saveError.message);
    } finally {
      setSaving(false);
    }
  }

  async function remove() {
    if (!selectedId || !confirm('¿Desactivar este local?')) return;
    try {
      await api(`/store-locations/${selectedId}`, { method: 'DELETE' });
      setMessage('Local desactivado');
      setSelectedId(null);
      await load();
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function uploadImage(event) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file || !selectedId) return;
    setUploading(true);
    setError('');
    try {
      const data = new FormData();
      data.append('file', file);
      const params = new URLSearchParams({ imageType: uploadMeta.imageType });
      if (uploadMeta.title) params.set('title', uploadMeta.title);
      if (uploadMeta.altText) params.set('altText', uploadMeta.altText);
      await api(`/store-locations/${selectedId}/images?${params}`, { method: 'POST', body: data });
      setUploadMeta({ imageType: 'GALLERY', title: '', altText: '' });
      setMessage('Fotografía agregada');
      await load(selectedId);
    } catch (uploadError) {
      setError(uploadError.message);
    } finally {
      setUploading(false);
    }
  }

  async function updateImage(image, changes) {
    try {
      await api(`/store-locations/${selectedId}/images/${image.id}`, {
        method: 'PATCH',
        body: JSON.stringify({
          title: image.title || '', description: image.description || '', altText: image.altText || '',
          imageType: image.imageType || 'GALLERY', displayOrder: image.displayOrder || 0,
          visible: image.visible !== false, ...changes
        })
      });
      await load(selectedId);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function deleteImage(imageId) {
    if (!confirm('¿Quitar esta fotografía de la galería?')) return;
    try {
      await api(`/store-locations/${selectedId}/images/${imageId}`, { method: 'DELETE' });
      await load(selectedId);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  return <>
    <PageHeader title="Locales y ubicación" subtitle="Mapa, cómo llegar y fotografías reales del negocio" action={<button className="primary" onClick={newLocation}>+ Nuevo local</button>} />
    {error && <ErrorBox message={error} />}
    {message && <div className="success">{message}</div>}

    <div className="locations-admin-layout">
      <aside className="panel location-list-panel">
        <h2>Locales</h2>
        {locations.length === 0 && <p className="muted">Todavía no hay locales registrados.</p>}
        {locations.map(location => <button type="button" key={location.id} className={selectedId === location.id ? 'location-list-item active' : 'location-list-item'} onClick={() => select(location)}>
          <span>{location.main ? '⭐' : '📍'}</span>
          <div><b>{location.name}</b><small>{location.address}</small></div>
          <em>{location.active ? 'Activo' : 'Inactivo'}</em>
        </button>)}
      </aside>

      <div className="locations-admin-content">
        <form className="panel form-grid settings-section" onSubmit={save}>
          <h2 className="full">{selectedId ? 'Editar local' : 'Registrar local'}</h2>
          <label>Nombre del local<input required value={form.name} onChange={event => setForm({ ...form, name: event.target.value })} /></label>
          <label>Teléfono<input value={form.phone || ''} onChange={event => setForm({ ...form, phone: event.target.value })} /></label>
          <label className="full">Dirección completa<input required value={form.address} onChange={event => setForm({ ...form, address: event.target.value })} /></label>
          <label>Distrito<input value={form.district || ''} onChange={event => setForm({ ...form, district: event.target.value })} /></label>
          <label>Provincia<input value={form.province || ''} onChange={event => setForm({ ...form, province: event.target.value })} /></label>
          <label>Departamento<input value={form.department || ''} onChange={event => setForm({ ...form, department: event.target.value })} /></label>
          <label>WhatsApp del local<input value={form.whatsappNumber || ''} onChange={event => setForm({ ...form, whatsappNumber: event.target.value })} /></label>
          <label>Latitud<input type="number" step="0.0000001" value={form.latitude} onChange={event => setForm({ ...form, latitude: event.target.value })} placeholder="-12.046374" /></label>
          <label>Longitud<input type="number" step="0.0000001" value={form.longitude} onChange={event => setForm({ ...form, longitude: event.target.value })} placeholder="-77.042793" /></label>
          <label className="full">Google Place ID<input value={form.googlePlaceId || ''} onChange={event => setForm({ ...form, googlePlaceId: event.target.value })} placeholder="Opcional: identifica exactamente el negocio" /></label>
          <label className="full">Enlace para abrir Google Maps<input type="url" value={form.googleMapsUrl || ''} onChange={event => setForm({ ...form, googleMapsUrl: event.target.value })} placeholder="https://maps.app.goo.gl/..." /></label>
          <label className="full">URL del mapa insertado<input type="url" value={form.googleMapsEmbedUrl || ''} onChange={event => setForm({ ...form, googleMapsEmbedUrl: extractEmbedUrl(event.target.value) })} placeholder="Google Maps → Compartir → Insertar mapa → copia solo el valor src" /></label>
          <label className="full">Referencia<textarea value={form.referenceText || ''} onChange={event => setForm({ ...form, referenceText: event.target.value })} placeholder="Frente al mercado, al costado de..." /></label>
          <label className="full">Estacionamiento<textarea value={form.parkingInformation || ''} onChange={event => setForm({ ...form, parkingInformation: event.target.value })} /></label>
          <label className="check"><input type="checkbox" checked={Boolean(form.main)} onChange={event => setForm({ ...form, main: event.target.checked })} /> Local principal</label>
          <label className="check"><input type="checkbox" checked={Boolean(form.active)} onChange={event => setForm({ ...form, active: event.target.checked })} /> Visible para clientes</label>
          <div className="form-actions full">
            {selectedId && <button type="button" className="danger" onClick={remove}>Desactivar</button>}
            {form.directionsUrl && <a className="ghost button-link" href={form.directionsUrl} target="_blank" rel="noreferrer">Probar ruta</a>}
            <button className="primary" disabled={saving}>{saving ? 'Guardando...' : 'Guardar local'}</button>
          </div>
        </form>

        {selectedId && <section className="panel location-gallery-admin">
          <div className="panel-heading"><div><h2>Fotografías del negocio</h2><p>Sube fachada, interior, mostrador y referencias cercanas.</p></div></div>
          <div className="gallery-upload-row">
            <label>Tipo<select value={uploadMeta.imageType} onChange={event => setUploadMeta({ ...uploadMeta, imageType: event.target.value })}>{imageTypes.map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
            <label>Título<input value={uploadMeta.title} onChange={event => setUploadMeta({ ...uploadMeta, title: event.target.value })} placeholder="Fachada principal" /></label>
            <label>Texto alternativo<input value={uploadMeta.altText} onChange={event => setUploadMeta({ ...uploadMeta, altText: event.target.value })} placeholder="Fachada de Carnicería..." /></label>
            <label className={uploading ? 'upload-button disabled' : 'upload-button'}>{uploading ? 'Subiendo...' : '📷 Subir fotografía'}<input type="file" accept="image/jpeg,image/png,image/webp" onChange={uploadImage} disabled={uploading} /></label>
          </div>

          <div className="admin-business-gallery">
            {(form.images || []).map(image => <article key={image.id}>
              <img src={image.imageUrl} alt={image.altText || image.title || 'Fotografía del negocio'} />
              <div>
                <input defaultValue={image.title || ''} placeholder="Título" onBlur={event => updateImage(image, { title: event.target.value })} />
                <input defaultValue={image.description || ''} placeholder="Descripción breve" onBlur={event => updateImage(image, { description: event.target.value })} />
                <input defaultValue={image.altText || ''} placeholder="Texto alternativo accesible" onBlur={event => updateImage(image, { altText: event.target.value })} />
                <select value={image.imageType || 'GALLERY'} onChange={event => updateImage(image, { imageType: event.target.value })}>{imageTypes.map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select>
                <label>Orden<input type="number" min="0" defaultValue={image.displayOrder || 0} onBlur={event => updateImage(image, { displayOrder: Number(event.target.value) })} /></label>
                <label className="check"><input type="checkbox" checked={image.visible !== false} onChange={event => updateImage(image, { visible: event.target.checked })} /> Visible</label>
                <button type="button" className="danger-link" onClick={() => deleteImage(image.id)}>Eliminar</button>
              </div>
            </article>)}
          </div>
        </section>}
      </div>
    </div>
  </>;
}
