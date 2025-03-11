// Assistant Tone Controller
// This controller handles changing the tone of assistants in a conversation

import { Controller } from "@hotwired/stimulus"

export default class extends Controller {
  static targets = ["modal", "toneOption"]
  static values = {
    assistantId: String,
    conversationId: String,
    currentTone: String
  }

  connect() {
    // Initialize controller
  }

  openModal(event) {
    // Get assistant and conversation IDs from the clicked button
    const button = event.currentTarget
    this.assistantIdValue = button.dataset.assistantId
    this.conversationIdValue = button.dataset.conversationId
    this.currentToneValue = button.dataset.tone || 'FORMAL'
    
    // Set the current tone in the modal
    this.toneOptionTargets.forEach(option => {
      if (option.value === this.currentToneValue) {
        option.checked = true
      }
    })
    
    // Show the modal
    this.modalTarget.classList.remove('hidden')
    this.modalTarget.classList.add('flex')
  }

  closeModal() {
    // Hide the modal
    this.modalTarget.classList.add('hidden')
    this.modalTarget.classList.remove('flex')
  }

  saveTone() {
    // Get the selected tone
    const selectedTone = this.toneOptionTargets.find(option => option.checked)?.value
    
    if (!selectedTone) {
      this.showError('Please select a tone')
      return
    }
    
    // Save the tone via API
    const endpoint = `/api/assistants/${this.assistantIdValue}/conversations/${this.conversationIdValue}/tone`
    
    fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: `tone=${selectedTone}`
    })
    .then(response => {
      if (response.ok) {
        // Update the button's data-tone attribute
        const button = document.querySelector(`.tone-button[data-assistant-id="${this.assistantIdValue}"][data-conversation-id="${this.conversationIdValue}"]`)
        if (button) {
          button.dataset.tone = selectedTone
        }
        
        // Close the modal
        this.closeModal()
        
        // Show a success notification
        this.showNotification(`Tone changed to ${this.getToneDisplayName(selectedTone)}`)
      } else {
        this.showError('Failed to change tone')
      }
    })
    .catch(error => {
      console.error('Error changing tone:', error)
      this.showError('An error occurred while changing the tone')
    })
  }
  
  getToneDisplayName(tone) {
    const toneMap = {
      'FORMAL': 'Formal',
      'CASUAL': 'Casual',
      'TECHNICAL': 'Technical',
      'SIMPLIFIED': 'Simplified'
    }
    return toneMap[tone] || tone
  }
  
  showError(message) {
    const errorElement = document.getElementById('toneErrorMessage')
    if (errorElement) {
      errorElement.textContent = message
      errorElement.classList.remove('hidden')
      
      // Hide the error after 3 seconds
      setTimeout(() => {
        errorElement.classList.add('hidden')
      }, 3000)
    }
  }
  
  showNotification(message) {
    const notifications = document.getElementById('notifications')
    if (!notifications) return
    
    const notification = document.createElement('div')
    notification.className = 'bg-green-100 border-l-4 border-green-500 text-green-700 p-4 rounded shadow-md mb-3'
    notification.style.animation = 'fadeInOut 5s forwards'
    
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
    `
    
    notifications.appendChild(notification)
    
    // Remove notification after animation completes
    setTimeout(() => {
      notification.remove()
    }, 5000)
  }
}