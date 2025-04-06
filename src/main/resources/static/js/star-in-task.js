// Функция для переключения состояния звездочки
function toggleStar(starIcon) {
    starIcon.classList.toggle("active"); // Переключаем класс "active"
    saveStarState(starIcon); // Сохраняем состояние в localStorage
}

// Функция для сохранения состояния звездочки в localStorage
function saveStarState(starIcon) {
    const taskId = starIcon.getAttribute("data-task-id"); // Получаем ID задачи
    const isStarred = starIcon.classList.contains("active");

    // Сохраняем состояние в localStorage
    localStorage.setItem(`task-${taskId}-starred`, isStarred);
}

// Функция для восстановления состояния звездочки при загрузке страницы
function restoreStarState() {
    const starIcon = document.querySelector(".star-icon");
    if (!starIcon) return;

    const taskId = starIcon.getAttribute("data-task-id"); // Получаем ID задачи
    const isStarred = localStorage.getItem(`task-${taskId}-starred`) === "true"; // Проверяем состояние

    if (isStarred) {
        starIcon.classList.add("active"); // Закрашиваем звездочку
    }
}

// Восстанавливаем состояние звездочки при загрузке страницы
document.addEventListener("DOMContentLoaded", restoreStarState);