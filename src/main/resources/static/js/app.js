// Глобальные переменные
let currentToken = null;
let currentPage = 0;
let editingItemId = null;

// Функции для работы с уведомлениями
function showAlert(message, type) {
    const alert = document.getElementById('alert');
    alert.textContent = message;
    alert.className = `alert alert-${type}`;
    alert.style.display = 'block';
    setTimeout(() => {
        alert.style.display = 'none';
    }, 5000);
}

// Функции аутентификации
async function register() {
    const username = document.getElementById('reg-username').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;

    if (!username || !email || !password) {
        showAlert('Please fill all fields', 'error');
        return;
    }

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, email, password })
        });

        if (response.ok) {
            const data = await response.json();
            showAlert('Registration successful! You can now login.', 'success');
            document.getElementById('reg-username').value = '';
            document.getElementById('reg-email').value = '';
            document.getElementById('reg-password').value = '';
        } else {
            const error = await response.text();
            showAlert('Registration failed: ' + error, 'error');
        }
    } catch (error) {
        showAlert('Error: ' + error.message, 'error');
    }
}

async function login() {
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    if (!username || !password) {
        showAlert('Please fill all fields', 'error');
        return;
    }

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            const data = await response.json();
            currentToken = data.token;
            const tokenInfo = document.getElementById('token-info');
            tokenInfo.innerHTML = `<strong>JWT Token:</strong><br>${currentToken.substring(0, 100)}...`;
            tokenInfo.style.display = 'block';
            document.getElementById('items-section').style.display = 'block';
            showAlert('Login successful!', 'success');
            loadItems();
        } else {
            showAlert('Login failed: Invalid credentials', 'error');
        }
    } catch (error) {
        showAlert('Error: ' + error.message, 'error');
    }
}

// Функции для работы с items
async function loadItems(page = 0) {
    if (!currentToken) return;

    const loading = document.getElementById('loading');
    const container = document.getElementById('items-container');
    loading.style.display = 'block';

    try {
        const response = await fetch(`/api/items?page=${page}&size=10`, {
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });

        if (response.ok) {
            const data = await response.json();
            displayItems(data.content);
            displayPagination(data);
            currentPage = page;
        } else if (response.status === 401) {
            showAlert('Session expired. Please login again.', 'error');
            currentToken = null;
            document.getElementById('items-section').style.display = 'none';
        }
    } catch (error) {
        showAlert('Error loading items: ' + error.message, 'error');
    } finally {
        loading.style.display = 'none';
    }
}

function displayItems(items) {
    const container = document.getElementById('items-container');

    if (!items || items.length === 0) {
        container.innerHTML = '<p style="text-align:center;">You have 0 items. Create a new one</p>';
        return;
    }

    container.innerHTML = items.map(item => `
        <div class="item-card">
            <div class="item-title">
                ${escapeHtml(item.name)}
                <span class="status-badge status-${item.status}">${item.status}</span>
            </div>
            <div class="item-details">
                <strong>Description:</strong> ${escapeHtml(item.description || 'No description')}<br>
                <strong>Price:</strong> $${item.price}<br>
                <strong>Created:</strong> ${new Date(item.createdAt).toLocaleString()}<br>
                <strong>Last updated:</strong> ${new Date(item.updatedAt).toLocaleString()}
            </div>
            <div class="item-actions">
                <button class="edit-btn" onclick="openEditModal(${item.id})">Edit</button>
                <button class="delete-btn" onclick="deleteItem(${item.id})">Delete</button>
            </div>
        </div>
    `).join('');
}

function displayPagination(pageData) {
    const pagination = document.getElementById('pagination');
    const totalPages = pageData.totalPages;
    const currentPage = pageData.number;

    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }

    let buttons = '';
    for (let i = 0; i < totalPages; i++) {
        buttons += `<button onclick="loadItems(${i})" style="${i === currentPage ? 'opacity:0.5;' : ''}">${i + 1}</button>`;
    }
    pagination.innerHTML = buttons;
}

async function createItem() {
    const name = document.getElementById('item-name').value;
    const description = document.getElementById('item-description').value;
    const price = document.getElementById('item-price').value;

    if (!name || !price) {
        showAlert('Please fill name and price', 'error');
        return;
    }

    try {
        const response = await fetch('/api/items', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({ name, description, price: parseFloat(price) })
        });

        if (response.ok) {
            showAlert('Item created successfully!', 'success');
            hideCreateForm();
            loadItems(currentPage);
        } else {
            const error = await response.text();
            showAlert('Failed to create item: ' + error, 'error');
        }
    } catch (error) {
        showAlert('Error: ' + error.message, 'error');
    }
}

function openEditModal(itemId) {
    editingItemId = itemId;
    fetch(`/api/items/${itemId}`, {
        headers: {
            'Authorization': `Bearer ${currentToken}`
        }
    })
    .then(response => response.json())
    .then(item => {
        document.getElementById('edit-name').value = item.name;
        document.getElementById('edit-description').value = item.description || '';
        document.getElementById('edit-price').value = item.price;
        document.getElementById('edit-status').value = item.status;
        document.getElementById('edit-modal').style.display = 'block';
    })
    .catch(error => {
        showAlert('Error loading item: ' + error.message, 'error');
    });
}

async function updateItem() {
    const name = document.getElementById('edit-name').value;
    const description = document.getElementById('edit-description').value;
    const price = document.getElementById('edit-price').value;
    const status = document.getElementById('edit-status').value;

    const updateData = {};
    if (name) updateData.name = name;
    if (description) updateData.description = description;
    if (price) updateData.price = parseFloat(price);
    if (status) updateData.status = status;

    try {
        const response = await fetch(`/api/items/${editingItemId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify(updateData)
        });

        if (response.ok) {
            showAlert('Item updated successfully!', 'success');
            closeModal();
            loadItems(currentPage);
        } else {
            const error = await response.text();
            showAlert('Failed to update item: ' + error, 'error');
        }
    } catch (error) {
        showAlert('Error: ' + error.message, 'error');
    }
}

async function deleteItem(itemId) {
    if (!confirm('Are you sure you want to delete this item?')) return;

    try {
        const response = await fetch(`/api/items/${itemId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });

        if (response.ok) {
            showAlert('Item deleted successfully!', 'success');
            loadItems(currentPage);
        } else {
            const error = await response.text();
            showAlert('Failed to delete item: ' + error, 'error');
        }
    } catch (error) {
        showAlert('Error: ' + error.message, 'error');
    }
}

// Вспомогательные функции
function showCreateForm() {
    document.getElementById('create-item-form').classList.add('active');
}

function hideCreateForm() {
    document.getElementById('create-item-form').classList.remove('active');
    document.getElementById('item-name').value = '';
    document.getElementById('item-description').value = '';
    document.getElementById('item-price').value = '';
}

function closeModal() {
    document.getElementById('edit-modal').style.display = 'none';
    editingItemId = null;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}