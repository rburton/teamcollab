import {Controller} from "@hotwired/stimulus"
import Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';

export default class extends Controller {
    static targets = ["chat", "input", "button"]

    connect() {
        const socket = new SockJS('http://localhost:8080/ws'); // Adjust endpoint as needed
        this.stompClient = Stomp.over(socket);
        this.stompClient.debug = () => {
        };

        this.buttonTarget.addEventListener('click', (e) => {
            const payload = JSON.stringify({
                'conversation_id': this.element.dataset.conversationChatIdValue,
                'content': this.inputTarget.value
            });
            this.inputTarget.value = "";
            this.stompClient.send('/app/chat.send', {}, payload);
        });

        this.stompClient.connect({}, (frame) => {
            this.stompClient.subscribe('/user/queue/messages', (messages) => {
                const listOfMessages = JSON.parse(messages.body);
                for (let i = 0; i < listOfMessages.length; i++) {
                    const message = listOfMessages[i];
                    const element = this.createMessage(message);
                    this.chatTarget.appendChild(element);
                }
            });
            this.stompClient.subscribe('/user/queue/personas', (personas) => {
                console.log('persons ', personas);
            });
            const payload = JSON.stringify({
                'conversation_id': this.element.dataset.conversationChatIdValue,
                'content': this.inputTarget.value
            });
            this.stompClient.send('/app/chat.join', {}, payload);
        }, (error) => {
            console.error('Connection error: ' + error);
        });

    }

    disconnect() {
        if (this.stompClient !== null) {
            this.stompClient.disconnect();
            console.log('Disconnected');
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
        } else if (message.personaName) {
            userBadge.textContent = message.personaName.charAt(0);
        }
        container.appendChild(userBadge);

        const messageBox = document.createElement('div');

        messageBox.className = `bg-${color}-100 p-3 rounded-lg max-w-[70%]`;

        const nameElement = document.createElement('p');
        nameElement.className = `text-sm font-semibold text-${color}-800`;
        if (message.username) {
            nameElement.innerHTML = message.username;
        } else {
            nameElement.innerHTML = message.personaName;
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
