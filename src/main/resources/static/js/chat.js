document.addEventListener('DOMContentLoaded', function() {
    // Элементы чата
    const chatButton = document.getElementById('chatButton');
    const chatContainer = document.getElementById('chatContainer');
    const chatClose = document.getElementById('chatClose');
    const chatMessages = document.getElementById('chatMessages');
    const chatInput = document.getElementById('chatInput');
    const chatSend = document.getElementById('chatSend');
    const chatBadge = document.getElementById('chatBadge');

    // Переменные STOMP
    let stompClient = null;
    const projectId = project.id;
    const currentUser = user.username;
    const isMember = isMemberCheck.value;

    // Переменные для непрочитанных сообщений
    let unreadMessagesCount = 0;
    let isChatOpen = false;

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
            // Подписка на топик проекта
            stompClient.subscribe(`/topic/project-chat/${projectId}`, function(message) {
                const parsedMessage = JSON.parse(message.body);
                showMessage(parsedMessage);

                // Увеличиваем счетчик, если чат закрыт и сообщение не от текущего пользователя
                if (!isChatOpen && parsedMessage.senderUsername !== currentUser) {
                    incrementUnreadCount();
                }
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

    // Обновление счетчика непрочитанных сообщений
    function incrementUnreadCount() {
        unreadMessagesCount++;
        updateBadge();
    }

    function resetUnreadCount() {
        unreadMessagesCount = 0;
        updateBadge();
    }

    function updateBadge() {
        if (unreadMessagesCount > 0) {
            chatBadge.style.display = 'flex';
            chatBadge.textContent = unreadMessagesCount > 9 ? '9+' : unreadMessagesCount.toString();
        } else {
            chatBadge.style.display = 'none';
        }
    }

    // Обработчики событий
    chatButton.addEventListener('click', () => {
        chatContainer.style.display = 'flex';
        isChatOpen = true;
        resetUnreadCount();
    });

    chatClose.addEventListener('click', () => {
        chatContainer.style.display = 'none';
        isChatOpen = false;
    });

    chatSend.addEventListener('click', sendMessage);
    chatInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });

    // Закрытие соединения при разгрузке страницы
    window.addEventListener('beforeunload', () => {
        if (stompClient !== null) {
            stompClient.disconnect();
        }
    });
});