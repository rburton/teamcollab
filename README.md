# TeamCollab
- https://www.pulumi.com/aws/
- 
## Frontend Setup

### Prerequisites
- Node.js (v14 or higher)
- npm (v6 or higher)

### Stimulus.js Setup
1. Install dependencies:
```bash
npm install
```

2. Build JavaScript assets:
```bash
npm run build
```

The build process will:
- Bundle all JavaScript files
- Process Stimulus.js controllers
- Output the bundled file to `src/main/resources/static/dist/application.js`

### Development
- Stimulus.js controllers are located in `src/main/resources/static/js/controllers/`
- Main application entry point is `src/main/resources/static/js/application.js`
- After making changes to JavaScript files, rebuild assets using `npm run build`

### Adding New Stimulus Controllers
1. Create a new controller file in `src/main/resources/static/js/controllers/`
2. Follow the Stimulus.js controller naming convention: `controller_name_controller.js`
3. Import and register the controller in `application.js` (automatic with current setup)
4. Add corresponding data attributes to HTML templates:
   - `data-controller="controller-name"`
   - `data-action="event->controller-name#method"`
   - `data-controller-name-target="targetName"`

### Example Controller Usage
```html
<div data-controller="persona">
  <button data-action="click->persona#open">Open Modal</button>
  <div data-persona-target="modal">
    <!-- Modal content -->
  </div>
</div>
```

https://www.writesoftwarewell.com/process-turbo-stream-javascript/


Create a pulumi script that does the following:

- Creates a RDS Postgres Instance
- ECR for deploying Docker images
- Secruity groups for protecting the network
- Fargate for running the application
- Update the Github flows so that it can be invoked on each merge in to main to do a release

Make it so a environment name can be provided so a dev and production enviroment can be setup or future environments.

Do this in Java
