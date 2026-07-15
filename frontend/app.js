const API_BASE_URL = 'http://localhost:8081';

const state = {
  username: '',
  password: ''
};

function setAuthStatus(message, isError = false) {
  const el = document.getElementById('auth-status');
  el.textContent = message;
  el.style.color = isError ? '#b91c1c' : '#475569';
}

function getAuthHeaders(extraHeaders = {}) {
  const headers = { ...extraHeaders };
  if (state.username && state.password) {
    const encoded = btoa(`${state.username}:${state.password}`);
    headers.Authorization = `Basic ${encoded}`;
  }
  return headers;
}

async function checkHealth() {
  const result = document.getElementById('health-result');
  result.textContent = 'Checking...';

  try {
    const response = await fetch(`${API_BASE_URL}/health`);
    const text = await response.text();
    result.textContent = text;
  } catch (error) {
    result.textContent = 'Unable to reach the health endpoint.';
  }
}

async function loadResources() {
  try {
    const response = await fetch(`${API_BASE_URL}/api/resources`, {
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      throw new Error('Authentication required or request failed');
    }

    const resources = await response.json();
    const tbody = document.getElementById('resource-table-body');

    if (!resources.length) {
      tbody.innerHTML = '<tr><td colspan="4">No resources found.</td></tr>';
      return;
    }

    tbody.innerHTML = resources
      .map(
        (resource) => `
          <tr>
            <td>${resource.id ?? '-'}</td>
            <td>${resource.name ?? '-'}</td>
            <td>${resource.type ?? '-'}</td>
            <td>${resource.status ?? '-'}</td>
          </tr>
        `
      )
      .join('');
  } catch (error) {
    const tbody = document.getElementById('resource-table-body');
    tbody.innerHTML = '<tr><td colspan="4">Unable to load resources.</td></tr>';
    setAuthStatus(error.message, true);
  }
}

async function createResource(event) {
  event.preventDefault();
  const messageEl = document.getElementById('resource-message');
  const payload = {
    name: document.getElementById('resource-name').value,
    type: document.getElementById('resource-type').value,
    status: document.getElementById('resource-status').value
  };

  try {
    const response = await fetch(`${API_BASE_URL}/api/resources`, {
      method: 'POST',
      headers: getAuthHeaders({ 'Content-Type': 'application/json' }),
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      throw new Error('Could not create resource. Check your credentials.');
    }

    const created = await response.json();
    messageEl.textContent = `Created resource: ${created.name}`;
    document.getElementById('resource-form').reset();
    await loadResources();
  } catch (error) {
    messageEl.textContent = error.message;
    messageEl.style.color = '#b91c1c';
  }
}

document.getElementById('auth-form').addEventListener('submit', (event) => {
  event.preventDefault();
  state.username = document.getElementById('username').value.trim();
  state.password = document.getElementById('password').value;
  setAuthStatus(`Connected as ${state.username || 'user'}.`);
  loadResources();
});

document.getElementById('health-btn').addEventListener('click', checkHealth);
document.getElementById('refresh-btn').addEventListener('click', loadResources);
document.getElementById('resource-form').addEventListener('submit', createResource);

checkHealth();
loadResources();
