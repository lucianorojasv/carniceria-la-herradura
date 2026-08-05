let runtimeConfigPromise;

function normalizeBase(value) {
  const base = String(value || '/api').trim();
  return (base || '/api').replace(/\/$/, '');
}

async function getApiBase() {
  const buildTimeBase = import.meta.env.VITE_API_URL;
  if (buildTimeBase) return normalizeBase(buildTimeBase);

  if (!runtimeConfigPromise) {
    runtimeConfigPromise = fetch('/runtime-config.json', { cache: 'no-store' })
      .then(response => response.ok ? response.json() : {})
      .then(config => normalizeBase(config.apiUrl))
      .catch(() => '/api');
  }
  return runtimeConfigPromise;
}

export async function api(path, options = {}) {
  const base = await getApiBase();
  const token = localStorage.getItem('token');
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (token) headers.Authorization = `Bearer ${token}`;

  const response = await fetch(base + path, { ...options, headers });
  if (response.status === 204) return null;

  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    if (response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (location.pathname !== '/login') location.href = '/login';
    }
    throw new Error(data.message || 'No se pudo completar la operación');
  }
  return data;
}

export const money = value => new Intl.NumberFormat('es-PE', {
  style: 'currency',
  currency: 'PEN'
}).format(Number(value || 0));
