import React, { useEffect, useState } from 'react';
import { api, money } from '../services/api';
import { PageHeader, Modal, ErrorBox } from '../components/UI';

const blank = {
  name: '',
  description: '',
  promotionalPrice: '',
  startDate: '',
  endDate: '',
  active: true,
  imageUrl: ''
};

export default function Promotions() {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(null);
  const [error, setError] = useState('');
  const [uploading, setUploading] = useState(false);

  const load = () => api('/promotions')
    .then(setItems)
    .catch(loadError => setError(loadError.message));

  useEffect(() => { load(); }, []);

  function edit(promotion) {
    setError('');
    setForm(promotion ? { ...promotion } : { ...blank });
  }

  async function uploadImage(event) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;

    setError('');
    setUploading(true);
    try {
      const data = new FormData();
      data.append('file', file);
      const result = await api('/media/promotion-image', {
        method: 'POST',
        body: data
      });
      setForm(current => ({ ...current, imageUrl: result.url }));
    } catch (uploadError) {
      setError(uploadError.message);
    } finally {
      setUploading(false);
    }
  }

  async function save(event) {
    event.preventDefault();
    setError('');
    try {
      const body = {
        ...form,
        promotionalPrice: form.promotionalPrice
          ? Number(form.promotionalPrice)
          : null
      };
      await api('/promotions' + (form.id ? '/' + form.id : ''), {
        method: form.id ? 'PUT' : 'POST',
        body: JSON.stringify(body)
      });
      setForm(null);
      load();
    } catch (saveError) {
      setError(saveError.message);
    }
  }

  return <>
    <PageHeader
      title="Promociones"
      subtitle="Combos, fotografías y campañas comerciales"
      action={<button className="primary" onClick={() => edit(null)}>+ Nueva promoción</button>}
    />

    <ErrorBox message={error} />

    <div className="promo-grid">
      {items.map(promotion => <article className="promo-card admin-promo-card" key={promotion.id}>
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
            : 'Precio por definir'}</b>
          {(promotion.startDate || promotion.endDate) && <small className="promo-dates">
            {promotion.startDate || 'Sin inicio'} — {promotion.endDate || 'Sin fin'}
          </small>}
          <button className="ghost" onClick={() => edit(promotion)}>Editar</button>
        </div>
      </article>)}
    </div>

    {form && <Modal title={form.id ? 'Editar promoción' : 'Nueva promoción'} onClose={() => setForm(null)}>
      <form className="form-grid" onSubmit={save}>
        <label className="full">Nombre
          <input
            required
            value={form.name}
            onChange={event => setForm({ ...form, name: event.target.value })}
          />
        </label>

        <label className="full">Descripción
          <textarea
            value={form.description || ''}
            onChange={event => setForm({ ...form, description: event.target.value })}
          />
        </label>

        <div className="full product-image-editor">
          <div className="product-image-preview promotion-image-preview">
            {form.imageUrl
              ? <img src={form.imageUrl} alt="Vista previa del combo" />
              : <span>🔥</span>}
          </div>
          <div className="product-image-controls">
            <strong>Imagen del combo o promoción</strong>
            <p>Selecciona una imagen JPG, PNG o WEBP de hasta 4 MB.</p>
            <label className={`upload-button ${uploading ? 'disabled' : ''}`}>
              {uploading ? 'Subiendo imagen…' : 'Seleccionar imagen de mi PC'}
              <input
                type="file"
                accept="image/jpeg,image/png,image/webp"
                disabled={uploading}
                onChange={uploadImage}
              />
            </label>
            <label>O pega una URL de imagen
              <input
                type="url"
                placeholder="https://..."
                value={form.imageUrl || ''}
                onChange={event => setForm({ ...form, imageUrl: event.target.value })}
              />
            </label>
            {form.imageUrl && <button
              type="button"
              className="ghost remove-image"
              onClick={() => setForm({ ...form, imageUrl: '' })}
            >Quitar imagen</button>}
          </div>
        </div>

        <label>Precio promocional
          <input
            type="number"
            step="0.01"
            min="0"
            value={form.promotionalPrice || ''}
            onChange={event => setForm({ ...form, promotionalPrice: event.target.value })}
          />
        </label>
        <label>Fecha inicio
          <input
            type="date"
            value={form.startDate || ''}
            onChange={event => setForm({ ...form, startDate: event.target.value })}
          />
        </label>
        <label>Fecha fin
          <input
            type="date"
            value={form.endDate || ''}
            onChange={event => setForm({ ...form, endDate: event.target.value })}
          />
        </label>
        <label className="check">
          <input
            type="checkbox"
            checked={form.active}
            onChange={event => setForm({ ...form, active: event.target.checked })}
          />
          Activa
        </label>

        <ErrorBox message={error} />
        <div className="form-actions full">
          <button type="button" className="ghost" onClick={() => setForm(null)}>Cancelar</button>
          <button className="primary" disabled={uploading}>
            {form.id ? 'Guardar cambios' : 'Crear promoción'}
          </button>
        </div>
      </form>
    </Modal>}
  </>;
}
