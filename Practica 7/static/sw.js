const CACHE = 'p7-books-v1';
const PRECACHE = [
  '/', '/index.html', '/style.css', '/app.js',
  '/manifest.json', '/icons/icon-192.png', '/icons/icon-512.png'
];

// install: precache
self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(PRECACHE)));
  self.skipWaiting();
});

// activate: limpiar caches viejos
self.addEventListener('activate', (e) => {
  e.waitUntil(
    caches.keys().then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k))))
  );
  self.clients.claim();
});

// fetch: cache-first para estáticos; dejar pasar API
self.addEventListener('fetch', (e) => {
  const req = e.request;
  const url = new URL(req.url);

  // No interceptar llamadas a /api ni métodos distintos de GET
  if (url.pathname.startsWith('/api/') || req.method !== 'GET') return;

  e.respondWith(
    caches.match(req).then(cached => {
      if (cached) return cached;
      return fetch(req).then(resp => {
        const clone = resp.clone();
        caches.open(CACHE).then(c => c.put(req, clone));
        return resp;
      }).catch(() => {
        if (req.mode === 'navigate') return caches.match('/index.html');
      });
    })
  );
});
