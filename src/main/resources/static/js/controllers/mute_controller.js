// Assistant Mute Controller
// This controller handles muting and unmuting assistants in a conversation

import { Controller } from "@hotwired/stimulus"

export default class extends Controller {
  static targets = ["muteToggle"]

  connect() {
    this.element.querySelectorAll('.mute-toggle').forEach(button => {
      button.addEventListener('click', this.toggleMute.bind(this))
    })
  }

  toggleMute(event) {
    const button = event.currentTarget
    const assistantId = button.dataset.assistantId
    const conversationId = button.dataset.conversationId
    const isMuted = button.dataset.muted === 'true'
    
    const endpoint = isMuted 
      ? `/api/assistants/${assistantId}/conversations/${conversationId}/unmute`
      : `/api/assistants/${assistantId}/conversations/${conversationId}/mute`
    
    fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    .then(response => {
      if (response.ok) {
        // Update button state
        button.dataset.muted = (!isMuted).toString()
        
        // Update button text
        button.textContent = isMuted ? 'Mute' : 'Unmute'
        
        // Update button style
        if (isMuted) {
          button.classList.remove('bg-green-500', 'hover:bg-green-600')
          button.classList.add('bg-red-500', 'hover:bg-red-600')
        } else {
          button.classList.remove('bg-red-500', 'hover:bg-red-600')
          button.classList.add('bg-green-500', 'hover:bg-green-600')
        }
      } else {
        console.error('Failed to toggle mute state')
      }
    })
    .catch(error => {
      console.error('Error toggling mute state:', error)
    })
  }
}