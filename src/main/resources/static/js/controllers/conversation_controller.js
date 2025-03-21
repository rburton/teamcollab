import {Controller} from "@hotwired/stimulus"
import Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';

export default class extends Controller {
    static targets = ["chat", "input", "button", "status", "statusLabel"]

    connect() {
        const socket = new SockJS('http://localhost:8080/ws'); // Adjust endpoint as needed
        this.stompClient = Stomp.over(socket);
        this.stompClient.debug = () => {
        };

        // Set up MutationObserver to detect DOM changes in the chat container
        this.chatObserver = new MutationObserver((mutations) => {
            this.scrollToBottom();
        });

        // Start observing the chat container for DOM changes
        this.chatObserver.observe(this.chatTarget, {
            childList: true,  // Watch for changes to the direct children
            subtree: true     // Watch for changes to the entire subtree
        });

        this.buttonTarget.addEventListener('click', (e) => {
            const payload = JSON.stringify({
                'conversation_id': this.element.dataset.conversationChatIdValue,
                'content': this.inputTarget.value
            });
            this.inputTarget.value = "";
            this.stompClient.send('/app/chat.send', {}, payload);
        });

        this.stompClient.connect({}, (frame) => {
            this.online();
            this.stompClient.subscribe(`/user/queue/messages`, (messages) => {
                const response = JSON.parse(messages.body);
                if (this.isMessage(response)) {
                    const messages = response.payload;
                    for (let i = 0; i < messages.length; i++) {
                        const message = messages[i];
                        const element = this.createMessage(message);
                        this.chatTarget.appendChild(element);
                    }
                    this.scrollToBottom();
                } else if (this.isTurbo(response)) {
                    if (Array.isArray(response.payload)) {
                        for (let i = 0; i < response.payload.length; i++) {
                            Turbo.renderStreamMessage(response.payload[i]);
                        }
                    } else {
                        Turbo.renderStreamMessage(response.payload);
                    }
                    // We don't need to call scrollToBottom here anymore as the MutationObserver will handle it
                }
            });

            this.stompClient.subscribe(`/user/queue/assistants`, (assistants) => {
                console.log('assistant ', assistants);
            });
            const payload = JSON.stringify({
                'conversation_id': this.element.dataset.conversationChatIdValue,
                'content': this.inputTarget.value
            });
            this.stompClient.send('/app/chat.join', {}, payload);
        }, (error) => {
            this.offline();
            console.error('Connection error: ' + error);
        });

        this.stompClient.onclose = () => {
            this.offline();
        }

    }

    isMessage(response) {
        return response.messageType === 'MESSAGE';
    }

    isTurbo(response) {
        return response.messageType === 'TURBO';
    }

    offline() {
        this.statusTarget.classList.remove('text-green-500');
        this.statusTarget.classList.add('text-amber-500');
        this.statusLabelTarget.innerHTML = 'Disconnected'
    }

    online() {
        this.statusTarget.classList.remove('text-amber-500');
        this.statusTarget.classList.add('text-green-500');
        this.statusLabelTarget.innerHTML = 'Connected'
    }

    disconnect() {
        if (this.stompClient !== null) {
            this.stompClient.disconnect();
            console.log('Disconnected');
        }

        // Clean up the MutationObserver when the controller is disconnected
        if (this.chatObserver) {
            this.chatObserver.disconnect();
        }
    }

    scrollToBottom() {
        const chatContainer = document.getElementById('chatMessages');
        if (chatContainer) {
            chatContainer.scrollTop = chatContainer.scrollHeight;
        }
    }

    createMessage(message) {
        const container = document.createElement('div');
        var color = 'blue';

        if (message.username) {
            color = 'green';
            container.className = 'flex items-start justify-end';
        } else {
            container.className = 'flex items-start';
        }

        const userBadge = document.createElement('div');
        userBadge.className = `w-8 h-8 bg-${color}-500 rounded-full flex items-center justify-center text-white mr-2`;
        if (message.username) {
            userBadge.textContent = message.username.charAt(0);
        } else if (message.assistantName) {
            userBadge.textContent = message.assistantName.charAt(0);
        }
        container.appendChild(userBadge);

        const messageBox = document.createElement('div');

        messageBox.className = `bg-${color}-100 p-3 rounded-lg max-w-[70%]`;

        const nameElement = document.createElement('p');
        nameElement.className = `text-sm font-semibold text-${color}-800`;
        if (message.username) {
            nameElement.innerHTML = message.username;
        } else {
            nameElement.innerHTML = message.assistantName;
        }
        messageBox.appendChild(nameElement);

        const contentElement = document.createElement('p');
        contentElement.className = 'text-gray-800';
        contentElement.textContent = message.content;
        messageBox.appendChild(contentElement);

        const timestampElement = document.createElement('p');
        timestampElement.className = 'text-xs text-gray-500 mt-1';
        timestampElement.textContent = 'MMM dd, yyyy, hh:mm a';
        messageBox.appendChild(timestampElement);

        container.appendChild(messageBox);
        return container
    }
}
