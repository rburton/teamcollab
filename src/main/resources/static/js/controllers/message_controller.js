import {Controller} from "@hotwired/stimulus"
import showdown from "showdown"

export default class extends Controller {
    static targets = ["message"]

    connect() {
        const converter = new showdown.Converter();
        const markdown = this.element.dataset.messageContentValue;
        const html = converter.makeHtml(markdown);
        this.messageTarget.innerHTML = html;
    }
}
