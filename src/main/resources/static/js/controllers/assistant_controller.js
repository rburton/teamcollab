import {Controller} from "@hotwired/stimulus"

export default class extends Controller {
    static targets = ["modal"]
    static values = {
        selected: String,
        selectedName: String
    }

    connect() {
    }

    open() {
        this.modalTarget.classList.remove('hidden')
        this.modalTarget.classList.add('flex')
    }

    close() {
        this.modalTarget.classList.add('hidden')
        this.modalTarget.classList.remove('flex')
    }

}
