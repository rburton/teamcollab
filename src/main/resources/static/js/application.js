// Import Turbo
import * as Turbo from "@hotwired/turbo"

// Stimulus setup
import {Application} from "@hotwired/stimulus"
import {definitionsFromContext} from "@hotwired/stimulus-webpack-helpers"

// Start Turbo
Turbo.start()
// Turbo.setFormMode("off")

// Start Stimulus
const application = Application.start()
const context = require.context("./controllers", true, /\.js$/)
application.load(definitionsFromContext(context))
