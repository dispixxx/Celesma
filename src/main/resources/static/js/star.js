// Функция для переключения состояния звездочки
function toggleStar(starIcon) {
    starIcon.classList.toggle("active"); // Переключаем класс "active"
    sortTasksByStar(); // Сортируем задачи
    saveStarState(starIcon); // Сохраняем состояние в localStorage
}

// Функция для сортировки задач по звездочке
function sortTasksByStar() {
    const taskLists = document.querySelectorAll(".task-list"); // Все списки задач
    taskLists.forEach(taskList => {
        const tasks = Array.from(taskList.querySelectorAll("li"));

        // Сортируем задачи: сначала с активной звездочкой, затем без
        tasks.sort((a, b) => {
            const aStar = a.querySelector(".star-icon").classList.contains("active");
            const bStar = b.querySelector(".star-icon").classList.contains("active");
            return bStar - aStar; // Сначала задачи с активной звездочкой
        });

        // Очищаем список и добавляем отсортированные задачи
        taskList.innerHTML = "";
        tasks.forEach(task => taskList.appendChild(task));
    });
}

// Функция для сохранения состояния звездочки в localStorage
function saveStarState(starIcon) {
    const taskItem = starIcon.closest("li");
    const taskId = taskItem.getAttribute("data-task-id"); // Используем ID задачи
    const isStarred = starIcon.classList.contains("active");

    // Сохраняем состояние в localStorage
    localStorage.setItem(`task-${taskId}-starred`, isStarred);
}

// Функция для восстановления состояния звездочек при загрузке страницы
function restoreStarState() {
    const taskLists = document.querySelectorAll(".task-list"); // Все списки задач
    taskLists.forEach(taskList => {
        const tasks = taskList.querySelectorAll("li");
        tasks.forEach(task => {
            const taskId = task.getAttribute("data-task-id"); // Используем ID задачи
            const isStarred = localStorage.getItem(`task-${taskId}-starred`) === "true";

            if (isStarred) {
                task.querySelector(".star-icon").classList.add("active");
            } else {
                task.querySelector(".star-icon").classList.remove("active");
            }
        });
    });

    sortTasksByStar(); // Сортируем задачи после восстановления состояния
}

// Восстанавливаем состояние звездочек при загрузке страницы
document.addEventListener("DOMContentLoaded", restoreStarState);