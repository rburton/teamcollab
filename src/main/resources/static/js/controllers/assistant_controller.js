import {Controller} from "@hotwired/stimulus"

export default class extends Controller {
    static targets = ["modal"]
    static values = {
        selected: String,
        selectedName: String,
        conversationId: String
    }

    connect() {
        // Listen for successful assistant additions
        document.addEventListener('turbo:frame-render', (event) => {
            const frame = event.target;
            if (frame.id === 'assistantPanelList') {
                // Close the modal after selection
                this.close();
            }
        });

        // Listen for turbo:submit-end events on assistant links
        document.addEventListener('turbo:submit-end', (event) => {
            const response = event.detail.fetchResponse;

            // Check if this is a response from adding an assistant
            if (response.location.includes('/assistants/') && response.location.includes('/conversations/')) {
                // Show a notification
                this.showNotification('Assistant added successfully!');
            }
        });
    }

    open() {
        this.modalTarget.classList.remove('hidden')
        this.modalTarget.classList.add('flex')
    }

    close() {
        this.modalTarget.classList.add('hidden')
        this.modalTarget.classList.remove('flex')
    }

    showNotification(message) {
        const notifications = document.getElementById('notifications');
        if (!notifications) return;

        const notification = document.createElement('div');
        notification.className = 'bg-green-100 border-l-4 border-green-500 text-green-700 p-4 rounded shadow-md mb-3';
        notification.style.animation = 'fadeInOut 5s forwards';

        notification.innerHTML = `
            <div class="flex items-center">
                <div class="py-1 mr-3">
                    <i class="fas fa-check-circle text-green-500"></i>
                </div>
                <div>
                    <p class="font-medium">Success!</p>
                    <p class="text-sm">${message}</p>
                </div>
            </div>
        `;

        notifications.appendChild(notification);

        // Remove notification after animation completes
        setTimeout(() => {
            notification.remove();
        }, 5000);
    }
}
