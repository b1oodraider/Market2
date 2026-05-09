// ===== СОСТОЯНИЕ ПРИЛОЖЕНИЯ =====

var app = {
    user: null,
    products: [],
    cart: []
};

// ===== ИНИЦИАЛИЗАЦИЯ =====

window.addEventListener('load', async function () {
    await checkAuth();
    router();
});

window.addEventListener('hashchange', router);

// ===== РОУТЕР =====

function router() {
    var hash = window.location.hash || '#/';

    renderHeader();

    if (hash === '#/' || hash === '#/catalog') {
        renderCatalog();
    } else if (hash.startsWith('#/product/')) {
        var id = hash.split('/')[2];
        renderProduct(id);
    } else if (hash === '#/cart') {
        if (!app.user) {
            window.location.hash = '#/login';
            return;
        }
        renderCart();
    } else if (hash === '#/login') {
        renderLogin();
    } else if (hash === '#/register') {
        renderRegister();
    } else if (hash === '#/admin/add') {
        if (!app.user || app.user.role !== 'ROLE_ADMIN') {
            window.location.hash = '#/';
            return;
        }
        renderAddProduct();
    } else {
        renderNotFound();
    }
}

// ===== API ФУНКЦИИ =====

async function apiGet(url) {
    var response = await fetch(url);
    if (!response.ok) throw response;
    return await response.json();
}

async function apiPost(url, data) {
    var response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });
    var json = await response.json();
    if (!response.ok) {
        var err = new Error('API error');
        err.data = json;
        err.status = response.status;
        throw err;
    }
    return json;
}

async function apiPatch(url, data) {
    var response = await fetch(url, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });
    if (!response.ok) throw response;
    return await response.json();
}

async function apiDelete(url) {
    var response = await fetch(url, {
        method: 'DELETE'
    });
    if (!response.ok) throw response;
    return await response.json();
}

// ===== АВТОРИЗАЦИЯ =====

async function checkAuth() {
    try {
        var data = await apiGet('/api/me');
        app.user = data;
    } catch (e) {
        app.user = null;
    }
}

async function doLogin(username, password) {
    var data = await apiPost('/api/login', { username: username, password: password });
    app.user = data;
    return data;
}

async function doRegister(username, password) {
    return await apiPost('/api/register', { username: username, password: password });
}

async function doLogout() {
    try {
        await apiPost('/api/logout', {});
    } catch (e) {
        // ignore
    }
    app.user = null;
    app.cart = [];
    window.location.hash = '#/';
}

// ===== ШАПКА =====

function renderHeader() {
    var header = document.getElementById('header');
    var html = '<div class="header-inner">';

    // logo
    html += '<a class="logo" href="#/">GreenCar</a>';

    // nav
    html += '<div class="nav-links">';
    html += '<a href="#/catalog">Каталог</a>';
    if (app.user) {
        html += '<a href="#/cart" class="cart-link">Корзина</a>';
    }
    if (app.user && app.user.role === 'ROLE_ADMIN') {
        html += '<a href="#/admin/add">Добавить товар</a>';
    }
    html += '</div>';

    // right side
    html += '<div class="header-right">';
    if (app.user) {
        html += '<span class="header-user">' + escapeHtml(app.user.username) + '</span>';
        html += '<button class="btn-logout" id="logoutBtn">Выйти</button>';
    } else {
        html += '<a href="#/login" class="nav-links"><a href="#/login">Войти</a></a>';
        html += '<a href="#/register" class="nav-links"><a href="#/register">Регистрация</a></a>';
    }
    html += '</div>';

    html += '</div>';
    header.innerHTML = html;

    // attach logout
    var logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function () {
            doLogout();
        });
    }
}

// ===== КАТАЛОГ =====

async function renderCatalog() {
    var page = document.getElementById('page');
    page.innerHTML = '<p class="loading">Загрузка товаров...</p>';

    try {
        var products = await apiGet('/api/products');
        app.products = products;

        if (products.length === 0) {
            page.innerHTML = '<h1 class="page-title">Каталог</h1><p class="info-message">Товаров пока нет</p>';
            return;
        }

        var html = '<h1 class="page-title">Каталог</h1>';
        html += '<div class="products-grid">';

        for (var i = 0; i < products.length; i++) {
            var p = products[i];
            html += '<div class="product-card">';
            html += '<h3>' + escapeHtml(p.productsName) + '</h3>';
            html += '<p class="product-type">' + escapeHtml(p.productsType) + '</p>';
            html += '<p class="product-stock">В наличии: ' + p.productsInStock + ' шт.</p>';
            html += '<div class="product-card-buttons">';
            html += '<a href="#/product/' + p.id + '" class="btn btn-primary btn-small">Подробнее</a>';
            if (app.user) {
                html += '<button class="btn btn-secondary btn-small add-to-cart-btn" data-id="' + p.id + '">В корзину</button>';
            }
            html += '</div>';
            html += '</div>';
        }

        html += '</div>';
        page.innerHTML = html;

        // attach add to cart buttons
        var buttons = document.querySelectorAll('.add-to-cart-btn');
        for (var j = 0; j < buttons.length; j++) {
            buttons[j].addEventListener('click', function () {
                var productId = this.getAttribute('data-id');
                addToCart(productId);
            });
        }

    } catch (e) {
        page.innerHTML = '<p class="error-message">Ошибка загрузки товаров</p>';
    }
}

async function addToCart(productId) {
    if (!app.user) {
        window.location.hash = '#/login';
        return;
    }
    try {
        await apiPost('/api/cart/' + productId, {});
        showNotification('Товар добавлен в корзину');
    } catch (e) {
        showNotification('Ошибка при добавлении в корзину');
    }
}

// ===== СТРАНИЦА ТОВАРА =====

async function renderProduct(id) {
    var page = document.getElementById('page');
    page.innerHTML = '<p class="loading">Загрузка...</p>';

    try {
        var product = await apiGet('/api/products/' + id);

        var html = '<div class="product-detail">';
        html += '<h1>' + escapeHtml(product.productsName) + '</h1>';
        html += '<p class="product-type">' + escapeHtml(product.productsType) + '</p>';
        html += '<p class="product-description">' + escapeHtml(product.productsDescription || 'Описание отсутствует') + '</p>';
        html += '<p class="product-stock">В наличии: ' + product.productsInStock + ' шт.</p>';
        html += '<div class="product-detail-buttons">';
        if (app.user) {
            html += '<button class="btn btn-primary" id="addToCartBtn">Добавить в корзину</button>';
        } else {
            html += '<a href="#/login" class="btn btn-secondary">Войдите, чтобы добавить в корзину</a>';
        }
        html += '<a href="#/catalog" class="btn btn-secondary">Назад к каталогу</a>';
        if (app.user) {
            html += '<a href="#/cart" class="btn btn-secondary">Просмотреть корзину</a>';
        }
        html += '</div>';
        html += '</div>';

        page.innerHTML = html;

        var addBtn = document.getElementById('addToCartBtn');
        if (addBtn) {
            addBtn.addEventListener('click', function () {
                addToCart(product.id);
            });
        }

    } catch (e) {
        page.innerHTML = '<p class="error-message">Товар не найден</p><a href="#/catalog" class="btn btn-secondary">Назад к каталогу</a>';
    }
}

// ===== КОРЗИНА =====

async function renderCart() {
    var page = document.getElementById('page');
    page.innerHTML = '<p class="loading">Загрузка корзины...</p>';

    try {
        var cart = await apiGet('/api/cart');
        app.cart = cart;

        var html = '<h1 class="page-title">Корзина</h1>';

        if (cart.length === 0) {
            html += '<p class="cart-empty">Корзина пуста</p>';
            html += '<a href="#/catalog" class="btn btn-primary">Перейти к каталогу</a>';
            page.innerHTML = html;
            return;
        }

        html += '<div class="cart-items">';
        for (var i = 0; i < cart.length; i++) {
            var item = cart[i];
            html += '<div class="cart-item">';
            html += '<span class="cart-item-name">' + escapeHtml(item.productsName) + '</span>';
            html += '<div class="cart-item-controls">';
            html += '<span>Кол-во:</span>';
            html += '<input type="number" min="1" value="' + item.productCount + '" id="count-' + item.productId + '">';
            html += '<button class="btn btn-primary btn-small change-count-btn" data-id="' + item.productId + '">Изменить</button>';
            html += '<button class="btn btn-danger btn-small remove-btn" data-id="' + item.productId + '">Удалить</button>';
            html += '</div>';
            html += '</div>';
        }
        html += '</div>';

        html += '<div class="cart-bottom">';
        html += '<button class="btn btn-danger" id="clearCartBtn">Очистить корзину</button>';
        html += '<a href="#/catalog" class="btn btn-secondary">Продолжить покупки</a>';
        html += '</div>';

        page.innerHTML = html;

        // change count buttons
        var changeButtons = document.querySelectorAll('.change-count-btn');
        for (var j = 0; j < changeButtons.length; j++) {
            changeButtons[j].addEventListener('click', function () {
                var productId = this.getAttribute('data-id');
                var input = document.getElementById('count-' + productId);
                var newCount = parseInt(input.value);
                if (newCount > 0) {
                    changeCartCount(productId, newCount);
                }
            });
        }

        // remove buttons
        var removeButtons = document.querySelectorAll('.remove-btn');
        for (var k = 0; k < removeButtons.length; k++) {
            removeButtons[k].addEventListener('click', function () {
                var productId = this.getAttribute('data-id');
                removeFromCart(productId);
            });
        }

        // clear cart
        var clearBtn = document.getElementById('clearCartBtn');
        if (clearBtn) {
            clearBtn.addEventListener('click', function () {
                clearCart();
            });
        }

    } catch (e) {
        page.innerHTML = '<p class="error-message">Ошибка загрузки корзины</p>';
    }
}

async function changeCartCount(productId, count) {
    try {
        await apiPatch('/api/cart/' + productId, { count: count });
        renderCart();
    } catch (e) {
        showNotification('Ошибка при изменении количества');
    }
}

async function removeFromCart(productId) {
    try {
        await apiDelete('/api/cart/' + productId);
        renderCart();
    } catch (e) {
        showNotification('Ошибка при удалении товара');
    }
}

async function clearCart() {
    try {
        await apiDelete('/api/cart');
        renderCart();
    } catch (e) {
        showNotification('Ошибка при очистке корзины');
    }
}

// ===== ЛОГИН =====

function renderLogin() {
    var page = document.getElementById('page');

    var html = '<div class="form-container">';
    html += '<h2>Вход</h2>';
    html += '<div id="loginError"></div>';
    html += '<div class="input-group">';
    html += '<label for="loginUsername">Имя пользователя</label>';
    html += '<input type="text" id="loginUsername" placeholder="Введите имя">';
    html += '</div>';
    html += '<div class="input-group">';
    html += '<label for="loginPassword">Пароль</label>';
    html += '<input type="password" id="loginPassword" placeholder="Введите пароль">';
    html += '</div>';
    html += '<button class="btn btn-primary" id="loginBtn">Войти</button>';
    html += '<p class="form-footer">Нет аккаунта? <a href="#/register">Зарегистрироваться</a></p>';
    html += '</div>';

    page.innerHTML = html;

    document.getElementById('loginBtn').addEventListener('click', handleLogin);
    // login on enter key
    document.getElementById('loginPassword').addEventListener('keydown', function (e) {
        if (e.key === 'Enter') handleLogin();
    });
    document.getElementById('loginUsername').addEventListener('keydown', function (e) {
        if (e.key === 'Enter') handleLogin();
    });
}

async function handleLogin() {
    var username = document.getElementById('loginUsername').value.trim();
    var password = document.getElementById('loginPassword').value;
    var errorDiv = document.getElementById('loginError');

    if (!username || !password) {
        errorDiv.innerHTML = '<p class="error-message">Заполните все поля</p>';
        return;
    }

    try {
        await doLogin(username, password);
        renderHeader();
        window.location.hash = '#/catalog';
    } catch (e) {
        var msg = 'Неправильное имя пользователя или пароль';
        if (e.data && e.data.error) {
            msg = e.data.error;
        }
        errorDiv.innerHTML = '<p class="error-message">' + escapeHtml(msg) + '</p>';
    }
}

// ===== РЕГИСТРАЦИЯ =====

function renderRegister() {
    var page = document.getElementById('page');

    var html = '<div class="form-container">';
    html += '<h2>Регистрация</h2>';
    html += '<div id="regError"></div>';
    html += '<div id="regSuccess"></div>';
    html += '<div class="input-group">';
    html += '<label for="regUsername">Имя пользователя</label>';
    html += '<input type="text" id="regUsername" placeholder="Придумайте имя">';
    html += '</div>';
    html += '<div class="input-group">';
    html += '<label for="regPassword">Пароль</label>';
    html += '<input type="password" id="regPassword" placeholder="Придумайте пароль (2-30 символов)">';
    html += '</div>';
    html += '<button class="btn btn-primary" id="regBtn">Зарегистрироваться</button>';
    html += '<p class="form-footer">Уже есть аккаунт? <a href="#/login">Войти</a></p>';
    html += '</div>';

    page.innerHTML = html;

    document.getElementById('regBtn').addEventListener('click', handleRegister);
    document.getElementById('regPassword').addEventListener('keydown', function (e) {
        if (e.key === 'Enter') handleRegister();
    });
}

async function handleRegister() {
    var username = document.getElementById('regUsername').value.trim();
    var password = document.getElementById('regPassword').value;
    var errorDiv = document.getElementById('regError');
    var successDiv = document.getElementById('regSuccess');

    errorDiv.innerHTML = '';
    successDiv.innerHTML = '';

    if (!username || !password) {
        errorDiv.innerHTML = '<p class="error-message">Заполните все поля</p>';
        return;
    }

    try {
        await doRegister(username, password);
        successDiv.innerHTML = '<p class="success-message">Регистрация успешна! Сейчас вы будете перенаправлены на страницу входа...</p>';
        setTimeout(function () {
            window.location.hash = '#/login';
        }, 1500);
    } catch (e) {
        var errors = e.data || {};
        var msg = '';
        if (errors.username) msg += '<p class="error-message">' + escapeHtml(errors.username) + '</p>';
        if (errors.password) msg += '<p class="error-message">' + escapeHtml(errors.password) + '</p>';
        if (!msg) msg = '<p class="error-message">Ошибка регистрации</p>';
        errorDiv.innerHTML = msg;
    }
}

// ===== ДОБАВЛЕНИЕ ТОВАРА (ADMIN) =====

function renderAddProduct() {
    var page = document.getElementById('page');

    var html = '<div class="form-container">';
    html += '<h2>Добавить товар</h2>';
    html += '<div id="addProductMsg"></div>';
    html += '<div class="input-group">';
    html += '<label for="prodName">Название</label>';
    html += '<input type="text" id="prodName" placeholder="Название товара">';
    html += '</div>';
    html += '<div class="input-group">';
    html += '<label for="prodType">Тип</label>';
    html += '<input type="text" id="prodType" placeholder="Тип товара">';
    html += '</div>';
    html += '<div class="input-group">';
    html += '<label for="prodDesc">Описание</label>';
    html += '<textarea id="prodDesc" placeholder="Описание товара"></textarea>';
    html += '</div>';
    html += '<div class="input-group">';
    html += '<label for="prodStock">Количество на складе</label>';
    html += '<input type="number" id="prodStock" min="0" placeholder="0">';
    html += '</div>';
    html += '<button class="btn btn-primary" id="addProductBtn">Добавить</button>';
    html += '<p class="form-footer"><a href="#/catalog">Вернуться к каталогу</a></p>';
    html += '</div>';

    page.innerHTML = html;

    document.getElementById('addProductBtn').addEventListener('click', handleAddProduct);
}

async function handleAddProduct() {
    var name = document.getElementById('prodName').value.trim();
    var type = document.getElementById('prodType').value.trim();
    var desc = document.getElementById('prodDesc').value.trim();
    var stock = parseInt(document.getElementById('prodStock').value) || 0;
    var msgDiv = document.getElementById('addProductMsg');

    if (!name || !type) {
        msgDiv.innerHTML = '<p class="error-message">Заполните название и тип товара</p>';
        return;
    }

    try {
        await apiPost('/api/products', {
            productsName: name,
            productsType: type,
            productsDescription: desc,
            productsInStock: stock
        });
        msgDiv.innerHTML = '<p class="success-message">Товар успешно добавлен!</p>';
        // clear form
        document.getElementById('prodName').value = '';
        document.getElementById('prodType').value = '';
        document.getElementById('prodDesc').value = '';
        document.getElementById('prodStock').value = '';
    } catch (e) {
        msgDiv.innerHTML = '<p class="error-message">Ошибка при добавлении товара</p>';
    }
}

// ===== 404 =====

function renderNotFound() {
    var page = document.getElementById('page');
    page.innerHTML = '<h1 class="page-title">Страница не найдена</h1>' +
        '<p>Такой страницы нет.</p>' +
        '<a href="#/catalog" class="btn btn-primary" style="margin-top:15px;display:inline-block;">На главную</a>';
}

// ===== УВЕДОМЛЕНИЯ =====

function showNotification(text) {
    // remove old notification if exists
    var old = document.getElementById('notification');
    if (old) old.remove();

    var div = document.createElement('div');
    div.id = 'notification';
    div.style.cssText = 'position:fixed;bottom:20px;right:20px;background:#333;color:white;padding:12px 22px;border-radius:6px;font-size:14px;z-index:1000;box-shadow:0 2px 10px rgba(0,0,0,0.3);transition:opacity 0.3s;';
    div.textContent = text;
    document.body.appendChild(div);

    setTimeout(function () {
        div.style.opacity = '0';
        setTimeout(function () {
            div.remove();
        }, 300);
    }, 2000);
}

// ===== УТИЛИТЫ =====

function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
