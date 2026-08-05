import React, { useEffect, useState } from 'react';
import { api, money } from '../services/api';
import { PageHeader, Modal, ErrorBox } from '../components/UI';

const empty = {
  categoryId: '',
  name: '',
  description: '',
  pricePerUnit: '',
  unit: 'KG',
  stockQuantity: '',
  minimumQuantity: '0.5',
  imageUrl: '',
  active: true,
  featured: false
};

export default function Products() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState(null);
  const [error, setError] = useState('');
  const [uploading, setUploading] = useState(false);

  const load = () => Promise.all([api('/products'), api('/categories')])
    .then(([productList, categoryList]) => {
      setProducts(productList);
      setCategories(categoryList);
    });

  useEffect(() => { load(); }, []);

  function edit(product) {
    setError('');
    setForm(product
      ? { ...product, categoryId: product.category.id }
      : { ...empty, categoryId: categories[0]?.id || '' });
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
      const result = await api('/media/product-image', {
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
        categoryId: Number(form.categoryId),
        pricePerUnit: Number(form.pricePerUnit),
        stockQuantity: Number(form.stockQuantity),
        minimumQuantity: Number(form.minimumQuantity)
      };
      await api('/products' + (form.id ? '/' + form.id : ''), {
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
      title="Productos"
      subtitle="Precios, stock, fotografías y catálogo"
      action={<button className="primary" onClick={() => edit(null)}>+ Nuevo producto</button>}
    />

    <div className="panel">
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Imagen</th>
              <th>Producto</th>
              <th>Categoría</th>
              <th>Precio</th>
              <th>Stock</th>
              <th>Destacado</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {products.map(product => <tr key={product.id}>
              <td>
                <div className="product-admin-thumb">
                  {product.imageUrl
                    ? <img src={product.imageUrl} alt={product.name} />
                    : <span>🥩</span>}
                </div>
              </td>
              <td><b>{product.name}</b><small>{product.description}</small></td>
              <td>{product.category?.name}</td>
              <td>{money(product.pricePerUnit)} / {product.unit === 'KG' ? 'kg' : product.unit}</td>
              <td>{product.stockQuantity}</td>
              <td>{product.featured ? '⭐' : '—'}</td>
              <td><button className="ghost" onClick={() => edit(product)}>Editar</button></td>
            </tr>)}
          </tbody>
        </table>
      </div>
    </div>

    {form && <Modal title={form.id ? 'Editar producto' : 'Nuevo producto'} onClose={() => setForm(null)}>
      <form className="form-grid" onSubmit={save}>
        <label>Categoría
          <select value={form.categoryId} onChange={event => setForm({ ...form, categoryId: event.target.value })}>
            {categories.map(category => <option key={category.id} value={category.id}>{category.name}</option>)}
          </select>
        </label>
        <label>Nombre
          <input required value={form.name} onChange={event => setForm({ ...form, name: event.target.value })} />
        </label>
        <label className="full">Descripción
          <textarea value={form.description || ''} onChange={event => setForm({ ...form, description: event.target.value })} />
        </label>

        <div className="full product-image-editor">
          <div className="product-image-preview">
            {form.imageUrl
              ? <img src={form.imageUrl} alt="Vista previa del producto" />
              : <span>🥩</span>}
          </div>
          <div className="product-image-controls">
            <strong>Fotografía del producto</strong>
            <p>Usa una imagen horizontal o cuadrada en JPG, PNG o WEBP, de hasta 4 MB.</p>
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

        <label>Precio
          <input type="number" step="0.01" required value={form.pricePerUnit} onChange={event => setForm({ ...form, pricePerUnit: event.target.value })} />
        </label>
        <label>Stock
          <input type="number" step="0.001" required value={form.stockQuantity} onChange={event => setForm({ ...form, stockQuantity: event.target.value })} />
        </label>
        <label>Cantidad mínima
          <input type="number" step="0.001" value={form.minimumQuantity} onChange={event => setForm({ ...form, minimumQuantity: event.target.value })} />
        </label>
        <label>Unidad
          <select value={form.unit} onChange={event => setForm({ ...form, unit: event.target.value })}>
            <option>KG</option><option>UNIT</option><option>PACK</option>
          </select>
        </label>
        <label className="check"><input type="checkbox" checked={form.active} onChange={event => setForm({ ...form, active: event.target.checked })} />Activo</label>
        <label className="check"><input type="checkbox" checked={form.featured} onChange={event => setForm({ ...form, featured: event.target.checked })} />Destacado</label>
        <ErrorBox message={error} />
        <div className="form-actions full">
          <button type="button" className="ghost" onClick={() => setForm(null)}>Cancelar</button>
          <button className="primary" disabled={uploading}>{form.id ? 'Guardar cambios' : 'Crear producto'}</button>
        </div>
      </form>
    </Modal>}
  </>;
}
