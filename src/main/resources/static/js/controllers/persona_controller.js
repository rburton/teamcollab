import {Controller} from "@hotwired/stimulus"

export default class extends Controller {
    static targets = ["modal", "list", "container"]
    static values = {
        selected: String,
        selectedName: String
    }

    getCsrfToken() {
        return {
            header: document.querySelector('meta[name="csrf-header"]').getAttribute('content'),
            token: document.querySelector('meta[name="csrf-token"]').getAttribute('content')
        }
    }

    connect() {
        this.loadPersonas()
    }

    open() {
        this.modalTarget.classList.remove('hidden')
        this.modalTarget.classList.add('flex')
        this.loadPersonas()
    }

    close() {
        this.modalTarget.classList.add('hidden')
        this.modalTarget.classList.remove('flex')
    }

    select(event) {
        const personaId = event.currentTarget.dataset.personaId
        const personaName = event.currentTarget.dataset.personaName
        this.selectedValue = personaId
        this.selectedNameValue = personaName

        // Update the conversation
        const csrf = this.getCsrfToken();
        fetch(`/api/conversations/${this.element.dataset.personaConversationIdValue}/persona`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrf.header]: csrf.token,
            },
            body: JSON.stringify({
                personaId
            })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok')
                }
                return response.json()
            })
            .then(() => {
                this.close()
            })
            .catch(error => {
                console.error('Error:', error)
                const errorDiv = document.getElementById('errorMessage')
                errorDiv.textContent = 'Failed to update persona'
                errorDiv.classList.remove('hidden')
                setTimeout(() => {
                    errorDiv.classList.add('hidden')
                }, 5000)
            })
    }

    loadPersonas() {
        const csrf = this.getCsrfToken();
        fetch('/api/personas/all', {
            headers: {
                [csrf.header]: csrf.token
            }
        })
            .then(response => response.json())
            .then(personas => {
                this.listTarget.innerHTML = ''
                personas.forEach(persona => {
                    const element = this.createPersonaElement(persona)
                    this.listTarget.appendChild(element)
                })
            })
            .catch(error => {
                console.error('Error loading personas:', error)
            })
    }

    createPersonaElement(persona) {
        const template = document.getElementById('personaTemplate')
        const element = template.content.cloneNode(true)
        const div = element.querySelector('div')
        div.dataset.personaId = persona.id
        div.dataset.personaName = persona.name
        div.dataset.action = "click->persona#select"

        const initial = element.querySelector('.rounded-full')
        initial.textContent = persona.name.charAt(0)

        const name = element.querySelector('h4')
        name.textContent = persona.name

        const expertise = element.querySelector('p')
        expertise.textContent = persona.expertises

        return element
    }
}
