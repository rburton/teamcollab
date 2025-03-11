import {Controller} from "@hotwired/stimulus"

export default class extends Controller {
    static targets = []

    connect() {
        this.element.addEventListener('click', this.toggleBookmark.bind(this))
        this.updateButtonStyle()
    }

    disconnect() {
        this.element.removeEventListener('click', this.toggleBookmark.bind(this))
    }

    async toggleBookmark(event) {
        event.preventDefault()

        const messageId = this.element.dataset.messageId
        const conversationId = this.element.dataset.conversationId
        const isBookmarked = this.element.classList.contains('bookmark-active')

        const endpoint = isBookmarked
            ? `/conversations/${conversationId}/messages/${messageId}/unbookmark`
            : `/conversations/${conversationId}/messages/${messageId}/bookmark`

        try {
            const response = await fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            })

            if (response.ok) {
                this.element.classList.toggle('bookmark-active')
                this.updateButtonStyle()

                // Show notification
                const action = isBookmarked ? 'removed from' : 'added to'
                this.showNotification(`Message ${action} bookmarks`)
            } else {
                const errorData = await response.json()
                this.showNotification(`Error: ${errorData.message || 'Failed to update bookmark'}`, true)
            }
        } catch (error) {
            console.error('Error toggling bookmark:', error)
            this.showNotification('Error: Could not connect to server', true)
        }
    }

    updateButtonStyle() {
        if (this.element.classList.contains('bookmark-active')) {
            this.element.classList.remove('text-gray-400')
            this.element.classList.add('text-yellow-500')
        } else {
            this.element.classList.remove('text-yellow-500')
            this.element.classList.add('text-gray-400')
        }
    }

    showNotification(message, isError = false) {
        const notificationsContainer = document.getElementById('notifications')
        if (!notificationsContainer) return

        const notification = document.createElement('div')
        notification.className = `p-3 rounded-lg shadow-md mb-2 ${isError ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'}`
        notification.style.animation = 'fadeInOut 4s forwards'
        notification.textContent = message

        notificationsContainer.appendChild(notification)

        // Remove notification after animation completes
        setTimeout(() => {
            notification.remove()
        }, 4000)
    }
}