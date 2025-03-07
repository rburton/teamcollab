import {Controller} from "@hotwired/stimulus"

export default class extends Controller {
    static targets = ["message"]

    connect() {
        this.messageTarget.innerHTML = this.element.dataset.messageContentValue;
    }


}
