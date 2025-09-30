// Текущий авторизованный пользователь
let currentUser = null;
// WT токен для авторизации запросов
let currentToken = null;
// Массив загруженных пользователем файлов
let userUploads = [];

// Инициализация приложения после загрузки DOM
document.addEventListener('DOMContentLoaded', function() {
    checkAuth();
    setupFileUpload();
    setupEventListeners();
});

// Настройка обработчиков событий
function setupEventListeners() {
    // Обработчик Enter для поля пароля
    document.getElementById('password').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            login();
        }
    });
}

// Аутентификация пользователя
async function login() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    if (!username || !password) {
        alert('Пожалуйста, заполните все поля!');
        return;
    }

    const loginBtn = document.querySelector('#login-form button');
    const originalText = loginBtn.innerHTML;

    // Cостояние загрузки
    loginBtn.innerHTML = '⏳ Вход...';
    loginBtn.disabled = true;

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            const data = await response.json();
            currentUser = username;
            currentToken = data.token;

            // Обновление UI после успешного входа
            updateUIAfterLogin(username);

            // Сохранение данные в localStorage
            localStorage.setItem('authToken', currentToken);
            localStorage.setItem('username', username);

            // Загрузка файлов пользователя
            await loadUserFilesFromServer();

            showNotification('Успешный вход в систему!', 'success');
        } else {
            const error = await response.text();
            showNotification('Ошибка входа: ' + (error || 'Неверные учетные данные'), 'error');
        }
    } catch (error) {
        console.error('Login error:', error);
        showNotification('Ошибка соединения!', 'error');
    } finally {
        // Восстанавление состояние кнопки
        loginBtn.innerHTML = originalText;
        loginBtn.disabled = false;
    }
}

// Обновление интерфейса после успешного входа
function updateUIAfterLogin(username) {
    document.getElementById('login-form').style.display = 'none';
    document.getElementById('user-info').style.display = 'flex';
    document.getElementById('welcome-message').textContent = `Добро пожаловать, ${username}!`;
    document.getElementById('upload-section').style.display = 'block';
}

// Выход из системы
function logout() {
    // Сброс состояния
    currentUser = null;
    currentToken = null;
    userUploads = [];

    // Очистка localStorage
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    localStorage.removeItem('userUploads');

    // Сброс UI
    resetUIAfterLogout();

    // Очистка список загрузок
    clearUploadsList();

    showNotification('Вы вышли из системы', 'info');
}

//Сброс интерфейса после выхода
function resetUIAfterLogout() {
    document.getElementById('login-form').style.display = 'block';
    document.getElementById('user-info').style.display = 'none';
    document.getElementById('upload-section').style.display = 'none';
    document.getElementById('stats-section').style.display = 'none';

    // Сбррос формы загрузки
    document.getElementById('file-input').value = '';
    document.getElementById('upload-btn').disabled = true;
    document.getElementById('upload-status').innerHTML = '';
    document.getElementById('file-info').style.display = 'none';
}

// Проверка авторизации при загрузке страницы
async function checkAuth() {
    const savedToken = localStorage.getItem('authToken');
    const savedUsername = localStorage.getItem('username');

    if (savedToken && savedUsername) {
        try {
            const response = await fetch('/api/stats', {
                headers: { 'Authorization': `Bearer ${savedToken}` }
            });

            if (response.ok) {
                currentUser = savedUsername;
                currentToken = savedToken;

                // Обновление UI для авторизованного пользователя
                document.getElementById('login-form').style.display = 'none';
                document.getElementById('user-info').style.display = 'flex';
                document.getElementById('welcome-message').textContent = `Добро пожаловать, ${savedUsername}!`;
                document.getElementById('upload-section').style.display = 'block';

                await loadUserFilesFromServer();
            } else {
                logout();
            }
        } catch (e) {
            console.error('Ошибка проверки токена', e);
            logout();
        }
    }
}

// Настройка функционала загрузки файлов
function setupFileUpload() {
    const fileInput = document.getElementById('file-input');
    const uploadArea = document.getElementById('upload-area');
    const uploadBtn = document.getElementById('upload-btn');

    // Обработчик выбора файла через диалог
    fileInput.addEventListener('change', function() {
        if (this.files.length > 0) {
            handleFileSelection(this.files[0]);
        }
    });

    // Drag and Drop
    uploadArea.addEventListener('dragover', function(e) {
        e.preventDefault();
        this.classList.add('dragover');
    });

    uploadArea.addEventListener('dragleave', function() {
        this.classList.remove('dragover');
    });

    uploadArea.addEventListener('drop', function(e) {
        e.preventDefault();
        this.classList.remove('dragover');

        if (e.dataTransfer.files.length > 0) {
            const file = e.dataTransfer.files[0];
            fileInput.files = e.dataTransfer.files;
            handleFileSelection(file);
        }
    });
}

// Обработка выбранного файла
function handleFileSelection(file) {
    const fileInfo = document.getElementById('file-info');
    const fileName = document.getElementById('file-name');
    const fileSize = document.getElementById('file-size');
    const uploadBtn = document.getElementById('upload-btn');

    // Обновление информации о файле
    fileName.textContent = file.name;
    fileSize.textContent = formatFileSize(file.size);
    fileInfo.style.display = 'block';
    uploadBtn.disabled = false;

    // Проверка размера файла (до 100)
    if (file.size > 100 * 1024 * 1024) {
        showNotification('Файл слишком большой! Максимальный размер: 100MB', 'error');
        uploadBtn.disabled = true;
    }
}

// Очистка выбранного файла
function clearFile() {
    const fileInput = document.getElementById('file-input');
    const fileInfo = document.getElementById('file-info');
    const uploadBtn = document.getElementById('upload-btn');

    fileInput.value = '';
    fileInfo.style.display = 'none';
    uploadBtn.disabled = true;
}

// Загрузка файла на сервер
async function uploadFile() {
    const fileInput = document.getElementById('file-input');
    const file = fileInput.files[0];

    if (!file || !currentToken) {
        showNotification('Пожалуйста, выберите файл!', 'error');
        return;
    }

    const statusDiv = document.getElementById('upload-status');
    const uploadBtn = document.getElementById('upload-btn');
    const btnText = uploadBtn.querySelector('.btn-text');
    const btnLoading = uploadBtn.querySelector('.btn-loading');

    // Состояние загрузки
    btnText.style.display = 'none';
    btnLoading.style.display = 'inline';
    uploadBtn.disabled = true;

    statusDiv.innerHTML = '<div class="status-uploading">⏳ Загрузка файла...</div>';

    try {
        const formData = new FormData();
        formData.append('file', file);

        const uploadUrl = `/api/upload?filename=${encodeURIComponent(file.name)}`;

        const response = await fetch(uploadUrl, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${currentToken}`
            },
            body: formData
        });

        if (response.ok) {
            const data = await response.json();
            statusDiv.innerHTML = '<div class="status-success">✅ Файл успешно загружен!</div>';

            const downloadUrl = `${window.location.origin}/api/download/${data.downloadId}`;

            // Добавление файла в историю загрузок
            const uploadItem = {
                fileName: file.name,
                fileSize: file.size,
                downloadUrl: downloadUrl,
                downloadId: data.downloadId,
                timestamp: new Date().toISOString()
            };

            userUploads.unshift(uploadItem);
            saveUserUploads();
            displayUserUploads();

            // Очистка формы
            clearFile();
            showNotification('Файл успешно загружен!', 'success');
        } else {
            const error = await response.text();
            statusDiv.innerHTML = `<div class="status-error">❌ Ошибка загрузки: ${error || response.status}</div>`;
            showNotification('Ошибка при загрузке файла', 'error');
        }
    } catch (error) {
        console.error('Upload error:', error);
        statusDiv.innerHTML = '<div class="status-error">❌ Ошибка соединения!</div>';
        showNotification('Ошибка соединения при загрузке', 'error');
    } finally {
        // Восстанавление состояния кнопки
        btnText.style.display = 'inline';
        btnLoading.style.display = 'none';
        uploadBtn.disabled = false;
    }
}

// Загрузка файлов пользователя с сервера
async function loadUserFilesFromServer() {
    if (!currentToken) return;

    try {
        const response = await fetch('/api/user/files', {
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });

        if (response.ok) {
            const data = await response.json();

            // Преобразование данных с сервера
            const serverFiles = data.files.map(file => ({
                fileName: file.fileName,
                fileSize: file.fileSize,
                downloadUrl: `${window.location.origin}/api/download/${file.downloadId}`,
                downloadId: file.downloadId,
                timestamp: file.uploadDate
            }));

            // Загрузка локальных данных
            const localUploads = JSON.parse(localStorage.getItem('userUploads') || '[]');

            // Объединение данных, убирая дубликаты
            const uploadsMap = new Map();

            // Приоритет у серверных данных
            serverFiles.forEach(file => {
                uploadsMap.set(file.downloadId, file);
            });

            // Добавление локальных, если нет на сервере
            localUploads.forEach(file => {
                if (!uploadsMap.has(file.downloadId)) {
                    uploadsMap.set(file.downloadId, file);
                }
            });

            // Сортировка по времени (новые сверху)
            userUploads = Array.from(uploadsMap.values())
                .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

            saveUserUploads();
            displayUserUploads();
        } else {
            console.error('Failed to load files from server');
            // Если сервер недоступен, используются локальные данные
            loadUserUploads();
        }
    } catch (error) {
        console.error('Error loading files from server:', error);
        loadUserUploads();
    }
}

// Сохранение истории загрузок в localStorage
function saveUserUploads() {
    localStorage.setItem('userUploads', JSON.stringify(userUploads));
}

// Загрузка истории загрузок из localStorage
function loadUserUploads() {
    const saved = localStorage.getItem('userUploads');
    if (saved) {
        userUploads = JSON.parse(saved);
        displayUserUploads();
    }
}

// Отображение списка загруженных файлов
function displayUserUploads() {
    const uploadsList = document.getElementById('uploads-list');
    if (!uploadsList) return;

    if (userUploads.length === 0) {
        uploadsList.innerHTML = '<p style="text-align: center; color: var(--text-secondary); padding: 20px;">Загруженные файлы появятся здесь</p>';
        return;
    }

    uploadsList.innerHTML = userUploads.map(item => `
        <div class="upload-item">
            <div class="upload-item-info">
                <span class="file-icon">📄</span>
                <div class="upload-details">
                    <div class="upload-file-name">${item.fileName}</div>
                    <div class="upload-meta">
                        ${formatFileSize(item.fileSize)} • 
                        ${new Date(item.timestamp).toLocaleString('ru-RU')}
                    </div>
                </div>
            </div>
            <div class="upload-actions">
                <a href="${item.downloadUrl}" target="_blank" class="btn-download">📥 Скачать</a>
                <button onclick="copyToClipboard('${item.downloadUrl}')" class="btn-copy" title="Копировать ссылку">📋</button>
            </div>
        </div>
    `).join('');
}

// Очистка списка загрузок
function clearUploadsList() {
    const uploadsList = document.getElementById('uploads-list');
    if (uploadsList) {
        uploadsList.innerHTML = '<p style="text-align: center; color: var(--text-secondary); padding: 20px;">Загруженные файлы появятся здесь</p>';
    }
}

// Показ статистики
async function showStats() {
    if (!currentToken) return;

    // Переключение видимости секций
    document.getElementById('upload-section').style.display = 'none';
    document.getElementById('stats-section').style.display = 'block';

    try {
        const response = await fetch('/api/stats', {
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });

        if (response.ok) {
            const stats = await response.json();
            displayStats(stats);
        } else {
            document.getElementById('stats-content').innerHTML =
                '<div class="status-error">❌ Ошибка загрузки статистики</div>';
        }
    } catch (error) {
        console.error('Stats error:', error);
        document.getElementById('stats-content').innerHTML =
            '<div class="status-error">❌ Ошибка соединения</div>';
    }
}

// Отображение статистики
function displayStats(stats) {
    const statsContent = document.getElementById('stats-content');

    statsContent.innerHTML = `
        <div class="stats-grid">
            <div class="stat-card">
                <h3>Всего файлов</h3>
                <div class="value">${stats.totalFiles}</div>
            </div>
            <div class="stat-card">
                <h3>Общий размер</h3>
                <div class="value">${formatFileSize(stats.totalSize)}</div>
            </div>
            <div class="stat-card">
                <h3>Активные загрузки</h3>
                <div class="value">${stats.activeDownloads}</div>
            </div>
        </div>
        
        <div class="file-list">
            <h3>Недавние файлы</h3>
            ${stats.recentFiles && stats.recentFiles.length > 0 ?
        stats.recentFiles.map(file => `
                    <div class="file-item">
                        <div class="file-name">${file.fileName}</div>
                        <div class="file-details">
                            ${formatFileSize(file.fileSize)} • 
                            ${new Date(file.uploadDate).toLocaleDateString('ru-RU')} •
                            Скачиваний: ${file.downloadCount}
                        </div>
                    </div>
                `).join('') :
        '<p style="text-align: center; color: var(--text-secondary); padding: 20px;">Нет загруженных файлов</p>'
    }
        </div>
    `;
}

// Скрытие статистики и возврат к загрузке файлов
function hideStats() {
    document.getElementById('stats-section').style.display = 'none';
    document.getElementById('upload-section').style.display = 'block';
}

// Форматирование размера файла в читаемый вид
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Байт';

    const k = 1024;
    const sizes = ['Байт', 'КБ', 'МБ', 'ГБ'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Показ уведомлений
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <span>${message}</span>
        <button onclick="this.parentElement.remove()">✕</button>
    `;

    // Динамическое добавление стилей если их нет
    if (!document.querySelector('#notification-styles')) {
        const styles = document.createElement('style');
        styles.id = 'notification-styles';
        styles.textContent = `
            .notification {
                position: fixed;
                top: 20px;
                right: 20px;
                padding: 12px 16px;
                border-radius: var(--radius);
                color: white;
                font-weight: 500;
                z-index: 1000;
                display: flex;
                align-items: center;
                gap: 10px;
                max-width: 400px;
                box-shadow: var(--shadow-lg);
                animation: slideInRight 0.3s ease;
                font-size: 14px;
            }
            .notification-success { background: var(--secondary-color); }
            .notification-error { background: #dc2626; }
            .notification-info { background: var(--primary-color); }
            .notification button {
                background: none;
                border: none;
                color: white;
                cursor: pointer;
                padding: 0;
                font-size: 14px;
                opacity: 0.8;
            }
            .notification button:hover {
                opacity: 1;
            }
            @keyframes slideInRight {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
        `;
        document.head.appendChild(styles);
    }

    document.body.appendChild(notification);

    // Автоматическое удаление через 5 секунд
    setTimeout(() => {
        if (notification.parentElement) {
            notification.remove();
        }
    }, 5000);
}

// Копирование текста в буфер обмена
async function copyToClipboard(text) {
    try {
        await navigator.clipboard.writeText(text);
        showNotification('Ссылка скопирована в буфер обмена!', 'success');
    } catch (err) {
        console.error('Copy failed:', err);
        const textArea = document.createElement('textarea');
        textArea.value = text;
        document.body.appendChild(textArea);
        textArea.select();
        try {
            document.execCommand('copy');
            showNotification('Ссылка скопирована в буфер обмена!', 'success');
        } catch (fallbackErr) {
            showNotification('Не удалось скопировать ссылку', 'error');
        }
        document.body.removeChild(textArea);
    }
}