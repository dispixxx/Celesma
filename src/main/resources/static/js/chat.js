// Чат
document.addEventListener('DOMContentLoaded', function() {
    const chatButton = document.getElementById('chatButton');
    const chatContainer = document.getElementById('chatContainer');
    const chatClose = document.getElementById('chatClose');
    const chatMessages = document.getElementById('chatMessages');
    const chatInput = document.getElementById('chatInput');
    const chatSend = document.getElementById('chatSend');
    const projectId = [[${project.id}]];
    const currentUser = [[${#authentication.name}]];

    // Подключение к WebSocket
    const socket = new WebSocket(`ws://${window.location.host}/ws/project-chat/${projectId}`);

    // Открытие/закрытие чата
    chatButton.addEventListener('click', () => {
        chatContainer.style.display = 'flex';
    });

    chatClose.addEventListener('click', () => {
        chatContainer.style.display = 'none';
    });

    // Отправка сообщения
    function sendMessage() {
        const message = chatInput.value.trim();
        if (message) {
            const chatMessage = {
                projectId: projectId,
                sender: currentUser,
                content: message,
                timestamp: new Date().toISOString()
            };

            socket.send(JSON.stringify(chatMessage));
            chatInput.value = '';
        }
    }

    chatSend.addEventListener('click', sendMessage);
    chatInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });

    // Обработка входящих сообщений
    socket.onmessage = function(event) {
        const message = JSON.parse(event.data);
        addMessageToChat(message);
    };

    // Добавление сообщения в чат
    function addMessageToChat(message) {
        const isCurrentUser = message.sender === currentUser;
        const messageElement = document.createElement('div');
        messageElement.classList.add('message', isCurrentUser ? 'message-sent' : 'message-received');

        const time = new Date(message.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

        messageElement.innerHTML = `
        <div>${message.content}</div>
        <div class="message-info">
          ${isCurrentUser ? 'Вы' : message.sender} • ${time}
        </div>
      `;

        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    // Загрузка истории сообщений
    fetch(`/api/projects/${projectId}/chat-messages`)
        .then(response => response.json())
        .then(messages => {
            messages.forEach(message => addMessageToChat(message));
        })
        .catch(error => console.error('Ошибка загрузки истории чата:', error));
});