// Функция для парсинга даты в формате 'dd.MM.yyyy HH:mm'
function parseHistoryDate(dateStr) {
    const [datePart, timePart] = dateStr.split(' ');
    const [day, month, year] = datePart.split('.');
    const [hours, minutes] = timePart.split(':');
    return new Date(year, month - 1, day, hours, minutes);
}

// Функция сортировки истории
function sortHistory(order) {
    const historyList = document.getElementById('history-content');
    const items = Array.from(historyList.querySelectorAll('.history-item'));

    items.sort((a, b) => {
        const dateA = parseHistoryDate(a.querySelector('.history-date').textContent);
        const dateB = parseHistoryDate(b.querySelector('.history-date').textContent);
        return order === 'desc' ? dateB - dateA : dateA - dateB;
    });

    // Очищаем и пересобираем список
    historyList.innerHTML = '';
    items.forEach(item => historyList.appendChild(item));

    // Обновляем активные кнопки
    document.querySelectorAll('.sort-btn').forEach(btn => {
        btn.classList.toggle('active',
            (order === 'desc' && btn.textContent.includes('новые')) ||
            (order === 'asc' && btn.textContent.includes('старые')));
    });

    // Обновляем URL без перезагрузки
    const url = new URL(window.location.href);
    url.searchParams.set('sortOrder', order);
    window.history.pushState({}, '', url);
}

// Инициализация при загрузке
document.addEventListener('DOMContentLoaded', function() {
    // Получаем параметр сортировки из URL
    const urlParams = new URLSearchParams(window.location.search);
    const sortOrder = urlParams.get('sortOrder') || 'desc';

    // Применяем сортировку
    sortHistory(sortOrder);

    // Остальная инициализация...
    updateTimeRemaining();
});

// Обработчик кнопки "Назад"
window.addEventListener('popstate', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const sortOrder = urlParams.get('sortOrder') || 'desc';
    sortHistory(sortOrder);
});