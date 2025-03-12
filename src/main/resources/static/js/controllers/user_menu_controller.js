import { Controller } from "@hotwired/stimulus"

export default class extends Controller {
  static targets = ["menu"]

  connect() {
    // Close the menu when clicking outside
    document.addEventListener('click', this.closeMenuOnClickOutside.bind(this))
  }

  disconnect() {
    document.removeEventListener('click', this.closeMenuOnClickOutside.bind(this))
  }

  toggle(event) {
    event.stopPropagation()
    this.menuTarget.classList.toggle('hidden')
  }

  closeMenuOnClickOutside(event) {
    if (!this.element.contains(event.target) && !this.menuTarget.classList.contains('hidden')) {
      this.menuTarget.classList.add('hidden')
    }
  }
}