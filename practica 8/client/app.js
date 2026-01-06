// Ajusta si cambias puertos
const URL_USUARIOS = 'http://127.0.0.1:9001/usuarios';
const URL_CLIMA    = 'http://127.0.0.1:9002/clima/';

const btnListar = document.getElementById('btnListar');
const selectUsuario = document.getElementById('selectUsuario');
const card = document.getElementById('resultsCard');

const spanNombre = document.getElementById('nombre_usuario');
const spanCiudad = document.getElementById('ciudad');
const spanTemp   = document.getElementById('temperatura');
const spanCond   = document.getElementById('condicion');
const errorBox   = document.getElementById('error');

btnListar.addEventListener('click', async () => {
  try {
    const res = await fetch(URL_USUARIOS, { cache: 'no-store' });
    if (!res.ok) throw new Error('Error listando usuarios');
    const usuarios = await res.json(); // { "1": {...}, "2": {...} }
    selectUsuario.innerHTML = '<option value="">2. Selecciona un usuario</option>';
    Object.keys(usuarios).forEach(id => {
      const u = usuarios[id];
      const opt = document.createElement('option');
      opt.value = id;
      opt.textContent = `#${id} — ${u.nombre} (${u.ciudad})`;
      selectUsuario.appendChild(opt);
    });
    selectUsuario.disabled = false;
  } catch (err) {
    alert(err.message);
  }
});

selectUsuario.addEventListener('change', async (ev) => {
  const id = ev.target.value;
  if (!id) { card.style.display = 'none'; return; }
  errorBox.textContent = '';
  try {
    // 1) detalle del usuario
    const ru = await fetch(`${URL_USUARIOS}/${id}`);
    if (!ru.ok) throw new Error('Usuario no encontrado');
    const user = await ru.json();

    spanNombre.textContent = user.nombre;
    spanCiudad.textContent = user.ciudad;

    // 2) clima por ciudad del usuario
    const rc = await fetch(`${URL_CLIMA}${encodeURIComponent(user.ciudad)}`);
    if (!rc.ok) throw new Error('Clima no disponible');
    const clima = await rc.json();

    spanTemp.textContent = clima.temperatura;
    spanCond.textContent = clima.condicion;

    card.style.display = 'block';
  } catch (e) {
    errorBox.textContent = 'Error: ' + e.message;
    card.style.display = 'block';
  }
});
