# TeamCollab

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