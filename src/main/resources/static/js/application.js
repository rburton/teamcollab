// Import Turbo
import * as Turbo from "@hotwired/turbo"

// Stimulus setup
import {Application} from "@hotwired/stimulus"
import {definitionsFromContext} from "@hotwired/stimulus-webpack-helpers"

// Start Turbo
Turbo.start()
Turbo.setFormMode("off")

document.addEventListener('turbo:before-fetch-request', (event) => {
    const token = document.querySelector('meta[name="csrf-token"]').content;
    event.detail.fetchOptions.headers['X-CSRF-Token'] = token;
});

document.addEventListener('turbo:click', (event) => {
    const link = event.target;
    if (link.dataset.turboMethod === 'post') {
        event.preventDefault();
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = link.href;
        form.style.display = 'none';
        const csrf = document.createElement('input');
        csrf.type = 'hidden';
        csrf.name = '_csrf';
        csrf.value = document.querySelector('meta[name="csrf-token"]').content;
        form.appendChild(csrf);
        document.body.appendChild(form);
        form.submit();
    }
});
// Start Stimulus
const application = Application.start()
const context = require.context("./controllers", true, /\.js$/)
application.load(definitionsFromContext(context))
