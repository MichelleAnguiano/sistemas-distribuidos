const API = '/api/books';
const $lista = document.getElementById('lista');
const $form = document.getElementById('form');
const $titulo = document.getElementById('titulo');
const $categoria = document.getElementById('categoria');
const $net = document.getElementById('net');

let offline = false;

async function cargar() {
  try {
    const res = await fetch(API, { cache: 'no-store' });
    if (!res.ok) throw new Error('bad');
    const data = await res.json();
    localStorage.setItem('books', JSON.stringify(data));
    pintar(data);
    offline = false;
    $net.textContent = '🟢 conectado';
  } catch (e) {
    const cache = JSON.parse(localStorage.getItem('books') || '[]');
    pintar(cache);
    offline = true;
    $net.textContent = '🔴 sin conexión (solo lectura)';
  }
}

function pintar(data) {
  $lista.innerHTML = '';
  data.forEach(b => {
    const li = document.createElement('li');
    li.innerHTML = `
      <div>
        <strong>#${b.id}</strong> ${escapeHTML(b.titulo)}
        <small style="color:#9aa3c7;margin-left:8px">${escapeHTML(b.categoria || "")}</small>
      </div>
      <button disabled title="Solo lectura">🗑️</button>
    `;
    $lista.appendChild(li);
  });
}

$form.addEventListener('submit', async (e) => {
  e.preventDefault();
  if (offline) { alert('Estás offline: no se puede agregar.'); return; }
  const titulo = $titulo.value.trim();
  const categoria = $categoria.value.trim();
  if (!titulo) return;
  try {
    const res = await fetch(API, {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({ titulo, categoria })
    });
    if (!res.ok) throw 0;
    $titulo.value = '';
    $categoria.value = '';
    await cargar();
  } catch {
    alert('No se pudo agregar el libro.');
  }
});

function escapeHTML(s) {
  return String(s).replace(/[&<>"']/g, c=>({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[c]));
}

cargar();
window.addEventListener('online', cargar);
