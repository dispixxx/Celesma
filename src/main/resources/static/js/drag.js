

    // Добавляем функционал для перетаскивания задач
    document.addEventListener('DOMContentLoaded', function() {
    const taskCards = document.querySelectorAll('.task-card');
    const taskLists = document.querySelectorAll('.task-list');

    // Делаем задачи перетаскиваемыми
    taskCards.forEach(task => {
    task.setAttribute('draggable', 'true');

    task.addEventListener('dragstart', () => {
    task.classList.add('dragging');
});

    task.addEventListener('dragend', () => {
    task.classList.remove('dragging');
});
});

    // Обработка перетаскивания над колонками
    taskLists.forEach(list => {
    list.addEventListener('dragover', e => {
    e.preventDefault();
    const draggingTask = document.querySelector('.dragging');
    const afterElement = getDragAfterElement(list, e.clientY);

    if (afterElement == null) {
    list.appendChild(draggingTask);
} else {
    list.insertBefore(draggingTask, afterElement);
}
});

    list.addEventListener('drop', async (e) => {
    e.preventDefault();
    const draggingTask = document.querySelector('.dragging');
    const taskId = draggingTask.getAttribute('data-task-id');
    const newStatus = list.parentElement.classList.contains('new-column') ? 'NEW' :
    list.parentElement.classList.contains('in-progress-column') ? 'IN_PROGRESS' :
    list.parentElement.classList.contains('review-column') ? 'REVIEW' :
    list.parentElement.classList.contains('completed-column') ? 'COMPLETED' :
    list.parentElement.classList.contains('on-hold-column') ? 'ON_HOLD' : 'CANCELED';

    /*try {
        const response = await fetch(`/tasks/${taskId}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            },
            body: JSON.stringify({ status: newStatus })
        });

        if (!response.ok) {
            throw new Error('Ошибка при обновлении статуса задачи');
        }

        showToast('Статус задачи обновлен', 'success');
    } catch (error) {
        console.error('Ошибка:', error);
        showToast('Не удалось обновить статус задачи', 'error');
        location.reload(); // Перезагружаем страницу, чтобы вернуть предыдущее состояние
    }*/
});
});

    /*function getDragAfterElement(container, y) {
        const draggableElements = [...container.querySelectorAll('.task-card:not(.dragging)')];

        return draggableElements.reduce((closest, child) => {
            const box = child.getBoundingClientRect();
            const offset = y - box.top - box.height / 2;

            if (offset < 0 && offset > closest.offset) {
                return { offset: offset, element: child };
            } else {
                return closest;
            }
        }, { offset: Number.NEGATIVE_INFINITY }).element;
    }*/
});
