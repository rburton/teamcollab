import * as Turbo from "@hotwired/turbo"

import {Application} from "@hotwired/stimulus"
import {definitionsFromContext} from "@hotwired/stimulus-webpack-helpers"

Turbo.start()

// Start Stimulus
const application = Application.start()
const context = require.context("./controllers", true, /\.js$/)
application.load(definitionsFromContext(context))
