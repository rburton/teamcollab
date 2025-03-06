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
                'conversation_id': 1,
                'content': this.inputTarget.value
            });
            this.inputTarget.value = "";
            this.stompClient.send('/app/chat.send', {}, payload);
        });

        this.stompClient.connect({}, (frame) => {
            console.log('Connected: ' + frame);

            this.stompClient.subscribe('/topic/public', (message) => {
                console.log('Received message: ' + message.body);
            });

            this.stompClient.subscribe('/topic/messages', (messages) => {
                const listOfMessages = JSON.parse(messages.body);
                for (let i = 0; i < listOfMessages.length; i++) {
                    const message = listOfMessages[i];
                    const newElement = document.createElement("div");
                    newElement.textContent = message.content;
                    this.chatTarget.appendChild(newElement);
                }
                console.log('Received message: ' + messages.body);
            });

            // Send a message to a destination (e.g., '/app/send')
            this.stompClient.send('/app/chat.join', {}, JSON.stringify({
                'conversation_id': 1
            }));
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

}
