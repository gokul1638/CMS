/* ----------------------------------------------------
   CMS - Enterprise Application Script
---------------------------------------------------- */

const API_BASE_URL = 'http://localhost:8081';

const state = {
  token: localStorage.getItem('jwt_token') || '',
  user: JSON.parse(localStorage.getItem('user_details')) || null,
  currentView: 'dashboard',
  activeChart: null,
  tariffRates: {
    CPU: 400.0,
    RAM: 120.0,
    Storage: 10.0,
    GPU: 7000.0,
    Bandwidth: 5.0
  }
};

// UI Elements routing
const views = [
  'dashboard', 'vms', 'catalog', 'orders', 'invoices', 
  'users', 'tariffs', 'reports', 'logs'
];

/* --- API Request Wrapper --- */
async function apiRequest(endpoint, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  };

  if (state.token) {
    headers['Authorization'] = `Bearer ${state.token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers
  });

  if (response.status === 401) {
    // Session Expired
    logout();
    throw new Error('Session expired. Please sign in again.');
  }

  if (!response.ok) {
    const errData = await response.json().catch(() => ({}));
    throw new Error(errData.message || `Request failed with code ${response.status}`);
  }

  // Handle file downloads
  const contentType = response.headers.get('Content-Type');
  if (contentType && contentType.includes('text/csv')) {
    return response.text();
  }

  return response.json().catch(() => ({}));
}

/* --- View Management Router --- */
function showView(viewName) {
  state.currentView = viewName;
  
  // Hide all screens
  views.forEach(v => {
    const el = document.getElementById(`view-${v}`);
    if (el) el.style.display = 'none';
  });

  // Highlight sidebar item
  document.querySelectorAll('.nav-item').forEach(item => {
    item.classList.remove('active');
    if (item.getAttribute('data-view') === viewName) {
      item.classList.add('active');
    }
  });

  // Show selected screen
  const targetView = document.getElementById(`view-${viewName}`);
  if (targetView) {
    targetView.style.display = 'block';
  }

  // Update Topbar title
  const viewTitles = {
    dashboard: 'System Overview Dashboard',
    vms: 'Compute Infrastructure (VMs)',
    catalog: 'Cloud Resource Catalog',
    orders: 'Resource Provisioning Orders',
    invoices: 'Billing, Invoices & Receipting',
    users: 'Cloud User Accounts Console',
    tariffs: 'Pricing Tariffs configuration',
    reports: 'Operational & Financial Reports',
    logs: 'Security Audit Logs'
  };
  document.getElementById('view-title').textContent = viewTitles[viewName] || 'Overview';

  // Load screen data
  triggerViewLoad(viewName);
}

function triggerViewLoad(viewName) {
  switch (viewName) {
    case 'dashboard':
      loadDashboard();
      break;
    case 'vms':
      loadVMs();
      break;
    case 'catalog':
      loadCatalog();
      break;
    case 'orders':
      loadOrders();
      break;
    case 'invoices':
      loadInvoices();
      break;
    case 'users':
      loadUsers();
      break;
    case 'tariffs':
      loadTariffs();
      break;
    case 'reports':
      loadReports();
      break;
    case 'logs':
      loadAuditLogs();
      break;
  }
}

/* --- Authentication Actions --- */
function setupAuthListeners() {
  // Navigation links
  document.getElementById('to-register').addEventListener('click', (e) => {
    e.preventDefault();
    toggleAuthForms('register');
  });
  document.getElementById('to-login-from-reg').addEventListener('click', (e) => {
    e.preventDefault();
    toggleAuthForms('login');
  });
  document.getElementById('to-forgot').addEventListener('click', (e) => {
    e.preventDefault();
    toggleAuthForms('forgot');
  });
  document.getElementById('to-login-from-forgot').addEventListener('click', (e) => {
    e.preventDefault();
    toggleAuthForms('login');
  });

  // Login submission
  document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value;
    const statusMsg = document.getElementById('auth-status-msg');

    statusMsg.style.color = '#3b82f6';
    statusMsg.textContent = 'Authenticating credentials...';

    try {
      const data = await apiRequest('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password })
      });

      state.token = data.token;
      state.user = {
        username: data.username,
        role: data.role,
        email: data.email,
        subscriptionPlan: data.subscriptionPlan
      };

      localStorage.setItem('jwt_token', data.token);
      localStorage.setItem('user_details', JSON.stringify(state.user));

      statusMsg.style.color = '#22c55e';
      statusMsg.textContent = 'Authentication successful! Loading dashboard...';

      setTimeout(() => {
        setupPortalAccess();
      }, 1000);

    } catch (err) {
      statusMsg.style.color = '#ef4444';
      statusMsg.textContent = err.message;
    }
  });

  // Register submission
  document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('reg-username').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;
    const statusMsg = document.getElementById('auth-status-msg');

    statusMsg.style.color = '#3b82f6';
    statusMsg.textContent = 'Creating account...';

    try {
      await apiRequest('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({ username, email, password, role: 'CUSTOMER' })
      });

      statusMsg.style.color = '#22c55e';
      statusMsg.textContent = 'Account created successfully! Redirecting to login...';

      setTimeout(() => {
        toggleAuthForms('login');
      }, 1500);

    } catch (err) {
      statusMsg.style.color = '#ef4444';
      statusMsg.textContent = err.message;
    }
  });

  // Forgot password
  document.getElementById('forgot-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('forgot-username').value.trim();
    const statusMsg = document.getElementById('auth-status-msg');

    statusMsg.style.color = '#3b82f6';
    statusMsg.textContent = 'Querying username record...';

    try {
      const data = await apiRequest('/api/auth/forgot-password', {
        method: 'POST',
        body: JSON.stringify({ username })
      });

      statusMsg.style.color = '#22c55e';
      statusMsg.textContent = 'Reset code generated successfully.';

      // Pre-fill reset password form with generated mock code
      document.getElementById('reset-username').value = username;
      document.getElementById('reset-code').value = data.resetCode;

      setTimeout(() => {
        toggleAuthForms('reset');
      }, 1500);

    } catch (err) {
      statusMsg.style.color = '#ef4444';
      statusMsg.textContent = err.message;
    }
  });

  // Reset password
  document.getElementById('reset-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('reset-username').value;
    const code = document.getElementById('reset-code').value.trim();
    const newPassword = document.getElementById('reset-password').value;
    const statusMsg = document.getElementById('auth-status-msg');

    statusMsg.style.color = '#3b82f6';
    statusMsg.textContent = 'Updating credential password...';

    try {
      await apiRequest('/api/auth/reset-password', {
        method: 'POST',
        body: JSON.stringify({ username, code, newPassword })
      });

      statusMsg.style.color = '#22c55e';
      statusMsg.textContent = 'Password reset complete! Redirecting...';

      setTimeout(() => {
        toggleAuthForms('login');
      }, 1500);

    } catch (err) {
      statusMsg.style.color = '#ef4444';
      statusMsg.textContent = err.message;
    }
  });

  // Sign out button
  document.getElementById('logout-btn').addEventListener('click', logout);
}

function toggleAuthForms(view) {
  const login = document.getElementById('login-form');
  const reg = document.getElementById('register-form');
  const forgot = document.getElementById('forgot-form');
  const reset = document.getElementById('reset-form');
  const title = document.getElementById('auth-title');
  const subtitle = document.getElementById('auth-subtitle');
  const statusMsg = document.getElementById('auth-status-msg');

  statusMsg.textContent = '';
  login.style.display = 'none';
  reg.style.display = 'none';
  forgot.style.display = 'none';
  reset.style.display = 'none';

  if (view === 'login') {
    login.style.display = 'block';
    title.textContent = 'Welcome Back';
    subtitle.textContent = 'Login to manage your provisioned cloud resources';
  } else if (view === 'register') {
    reg.style.display = 'block';
    title.textContent = 'Create Profile';
    subtitle.textContent = 'Sign up to configure custom cloud environments';
  } else if (view === 'forgot') {
    forgot.style.display = 'block';
    title.textContent = 'Forgot Password';
    subtitle.textContent = 'Enter username to request standard code verification';
  } else if (view === 'reset') {
    reset.style.display = 'block';
    title.textContent = 'Confirm Reset';
    subtitle.textContent = 'Set new secure login credentials for profile access';
  }
}

function setupPortalAccess() {
  document.getElementById('auth-view').style.display = 'none';
  document.getElementById('portal-view').style.display = 'flex';

  // Set user details in sidebar
  document.getElementById('nav-user-name').textContent = state.user.username;
  const roleName = state.user.role.replace('ROLE_', '').replace('_', ' ').toLowerCase();
  const roleBadge = document.getElementById('nav-user-role');
  roleBadge.textContent = roleName;

  // Filter sidebar options based on role authorization
  const isAdmin = state.user.role === 'ROLE_ADMIN' || state.user.role === 'ROLE_SUPER_ADMIN';
  const isStaff = isAdmin || state.user.role === 'ROLE_SUPPORT' || state.user.role === 'ROLE_BILLING_ADMIN';

  document.querySelectorAll('.admin-only').forEach(el => {
    el.style.display = isAdmin ? 'flex' : 'none';
  });

  document.querySelectorAll('.staff-only').forEach(el => {
    el.style.display = isStaff ? 'flex' : 'none';
  });

  // Clean initial notifications triggers
  loadNotifications();

  // Navigate to Dashboard
  showView('dashboard');
}

function logout() {
  state.token = '';
  state.user = null;
  localStorage.removeItem('jwt_token');
  localStorage.removeItem('user_details');

  document.getElementById('portal-view').style.display = 'none';
  document.getElementById('auth-view').style.display = 'flex';
  toggleAuthForms('login');
}

/* --- Dashboard Loading --- */
async function loadDashboard() {
  try {
    const data = await apiRequest('/api/dashboard');
    const isAdmin = state.user.role === 'ROLE_ADMIN' || state.user.role === 'ROLE_SUPER_ADMIN' || state.user.role === 'ROLE_SUPPORT';

    if (isAdmin) {
      document.getElementById('cust-kpis').style.display = 'none';
      document.getElementById('admin-kpis').style.display = 'flex';
      
      document.getElementById('kpi-admin-users').textContent = data.totalUsers;
      document.getElementById('kpi-admin-vms').textContent = data.totalVMs;
      document.getElementById('kpi-admin-revenue').textContent = `₹${data.monthlyRevenue.toLocaleString('en-IN')}`;
      document.getElementById('kpi-admin-approvals').textContent = data.pendingApprovals;

      // Draw Admin Revenue Trend Chart
      drawChart('monthlyRevenue', data.revenueChart.labels, data.revenueChart.values, 'Total Monthly Revenue (₹)');

      // Draw secondary block: Inventory Meters
      renderInventoryMeters(data.inventory);

    } else {
      document.getElementById('admin-kpis').style.display = 'none';
      document.getElementById('cust-kpis').style.display = 'flex';

      document.getElementById('kpi-running-vms').textContent = data.runningVMs;
      document.getElementById('kpi-total-cpu').textContent = data.totalCpu;
      document.getElementById('kpi-total-storage').textContent = data.totalStorage;
      document.getElementById('kpi-pending-bill').textContent = `₹${data.pendingBill.toLocaleString('en-IN')}`;

      // Draw Customer usage graph
      drawChart('telemetry', ['Session 1', 'Session 2', 'Session 3', 'Session 4', 'Session 5', 'Session 6', 'Session 7'], 
                data.cpuUsageChart, 'CPU Core Load Average (%)', data.memoryUsageChart, 'Memory Allocation (%)');

      // Draw secondary block: Customer account overview
      renderCustomerOverview(data);
    }
  } catch (err) {
    console.error('Failed to load dashboard data:', err);
  }
}

function drawChart(type, labels, dataset1, label1, dataset2 = null, label2 = '') {
  const ctx = document.getElementById('dashboardChart').getContext('2d');
  
  if (state.activeChart) {
    state.activeChart.destroy();
  }

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        labels: { color: '#94a3b8', font: { family: 'Outfit' } }
      }
    },
    scales: {
      x: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#94a3b8' } },
      y: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#94a3b8' } }
    }
  };

  const chartDatasets = [{
    label: label1,
    data: dataset1,
    borderColor: '#3b82f6',
    backgroundColor: 'rgba(59, 130, 246, 0.1)',
    fill: true,
    tension: 0.4
  }];

  if (dataset2) {
    chartDatasets.push({
      label: label2,
      data: dataset2,
      borderColor: '#a78bfa',
      backgroundColor: 'rgba(167, 139, 250, 0.1)',
      fill: true,
      tension: 0.4
    });
  }

  state.activeChart = new Chart(ctx, {
    type: 'line',
    data: { labels, datasets: chartDatasets },
    options: chartOptions
  });

  document.getElementById('chart-section-title').textContent = type === 'telemetry' ? 'Telemetry Usage Tracker' : 'Paid Revenue Growth Tracking';
}

function renderInventoryMeters(inventory) {
  const container = document.getElementById('dashboard-secondary-card');
  container.querySelector('h3').textContent = 'Infrastructure Capacity utilization';
  
  const content = document.getElementById('dashboard-secondary-content');
  content.innerHTML = '';

  inventory.forEach(inv => {
    const percentage = Math.min(100, Math.round((inv.allocatedQuantity / inv.totalQuantity) * 100));
    let colorClass = '';
    if (percentage > 85) colorClass = 'danger';
    else if (percentage > 60) colorClass = 'warning';

    const row = document.createElement('div');
    row.className = 'meter-row';
    row.innerHTML = `
      <div class="meter-labels">
        <strong>${inv.resourceType}</strong>
        <span>${inv.allocatedQuantity} / ${inv.totalQuantity} allocated (${percentage}%)</span>
      </div>
      <div class="meter-track">
        <div class="meter-fill ${colorClass}" style="width: ${percentage}%"></div>
      </div>
    `;
    content.appendChild(row);
  });
}

function renderCustomerOverview(dashboardData) {
  const container = document.getElementById('dashboard-secondary-card');
  container.querySelector('h3').textContent = 'Provisioning profile overview';

  const content = document.getElementById('dashboard-secondary-content');
  content.innerHTML = `
    <div style="display: flex; flex-direction: column; gap: 1rem; padding: 0.5rem 0;">
      <div style="display: flex; justify-content: space-between; border-bottom: 1px solid var(--border-glass); padding-bottom: 0.5rem;">
        <span style="color: var(--text-secondary);">Subscription Tier</span>
        <strong style="color: var(--accent-purple);">${dashboardData.subscriptionPlan || 'None'} Plan</strong>
      </div>
      <div style="display: flex; justify-content: space-between; border-bottom: 1px solid var(--border-glass); padding-bottom: 0.5rem;">
        <span style="color: var(--text-secondary);">Deployment Region</span>
        <strong>US East, US West, Mumbai</strong>
      </div>
      <div style="display: flex; justify-content: space-between; border-bottom: 1px solid var(--border-glass); padding-bottom: 0.5rem;">
        <span style="color: var(--text-secondary);">Support SLA status</span>
        <strong style="color: var(--accent-green);">Standard Response</strong>
      </div>
      <div style="display: flex; justify-content: space-between;">
        <span style="color: var(--text-secondary);">Authentication Status</span>
        <span class="badge badge-success">JWT Encrypted</span>
      </div>
    </div>
  `;
}

/* --- Virtual Machines Life Cycle --- */
async function loadVMs() {
  try {
    const vms = await apiRequest('/api/vms');
    const tbody = document.getElementById('vms-table-body');
    tbody.innerHTML = '';

    if (vms.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="text-center">No provisioned Virtual Machines found.</td></tr>';
      return;
    }

    vms.forEach(vm => {
      let statusClass = 'badge-info';
      if (vm.status === 'Running') statusClass = 'badge-success';
      else if (vm.status === 'Stopped') statusClass = 'badge-danger';
      else if (vm.status === 'Restarting' || vm.status === 'Provisioning' || vm.status === 'Pending Approval') statusClass = 'badge-warning';

      const specsText = `${vm.cpuCores} vCPU / ${vm.ramGb} GB RAM / ${vm.storageGb} GB SSD`;
      const row = document.createElement('tr');
      row.innerHTML = `
        <td><strong>${vm.name}</strong></td>
        <td><span style="font-family: monospace;">${vm.region}</span></td>
        <td>${specsText}</td>
        <td>${vm.operatingSystem}</td>
        <td><span class="badge ${statusClass}">${vm.status}</span></td>
        <td>
          <div style="display: flex; gap: 0.25rem;">
            ${vm.status === 'Stopped' ? `<button onclick="controlVM(${vm.id}, 'start')" class="icon-btn" title="Start VM"><i data-lucide="play"></i></button>` : ''}
            ${vm.status === 'Running' ? `<button onclick="controlVM(${vm.id}, 'stop')" class="icon-btn" title="Stop VM"><i data-lucide="square"></i></button>` : ''}
            ${vm.status === 'Running' ? `<button onclick="controlVM(${vm.id}, 'restart')" class="icon-btn" title="Restart VM"><i data-lucide="refresh-cw"></i></button>` : ''}
            <button onclick="openUpgradeModal(${vm.id}, ${vm.cpuCores}, ${vm.ramGb}, ${vm.storageGb}, ${vm.gpuCards || 0}, ${vm.bandwidthMbps || 100})" class="icon-btn" title="Upgrade Specs"><i data-lucide="arrow-up-circle"></i></button>
            <button onclick="terminateVM(${vm.id})" class="icon-btn" style="color: var(--accent-red);" title="Terminate Instance"><i data-lucide="trash-2"></i></button>
          </div>
        </td>
      `;
      tbody.appendChild(row);
    });

    lucide.createIcons();
  } catch (err) {
    console.error('Failed to load virtual machines:', err);
  }
}

async function controlVM(id, action) {
  try {
    await apiRequest(`/api/vms/${id}/${action}`, { method: 'POST' });
    loadVMs();
    loadNotifications();
  } catch (err) {
    alert(`Could not perform action: ${err.message}`);
  }
}

async function terminateVM(id) {
  if (!confirm('Are you sure you want to terminate this instance? This will release all allocated resources immediately.')) {
    return;
  }
  try {
    const data = await apiRequest(`/api/vms/${id}`, { method: 'DELETE' });
    alert(data.message);
    loadVMs();
    loadNotifications();
  } catch (err) {
    alert(`Termination failed: ${err.message}`);
  }
}

function openUpgradeModal(id, cpu, ram, storage, gpu, bandwidth) {
  document.getElementById('upgrade-vm-id').value = id;
  
  // Set current slider values
  document.getElementById('up-cpu').value = cpu;
  document.getElementById('up-cpu-val').textContent = cpu;
  
  document.getElementById('up-ram').value = ram;
  document.getElementById('up-ram-val').textContent = ram;
  
  document.getElementById('up-storage').value = storage;
  document.getElementById('up-storage-val').textContent = storage;

  document.getElementById('up-gpu').value = gpu;
  document.getElementById('up-bandwidth').value = bandwidth;

  document.getElementById('vm-upgrade-modal').style.display = 'flex';
  lucide.createIcons();
}

/* --- Create VM Order Wizard Flow --- */
function setupWizardFlow() {
  const modal = document.getElementById('order-wizard-modal');
  const openBtn = document.getElementById('open-order-wizard-btn');
  const closeBtns = document.querySelectorAll('.close-modal-btn');
  
  // Custom slider panels
  const customPanel = document.getElementById('custom-sizing-panel');
  const planCards = document.querySelectorAll('.plan-select-card');
  let selectedPlan = 'Basic';

  openBtn.addEventListener('click', () => {
    modal.style.display = 'flex';
    calculateWizardPrice();
    lucide.createIcons();
  });

  closeBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      modal.style.display = 'none';
      document.getElementById('vm-upgrade-modal').style.display = 'none';
      document.getElementById('payment-modal').style.display = 'none';
    });
  });

  planCards.forEach(card => {
    card.addEventListener('click', () => {
      planCards.forEach(c => c.classList.remove('active'));
      card.classList.add('active');
      selectedPlan = card.getAttribute('data-plan');

      if (selectedPlan === 'Custom') {
        customPanel.style.display = 'block';
      } else {
        customPanel.style.display = 'none';
      }
      calculateWizardPrice();
    });
  });

  // Slider change trackers
  const sliders = ['wiz-cpu', 'wiz-ram', 'wiz-storage', 'up-cpu', 'up-ram', 'up-storage'];
  sliders.forEach(sliderId => {
    const slider = document.getElementById(sliderId);
    const valueDisp = document.getElementById(`${sliderId}-val`);
    if (slider && valueDisp) {
      slider.addEventListener('input', () => {
        valueDisp.textContent = slider.value;
        if (sliderId.startsWith('wiz')) {
          calculateWizardPrice();
        }
      });
    }
  });

  // GPU and Bandwidth calculations trigger
  document.getElementById('wiz-gpu').addEventListener('input', calculateWizardPrice);
  document.getElementById('wiz-bandwidth').addEventListener('input', calculateWizardPrice);

  // Form submit order
  document.getElementById('order-wizard-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const vmName = document.getElementById('wiz-vm-name').value.trim();
    const operatingSystem = document.getElementById('wiz-vm-os').value;
    const region = document.getElementById('wiz-vm-region').value;

    let cpu = 2, ram = 4, storage = 50, gpu = 0, bandwidth = 100;

    if (selectedPlan === 'Standard') {
      cpu = 4; ram = 8; storage = 100;
    } else if (selectedPlan === 'Premium') {
      cpu = 8; ram = 16; storage = 250;
    } else if (selectedPlan === 'Custom') {
      cpu = parseInt(document.getElementById('wiz-cpu').value);
      ram = parseInt(document.getElementById('wiz-ram').value);
      storage = parseInt(document.getElementById('wiz-storage').value);
      gpu = parseInt(document.getElementById('wiz-gpu').value) || 0;
      bandwidth = parseInt(document.getElementById('wiz-bandwidth').value) || 100;
    }

    try {
      // Create user details updates to track subscription plan
      await apiRequest(`/api/users/me`, {
        method: 'PUT',
        body: JSON.stringify({ subscriptionPlan: selectedPlan })
      });
      // Update plan in local state
      state.user.subscriptionPlan = selectedPlan;
      localStorage.setItem('user_details', JSON.stringify(state.user));

      // Submit provisioning order request
      const payload = {
        vmName, operatingSystem, region,
        cpuCores: cpu, ramGb: ram, storageGb: storage,
        gpuCards: gpu, bandwidthMbps: bandwidth
      };

      await apiRequest('/api/orders', {
        method: 'POST',
        body: JSON.stringify(payload)
      });

      alert('Provisioning requisition submitted successfully. Pending administrative validation approval.');
      modal.style.display = 'none';
      showView('orders');
    } catch (err) {
      alert(`Provisioning request rejected: ${err.message}`);
    }
  });

  // Upgrade form submission
  document.getElementById('vm-upgrade-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('upgrade-vm-id').value;
    const cpu = parseInt(document.getElementById('up-cpu').value);
    const ram = parseInt(document.getElementById('up-ram').value);
    const storage = parseInt(document.getElementById('up-storage').value);
    const gpu = parseInt(document.getElementById('up-gpu').value) || 0;
    const bandwidth = parseInt(document.getElementById('up-bandwidth').value) || 100;

    try {
      await apiRequest(`/api/vms/${id}/upgrade`, {
        method: 'PUT',
        body: JSON.stringify({ cpuCores: cpu, ramGb: ram, storageGb: storage, gpuCards: gpu, bandwidthMbps: bandwidth })
      });
      alert('Virtual Machine hardware scaling successful!');
      document.getElementById('vm-upgrade-modal').style.display = 'none';
      loadVMs();
      loadNotifications();
    } catch (err) {
      alert(`Upgrade failed: ${err.message}`);
    }
  });
}

function calculateWizardPrice() {
  const plan = document.querySelector('.plan-select-card.active').getAttribute('data-plan');
  let cpu = 2, ram = 4, storage = 50, gpu = 0, bandwidth = 100;

  if (plan === 'Standard') {
    cpu = 4; ram = 8; storage = 100;
  } else if (plan === 'Premium') {
    cpu = 8; ram = 16; storage = 250;
  } else if (plan === 'Custom') {
    cpu = parseInt(document.getElementById('wiz-cpu').value);
    ram = parseInt(document.getElementById('wiz-ram').value);
    storage = parseInt(document.getElementById('wiz-storage').value);
    gpu = parseInt(document.getElementById('wiz-gpu').value) || 0;
    bandwidth = parseInt(document.getElementById('wiz-bandwidth').value) || 100;
  }

  // Cost calculations base active tariffs
  const cpuPrice = state.tariffRates.CPU;
  const ramPrice = state.tariffRates.RAM;
  const storagePrice = state.tariffRates.Storage;
  const gpuPrice = state.tariffRates.GPU;
  const bandwidthPrice = state.tariffRates.Bandwidth;

  const rawSubtotal = (cpu * cpuPrice) + (ram * ramPrice) + (storage * storagePrice) + (gpu * gpuPrice) + (bandwidth * bandwidthPrice);
  const gst = rawSubtotal * 0.18;
  const discount = plan !== 'Custom' && plan !== 'None' ? rawSubtotal * 0.10 : 0.0;
  const monthlyTotal = rawSubtotal + gst - discount;

  document.getElementById('wiz-estimated-cost').textContent = `₹${monthlyTotal.toLocaleString('en-IN', { maximumFractionDigits: 2 })} / month`;
}

/* --- Resource Catalog --- */
async function loadCatalog() {
  try {
    const resources = await apiRequest('/api/resources');
    const container = document.getElementById('catalog-grid');
    container.innerHTML = '';

    // Cache current tariffs in local state
    resources.forEach(r => {
      if (state.tariffRates[r.name] !== undefined) {
        state.tariffRates[r.name] = r.tariff;
      }
    });

    resources.forEach(r => {
      const card = document.createElement('div');
      card.className = 'card catalog-card glass';
      card.innerHTML = `
        <div class="catalog-head">
          <h4>${r.name}</h4>
          <span class="catalog-price">₹${r.tariff} <span>/ ${r.unit}</span></span>
        </div>
        <p class="catalog-desc">${r.description}</p>
        <div class="catalog-foot">
          <span>Available capacity:</span>
          <strong>${r.availableQuantity} ${r.unit}s / ${r.totalQuantity}</strong>
        </div>
      `;
      container.appendChild(card);
    });
  } catch (err) {
    console.error('Failed to load resource catalog:', err);
  }
}

/* --- Orders Handling --- */
async function loadOrders() {
  try {
    const orders = await apiRequest('/api/orders');
    const tbody = document.getElementById('orders-table-body');
    tbody.innerHTML = '';

    if (orders.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center">No submitted provisioning orders found.</td></tr>';
      return;
    }

    const isAdmin = state.user.role === 'ROLE_ADMIN' || state.user.role === 'ROLE_SUPER_ADMIN';

    orders.forEach(o => {
      let statusClass = 'badge-info';
      if (o.status === 'Completed' || o.status === 'Approved') statusClass = 'badge-success';
      else if (o.status === 'Cancelled') statusClass = 'badge-danger';
      else if (o.status === 'Pending Approval' || o.status === 'Provisioning') statusClass = 'badge-warning';

      const specsText = `${o.cpuCores} CPU / ${o.ramGb}GB RAM / ${o.storageGb}GB HDD` + (o.gpuCards > 0 ? ` / ${o.gpuCards} GPU` : '');
      const row = document.createElement('tr');
      row.innerHTML = `
        <td>#${o.id}</td>
        <td><strong>${o.user.username}</strong></td>
        <td>${o.vmName} (${o.operatingSystem})</td>
        <td>${specsText}</td>
        <td><span style="font-family: monospace;">${o.region}</span></td>
        <td><span class="badge ${statusClass}">${o.status}</span></td>
        <td>
          ${(isAdmin && o.status === 'Pending Approval') ? `
            <div style="display: flex; gap: 0.25rem;">
              <button onclick="handleOrderApproval(${o.id}, 'approve')" class="btn btn-primary" style="padding: 0.4rem 0.6rem; font-size: 0.75rem;">Approve</button>
              <button onclick="handleOrderApproval(${o.id}, 'reject')" class="btn btn-secondary" style="padding: 0.4rem 0.6rem; font-size: 0.75rem; color: var(--accent-red);">Reject</button>
            </div>
          ` : '<span>-</span>'}
        </td>
      `;
      tbody.appendChild(row);
    });
  } catch (err) {
    console.error('Failed to load orders queue:', err);
  }
}

async function handleOrderApproval(id, action) {
  try {
    const data = await apiRequest(`/api/orders/${id}/${action}`, { method: 'POST' });
    alert(`Order #${id} has been ${action === 'approve' ? 'approved and VM running' : 'rejected'}`);
    loadOrders();
    loadNotifications();
  } catch (err) {
    alert(`Action failed: ${err.message}`);
  }
}

/* --- Invoices & billing --- */
async function loadInvoices() {
  try {
    const invoices = await apiRequest('/api/invoices');
    const tbody = document.getElementById('invoices-table-body');
    tbody.innerHTML = '';

    if (invoices.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="text-center">No invoices generated yet.</td></tr>';
      return;
    }

    invoices.forEach(inv => {
      let statusClass = 'badge-info';
      if (inv.status === 'Paid') statusClass = 'badge-success';
      else if (inv.status === 'Generated') statusClass = 'badge-warning';
      else if (inv.status === 'Cancelled') statusClass = 'badge-danger';

      const dateStr = new Date(inv.billingPeriodStart).toLocaleDateString() + ' - ' + new Date(inv.billingPeriodEnd).toLocaleDateString();
      const dueStr = new Date(inv.dueDate).toLocaleDateString();
      const row = document.createElement('tr');
      row.innerHTML = `
        <td><strong>${inv.invoiceNumber}</strong></td>
        <td>${dateStr}</td>
        <td><strong>₹${inv.grandTotal.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</strong></td>
        <td><span class="badge ${statusClass}">${inv.status}</span></td>
        <td>${dueStr}</td>
        <td>
          ${inv.status === 'Generated' ? `<button onclick="openPaymentModal(${inv.id}, ${inv.grandTotal})" class="btn btn-primary" style="padding: 0.4rem 0.6rem; font-size: 0.75rem;">Pay Invoice</button>` : '<span>-</span>'}
        </td>
      `;
      tbody.appendChild(row);
    });
  } catch (err) {
    console.error('Failed to load invoices database:', err);
  }
}

function openPaymentModal(invoiceId, total) {
  document.getElementById('payment-invoice-id').value = invoiceId;
  document.getElementById('payment-amount-val').textContent = `₹${total.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
  document.getElementById('payment-modal').style.display = 'flex';
  lucide.createIcons();
}

// Payment form submission
document.getElementById('payment-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const invoiceId = document.getElementById('payment-invoice-id').value;
  const method = document.getElementById('pay-method').value;
  const simulateSuccess = document.getElementById('pay-simulate-success').checked;

  try {
    const data = await apiRequest('/api/payments', {
      method: 'POST',
      body: JSON.stringify({ invoiceId, method, simulateSuccess })
    });

    if (data.status === 'Successful') {
      alert('Transaction approved successfully! Invoice status set to PAID.');
    } else {
      alert('Transaction failed. Please check payment credentials and try again.');
    }
    document.getElementById('payment-modal').style.display = 'none';
    loadInvoices();
    loadNotifications();
  } catch (err) {
    alert(`Transaction refused: ${err.message}`);
  }
});

/* --- Users Management (Admin) --- */
async function loadUsers() {
  try {
    const users = await apiRequest('/api/users');
    const tbody = document.getElementById('users-table-body');
    tbody.innerHTML = '';

    users.forEach(u => {
      let statusClass = u.status === 'ACTIVE' ? 'badge-success' : 'badge-danger';
      const roleStr = u.role.replace('ROLE_', '');

      const row = document.createElement('tr');
      row.innerHTML = `
        <td>#${u.id}</td>
        <td><strong>${u.username}</strong></td>
        <td>${u.email}</td>
        <td>
          <select onchange="changeUserRole(${u.id}, this.value)" style="padding: 0.25rem; font-size: 0.8rem; width: max-content;">
            <option value="CUSTOMER" ${roleStr === 'CUSTOMER' ? 'selected' : ''}>Customer</option>
            <option value="SUPPORT" ${roleStr === 'SUPPORT' ? 'selected' : ''}>Support Engineer</option>
            <option value="BILLING_ADMIN" ${roleStr === 'BILLING_ADMIN' ? 'selected' : ''}>Billing Admin</option>
            <option value="ADMIN" ${roleStr === 'ADMIN' ? 'selected' : ''}>Administrator</option>
            <option value="SUPER_ADMIN" ${roleStr === 'SUPER_ADMIN' ? 'selected' : ''}>Super Admin</option>
          </select>
        </td>
        <td><span class="badge ${statusClass}">${u.status}</span></td>
        <td>${u.subscriptionPlan}</td>
        <td>
          <button onclick="toggleUserStatus(${u.id}, '${u.status}')" class="btn btn-secondary" style="padding: 0.4rem 0.6rem; font-size: 0.75rem;">
            ${u.status === 'ACTIVE' ? 'Disable' : 'Enable'}
          </button>
        </td>
      `;
      tbody.appendChild(row);
    });
  } catch (err) {
    console.error('Failed to load user list:', err);
  }
}

async function changeUserRole(id, role) {
  try {
    await apiRequest(`/api/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ role })
    });
    alert('User security role changed successfully.');
    loadUsers();
  } catch (err) {
    alert(`Update failed: ${err.message}`);
  }
}

async function toggleUserStatus(id, currentStatus) {
  const newStatus = currentStatus === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
  try {
    await apiRequest(`/api/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ status: newStatus })
    });
    loadUsers();
  } catch (err) {
    alert(`Update failed: ${err.message}`);
  }
}

/* --- Tariffs Configurator (Admin) --- */
async function loadTariffs() {
  try {
    const list = await apiRequest('/api/tariffs');
    const logList = document.getElementById('tariffs-log-list');
    logList.innerHTML = '';

    // Reverse list to show newest tariff updates on top
    const sorted = [...list].reverse();

    sorted.forEach(t => {
      const activeText = t.status === 'ACTIVE' ? '<span class="badge badge-success">Active</span>' : '<span>Archived</span>';
      const item = document.createElement('div');
      item.className = 'tariff-log-item glass';
      item.innerHTML = `
        <div>
          <strong>${t.resourceType}</strong> 
          <span style="font-size: 0.75rem; color: var(--text-muted); margin-left: 0.5rem;">${new Date(t.effectiveFrom).toLocaleDateString()}</span>
        </div>
        <div>
          <strong>₹${t.pricePerUnit}</strong>
          <span style="margin-left: 0.75rem;">${activeText}</span>
        </div>
      `;
      logList.appendChild(item);
    });
  } catch (err) {
    console.error('Failed to load tariffs list:', err);
  }
}

document.getElementById('tariff-update-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const resourceType = document.getElementById('tariff-resource-type').value;
  const pricePerUnit = parseFloat(document.getElementById('tariff-price').value);

  try {
    await apiRequest('/api/tariffs', {
      method: 'POST',
      body: JSON.stringify({ resourceType, pricePerUnit })
    });
    alert('Pricing tariff setting modified successfully. New active rate published.');
    document.getElementById('tariff-price').value = '';
    loadTariffs();
  } catch (err) {
    alert(`Tariff modification failed: ${err.message}`);
  }
});

/* --- Reports & Export Console (Staff/Admin) --- */
async function loadReports() {
  try {
    const summary = await apiRequest('/api/reports');
    const grid = document.getElementById('reports-grid');
    grid.innerHTML = '';

    summary.availableReports.forEach(rep => {
      const card = document.createElement('div');
      card.className = 'card report-card glass';
      card.innerHTML = `
        <div class="report-details">
          <h4>${rep.name}</h4>
          <p>${rep.description}</p>
        </div>
        <div>
          <button onclick="exportCSV('${rep.id}')" class="btn btn-secondary" style="width: 100%;">
            <i data-lucide="download-cloud"></i>
            <span>Download CSV Report</span>
          </button>
        </div>
      `;
      grid.appendChild(card);
    });
    lucide.createIcons();
  } catch (err) {
    console.error('Failed to load reports summary:', err);
  }
}

async function exportCSV(reportType) {
  try {
    const csvContent = await apiRequest(`/api/reports/${reportType}/export`);
    
    // Create CSV download link locally in-browser
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `${reportType}_report.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (err) {
    alert(`Could not download report: ${err.message}`);
  }
}

/* --- Audit Logs (Admin) --- */
async function loadAuditLogs() {
  try {
    const logs = await apiRequest('/api/audit-logs');
    const tbody = document.getElementById('logs-table-body');
    tbody.innerHTML = '';

    if (logs.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="text-center">No system log traces found.</td></tr>';
      return;
    }

    logs.forEach(log => {
      const row = document.createElement('tr');
      row.innerHTML = `
        <td><small>${new Date(log.timestamp).toLocaleString()}</small></td>
        <td><strong>${log.username}</strong></td>
        <td><span class="badge badge-info">${log.action}</span></td>
        <td><small style="color: var(--text-muted); font-family: monospace;">${log.previousValue || '-'}</small></td>
        <td><small style="color: var(--accent-purple); font-family: monospace;">${log.newValue || '-'}</small></td>
        <td><span style="font-family: monospace;">${log.ipAddress}</span></td>
      `;
      tbody.appendChild(row);
    });
  } catch (err) {
    console.error('Failed to load audit logs:', err);
  }
}

/* --- System Notifications --- */
async function loadNotifications() {
  if (!state.token) return;
  try {
    const unread = await apiRequest('/api/notifications/unread');
    const all = await apiRequest('/api/notifications');

    const badge = document.getElementById('noti-badge');
    if (unread.length > 0) {
      badge.textContent = unread.length;
      badge.style.display = 'inline-flex';
    } else {
      badge.style.display = 'none';
    }

    const container = document.getElementById('noti-list');
    container.innerHTML = '';

    if (all.length === 0) {
      container.innerHTML = '<div class="noti-empty">No alerts found.</div>';
      return;
    }

    all.forEach(n => {
      const isUnread = n.readStatus === 'UNREAD';
      const item = document.createElement('div');
      item.className = `noti-item ${isUnread ? 'unread' : ''}`;
      item.innerHTML = `
        <span>${n.message}</span>
        <span class="time">${new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
      `;
      container.appendChild(item);
    });

  } catch (err) {
    console.error('Failed to load notifications:', err);
  }
}

async function markNotificationsRead() {
  try {
    await apiRequest('/api/notifications/read', { method: 'POST' });
    loadNotifications();
  } catch (err) {
    console.error('Could not clear notifications:', err);
  }
}

// Notifications toggle
document.getElementById('noti-trigger-btn').addEventListener('click', (e) => {
  e.stopPropagation();
  const dropdown = document.getElementById('noti-dropdown');
  const isHidden = dropdown.style.display === 'none';
  dropdown.style.display = isHidden ? 'flex' : 'none';
});

document.getElementById('noti-read-all-btn').addEventListener('click', markNotificationsRead);

document.addEventListener('click', () => {
  document.getElementById('noti-dropdown').style.display = 'none';
});

/* --- App Initializations --- */
function setupNavigation() {
  document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', (e) => {
      e.preventDefault();
      const target = item.getAttribute('data-view');
      showView(target);
    });
  });
}

function checkSession() {
  if (state.token && state.user) {
    setupPortalAccess();
  } else {
    document.getElementById('auth-view').style.display = 'flex';
    document.getElementById('portal-view').style.display = 'none';
    toggleAuthForms('login');
  }
}

// Global script load initializer
document.addEventListener('DOMContentLoaded', () => {
  setupAuthListeners();
  setupNavigation();
  setupWizardFlow();
  checkSession();
  lucide.createIcons();
});
