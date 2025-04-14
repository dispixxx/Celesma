document.addEventListener('DOMContentLoaded', function() {
    // Элементы чата
    const chatButton = document.getElementById('chatButton');
    const chatContainer = document.getElementById('chatContainer');
    const chatClose = document.getElementById('chatClose');
    const chatMessages = document.getElementById('chatMessages');
    const chatInput = document.getElementById('chatInput');
    const chatSend = document.getElementById('chatSend');

    // Переменные STOMP
    let stompClient = null;
    const projectId = project.id;
    const currentUser = user.username;
    const isMember = isMemberCheck.value;

    // Показывать/скрывать кнопку чата в зависимости от прав
    if (!isMember) {
        chatButton.style.display = 'none';
    } else {
        initWebSocket();
    }

    // Инициализация соединения
    function initWebSocket() {
        const socket = new SockJS('/ws/project-chat');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function(frame) {
            // console.log('Connected: ' + frame);

            // Подписка на топик проекта
            stompClient.subscribe(`/topic/project-chat/${projectId}`, function(message) {
                showMessage(JSON.parse(message.body));
            });

            // Загрузка истории сообщений
            fetchChatHistory();
        }, function(error) {
            console.error('STOMP error: ', error);
        });
    }

    // Отправка сообщения
    function sendMessage() {
        const content = chatInput.value.trim();
        if (content && stompClient) {
            const chatMessage = {
                content: content
            };

            stompClient.send(`/app/project-chat/${projectId}`, {}, JSON.stringify(chatMessage));
            chatInput.value = '';
        }
    }

// Отображение сообщения
    function showMessage(message) {
        const isCurrentUser = message.senderUsername === currentUser;
        const messageElement = document.createElement('div');
        messageElement.classList.add('message', isCurrentUser ? 'message-sent' : 'message-received');

        const time = new Date(message.timestamp).toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit'
        });

        messageElement.innerHTML = `
        <div class="message-content">${message.content}</div>
        <div class="message-info">
            <span class="sender-name">${isCurrentUser ? 'Вы' : message.senderUsername}</span>
            <span class="message-time">${time}</span>
        </div>
    `;

        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    // Загрузка истории сообщений
    function fetchChatHistory() {
        fetch(`/api/projects/${projectId}/chat-messages`)
            .then(response => response.json())
            .then(messages => {
                messages.forEach(message => showMessage(message));
            })
            .catch(error => console.error('Ошибка загрузки истории чата:', error));
    }

    // Обработчики событий
    chatButton.addEventListener('click', () => {
        chatContainer.style.display = 'flex';
    });

    chatClose.addEventListener('click', () => {
        chatContainer.style.display = 'none';
    });

    chatSend.addEventListener('click', sendMessage);
    chatInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });

    // Инициализация WebSocket
    // initWebSocket();

    // Закрытие соединения при разгрузке страницы
    window.addEventListener('beforeunload', () => {
        if (stompClient !== null) {
            stompClient.disconnect();
        }
    });
});