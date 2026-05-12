/**
 * app.js – E-Commerce Frontend
 *
 * Communicates with the Spring Boot REST API (localhost:8080)
 * using the Fetch API with async/await for all data operations.
 *
 * Key features:
 *  - fetchProducts()   → loads products from the database on page load
 *  - fetchCategories() → populates filter & form dropdowns
 *  - createProduct()   → POSTs a new product via the modal form
 *  - Cart state is managed in-memory (JS variable)
 */

'use strict';

// ── Configuration ─────────────────────────────────────────────────────────
const API_BASE = 'http://localhost:8080/api';

// ── Cart state ────────────────────────────────────────────────────────────
let cartCount = 0;

// ── DOM references ────────────────────────────────────────────────────────
const productGrid    = document.getElementById('productGrid');
const loadingState   = document.getElementById('loadingState');
const emptyState     = document.getElementById('emptyState');
const errorState     = document.getElementById('errorState');
const errorTitle     = document.getElementById('errorTitle');
const errorMessage   = document.getElementById('errorMessage');
const productCount   = document.getElementById('productCount');
const categoryFilter = document.getElementById('categoryFilter');
const pCategorySelect = document.getElementById('pCategory');
const cartCountEl    = document.querySelector('.cart-count');
const modalOverlay   = document.getElementById('modalOverlay');
const formError      = document.getElementById('formError');

// ─────────────────────────────────────────────────────────────────────────
// UTILITY: Centralised fetch wrapper
// ─────────────────────────────────────────────────────────────────────────

/**
 * Generic fetch helper that checks response.ok and throws descriptive
 * errors so every caller gets a consistent failure mode.
 *
 * @param {string} url      - Full URL to request
 * @param {RequestInit} [options] - Optional fetch options (method, body, etc.)
 * @returns {Promise<any>}  - Parsed JSON response body
 * @throws {Error}          - With HTTP status embedded in the message
 */
async function apiFetch(url, options = {}) {
  // Default headers
  const defaultHeaders = { 'Content-Type': 'application/json', Accept: 'application/json' };
  options.headers = { ...defaultHeaders, ...options.headers };

  const response = await fetch(url, options);

  // response.ok is true for 2xx status codes
  if (!response.ok) {
    // Attempt to read the JSON error body from our GlobalExceptionHandler
    let serverMessage = `HTTP ${response.status} – ${response.statusText}`;
    try {
      const errBody = await response.json();
      serverMessage = errBody.message || serverMessage;
    } catch {
      // If the body isn't JSON, keep the default message
    }

    // Throw with specific messages for common statuses
    if (response.status === 404) throw new Error(`Not found: ${serverMessage}`);
    if (response.status === 400 || response.status === 422) throw new Error(`Bad request: ${serverMessage}`);
    if (response.status >= 500) throw new Error(`Server error: ${serverMessage}`);
    throw new Error(serverMessage);
  }

  // Return parsed JSON, or null for 204 No Content
  return response.status === 204 ? null : response.json();
}

// ─────────────────────────────────────────────────────────────────────────
// PRODUCTS – Fetch & Render
// ─────────────────────────────────────────────────────────────────────────

/**
 * Fetches products from the backend and renders them into the grid.
 *
 * Accepts an optional params object to build query strings for filtering:
 *   { category, search, minPrice, maxPrice }
 *
 * Error handling:
 *  - try…catch captures network failures and non-2xx responses.
 *  - response.ok is checked inside apiFetch() before JSON parsing.
 *  - Specific 404/400/5xx messages are surfaced to the user.
 *
 * @param {Object} [params={}] - Optional filter parameters
 */
async function fetchProducts(params = {}) {
  showLoading();

  try {
    // Build query string from params (skip empty values)
    const qs = new URLSearchParams(
      Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null))
    ).toString();

    const url = `${API_BASE}/products${qs ? '?' + qs : ''}`;

    console.log('[fetchProducts] GET', url);

    // Await the API response – apiFetch throws on non-2xx
    const products = await apiFetch(url);

    console.log('[fetchProducts] Response:', products);

    // Render or show empty state
    if (!products || products.length === 0) {
      showEmpty();
    } else {
      renderProducts(products);
    }
  } catch (error) {
    // Log the full error for debugging
    console.error('[fetchProducts] Error:', error);
    showError('Failed to load products', error.message);
  }
}

/**
 * Injects product cards into the DOM.
 *
 * @param {Array} products - Array of ProductDTO objects from the API
 */
function renderProducts(products) {
  hideAllStates();
  productGrid.innerHTML = '';
  productCount.textContent = `${products.length} product${products.length !== 1 ? 's' : ''}`;

  // Stagger animation delay for each card
  products.forEach((product, index) => {
    const card = createProductCard(product, index);
    productGrid.appendChild(card);
  });
}

/**
 * Creates and returns a product card HTMLElement.
 *
 * @param {Object} product - ProductDTO from the API
 * @param {number} index   - Index used for staggered animation delay
 * @returns {HTMLElement}
 */
function createProductCard(product, index) {
  const card = document.createElement('article');
  card.className = 'product-card';
  card.style.animationDelay = `${index * 0.05}s`;

  const stockClass = product.stock === 0 ? 'out' : product.stock < 5 ? 'low' : '';
  const stockLabel = product.stock === 0 ? 'Out of stock' : `${product.stock} in stock`;

  // Image or placeholder emoji
  const imageHTML = product.imageUrl
    ? `<img class="card-image" src="${escapeHtml(product.imageUrl)}" alt="${escapeHtml(product.name)}" loading="lazy" />`
    : `<div class="card-image-placeholder">🛍️</div>`;

  card.innerHTML = `
    ${imageHTML}
    <div class="card-body">
      ${product.categoryName ? `<span class="card-category">${escapeHtml(product.categoryName)}</span>` : ''}
      <h3 class="card-name">${escapeHtml(product.name)}</h3>
      ${product.description ? `<p class="card-description">${escapeHtml(product.description)}</p>` : ''}
    </div>
    <div class="card-footer">
      <div>
        <div class="card-price">₱${parseFloat(product.price).toFixed(2)}</div>
        <div class="card-stock ${stockClass}">${stockLabel}</div>
      </div>
      <button
        class="btn-add-cart"
        data-id="${product.id}"
        ${product.stock === 0 ? 'disabled' : ''}
      >Add</button>
    </div>
  `;

  // Add-to-cart handler
  card.querySelector('.btn-add-cart')?.addEventListener('click', () => addToCart(product));

  return card;
}

// ─────────────────────────────────────────────────────────────────────────
// CATEGORIES – Fetch & Populate Dropdowns
// ─────────────────────────────────────────────────────────────────────────

/**
 * Fetches categories and populates both the filter <select>
 * and the modal form <select>.
 *
 * try…catch handles network or server errors gracefully —
 * the page still works even if categories fail to load.
 */
async function fetchCategories() {
  try {
    const categories = await apiFetch(`${API_BASE}/categories`);
    console.log('[fetchCategories] Response:', categories);
    populateCategoryDropdowns(categories);
  } catch (error) {
    console.error('[fetchCategories] Error:', error);
    // Non-fatal: categories dropdowns stay at their default values
  }
}

/**
 * Inserts <option> elements into both category <select> elements.
 *
 * @param {Array} categories - Array of Category objects { id, name }
 */
function populateCategoryDropdowns(categories) {
  const options = categories.map(c =>
    `<option value="${c.id}">${escapeHtml(c.name)}</option>`
  ).join('');

  // Filter bar dropdown (value = name for query param)
  categoryFilter.innerHTML =
    '<option value="">All</option>' +
    categories.map(c => `<option value="${escapeHtml(c.name)}">${escapeHtml(c.name)}</option>`).join('');

  // Modal form dropdown (value = id for the POST body)
  pCategorySelect.innerHTML = `<option value="">— None —</option>` + options;
}

// ─────────────────────────────────────────────────────────────────────────
// CREATE PRODUCT – POST via Fetch
// ─────────────────────────────────────────────────────────────────────────

/**
 * Reads the modal form values, validates them, then POSTs to the API.
 *
 * try…catch:
 *  - Catches validation errors returned by Spring (422) and displays them.
 *  - Catches network errors and shows an inline error message.
 *  - On success, closes the modal and refreshes the product grid.
 */
async function createProduct() {
  const btnSubmit = document.getElementById('btnSubmit');
  hideFormError();

  // Collect form values
  const payload = {
    name:        document.getElementById('pName').value.trim(),
    description: document.getElementById('pDesc').value.trim(),
    price:       parseFloat(document.getElementById('pPrice').value),
    stock:       parseInt(document.getElementById('pStock').value, 10),
    categoryId:  document.getElementById('pCategory').value || null,
    imageUrl:    document.getElementById('pImage').value.trim() || null,
  };

  // Basic client-side validation before hitting the API
  if (!payload.name) { showFormError('Product name is required.'); return; }
  if (isNaN(payload.price) || payload.price <= 0) { showFormError('Enter a valid price greater than 0.'); return; }
  if (isNaN(payload.stock) || payload.stock < 0) { showFormError('Stock cannot be negative.'); return; }

  btnSubmit.disabled = true;
  btnSubmit.textContent = 'Saving…';

  try {
    console.log('[createProduct] POST payload:', payload);

    const created = await apiFetch(`${API_BASE}/products`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });

    console.log('[createProduct] Created:', created);

    closeModal();
    await fetchProducts(); // Refresh grid from DB
  } catch (error) {
    console.error('[createProduct] Error:', error);
    showFormError(error.message);
  } finally {
    btnSubmit.disabled = false;
    btnSubmit.textContent = 'Save Product';
  }
}

// ─────────────────────────────────────────────────────────────────────────
// CART (in-memory)
// ─────────────────────────────────────────────────────────────────────────

/** Increments the in-memory cart count and updates the badge. */
function addToCart(product) {
  cartCount++;
  cartCountEl.textContent = cartCount;
  // Visual feedback
  const btn = productGrid.querySelector(`[data-id="${product.id}"]`);
  if (btn) {
    btn.textContent = '✓ Added';
    setTimeout(() => { btn.textContent = 'Add'; }, 1200);
  }
}

// ─────────────────────────────────────────────────────────────────────────
// UI State helpers
// ─────────────────────────────────────────────────────────────────────────

function showLoading() {
  loadingState.hidden = false;
  productGrid.innerHTML = '';
  emptyState.hidden = true;
  errorState.hidden = true;
  productCount.textContent = '';
}

function hideAllStates() {
  loadingState.hidden = true;
  emptyState.hidden = true;
  errorState.hidden = true;
}

function showEmpty() {
  loadingState.hidden = true;
  emptyState.hidden = false;
  productCount.textContent = '0 products';
}

function showError(title, message) {
  loadingState.hidden = true;
  errorState.hidden = false;
  errorTitle.textContent = title;
  errorMessage.textContent = message;
}

function showFormError(msg) {
  formError.textContent = msg;
  formError.hidden = false;
}

function hideFormError() {
  formError.hidden = true;
  formError.textContent = '';
}

// ─────────────────────────────────────────────────────────────────────────
// MODAL helpers
// ─────────────────────────────────────────────────────────────────────────

function openModal() {
  modalOverlay.hidden = false;
  document.getElementById('pName').focus();
}

function closeModal() {
  modalOverlay.hidden = true;
  // Reset form fields
  ['pName','pDesc','pPrice','pStock','pImage'].forEach(id => {
    document.getElementById(id).value = '';
  });
  document.getElementById('pCategory').value = '';
  hideFormError();
}

/** Convenience function used by the empty-state "Show all" button. */
window.resetAndFetch = function () {
  categoryFilter.value = '';
  document.getElementById('minPrice').value = '';
  document.getElementById('maxPrice').value = '';
  fetchProducts();
};

// ─────────────────────────────────────────────────────────────────────────
// SECURITY: XSS prevention
// ─────────────────────────────────────────────────────────────────────────

/** Escapes HTML special characters to prevent XSS when injecting user data. */
function escapeHtml(str) {
  if (str == null) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

// ─────────────────────────────────────────────────────────────────────────
// EVENT LISTENERS
// ─────────────────────────────────────────────────────────────────────────

// FAB → open modal
document.getElementById('fabBtn').addEventListener('click', openModal);

// Modal close buttons
document.getElementById('modalClose').addEventListener('click', closeModal);
document.getElementById('btnCancel').addEventListener('click', closeModal);
modalOverlay.addEventListener('click', e => { if (e.target === modalOverlay) closeModal(); });

// Modal submit
document.getElementById('btnSubmit').addEventListener('click', createProduct);

// Filter bar – Apply
document.getElementById('applyFilters').addEventListener('click', () => {
  const params = {
    category: categoryFilter.value,
    minPrice: document.getElementById('minPrice').value,
    maxPrice: document.getElementById('maxPrice').value,
  };
  fetchProducts(params);
});

// Filter bar – Reset
document.getElementById('resetFilters').addEventListener('click', resetAndFetch);

// Search
document.getElementById('searchBtn').addEventListener('click', () => {
  const search = document.getElementById('searchInput').value.trim();
  if (search) fetchProducts({ search });
  else fetchProducts();
});
document.getElementById('searchInput').addEventListener('keydown', e => {
  if (e.key === 'Enter') document.getElementById('searchBtn').click();
});

// ─────────────────────────────────────────────────────────────────────────
// INITIALISE on page load
// ─────────────────────────────────────────────────────────────────────────
(async function init() {
  // Load categories first (populates dropdowns), then load all products
  await fetchCategories();
  await fetchProducts();
})();
