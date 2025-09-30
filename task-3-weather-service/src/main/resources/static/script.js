// Асинхронная функция для получения данных о погоде
async function getWeather() {
    // Получение значения из поля ввода города и убираем лишние пробелы
    const city = document.getElementById('cityInput').value.trim();
    // Получение элементов DOM для отображения результатов
    const resultDiv = document.getElementById('result');
    const chartContainer = document.getElementById('chartContainer');
    const loadingDiv = document.getElementById('loading');

    // Проверка, что пользователь ввел название города
    if (!city) {
        showError('Please enter a city name');
        return;
    }

    // Показ индикатора загрузки и очищаем предыдущие результаты
    loadingDiv.classList.remove('hidden');
    resultDiv.innerHTML = '';
    chartContainer.innerHTML = '';

    try {
        // Отправка GET-запроса на сервер для получения данных о погоде
        const response = await fetch('/weather?city=' + encodeURIComponent(city));
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || 'Failed to fetch weather data');
        }

        if (data.error) {
            showError(data.error);
            return;
        }

        displayWeatherData(data);

    } catch (error) {
        showError(error.message);
    } finally {
        loadingDiv.classList.add('hidden');
    }
}

// Функция для отображения данных о погоде
function displayWeatherData(data) {
    const resultDiv = document.getElementById('result');
    const chartContainer = document.getElementById('chartContainer');

    // Отображение основной информации о городе
    resultDiv.innerHTML = `
        <div class="city-name">Погода в ${data.city}</div>
        <div class="coordinates">Координаты: ${data.latitude.toFixed(4)}, ${data.longitude.toFixed(4)}</div>
    `;

    // Отображение графика температуры, если он доступен
    if (data.temperatureChart) {
        chartContainer.innerHTML = `
            <div class="chart-title">Прогноз температуры на 24 часа</div>
            <img src="data:image/png;base64,${data.temperatureChart}" 
                 alt="График температуры для ${data.city}" 
                 class="chart-image">
        `;
    } else {
        chartContainer.innerHTML = '<div class="error">График температуры недоступен</div>';
    }
}

// Функция для отображения ошибок
function showError(message) {
    const resultDiv = document.getElementById('result');
    // Показ сообщения об ошибке
    resultDiv.innerHTML = `<div class="error">Ошибка: ${message}</div>`;
    // Очистка контейнера с графиком
    document.getElementById('chartContainer').innerHTML = '';
}

// Добавление обработчиков событий после загрузки DOM
document.addEventListener('DOMContentLoaded', function() {
    // Обработчик для клавиши Enter в поле ввода
    document.getElementById('cityInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            getWeather();
        }
    });

    // Загрузка погоды для города по умолчанию
    getWeather();
});