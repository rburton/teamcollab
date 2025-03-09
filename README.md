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
<div data-controller="assistant">
  <button data-action="click->assistant#open">Open Modal</button>
  <div data-assistant-target="modal">
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

https://teamai.com/blog/large-language-models-llms/understanding-different-chatgpt-models/
https://buildpad.io/
https://josephthacker.com/hacking/2025/02/25/how-to-hack-ai-apps.html
https://grok.com/chat/25045acb-0a42-4516-9feb-98bc57c50a06
https://grok.com/chat/2c5496df-8b0e-46e9-aa6e-1687d8c32405
https://grok.com/chat/685f46a0-cc65-4c45-b03b-4f5387cfb983
https://grok.com/chat/d392c3ee-bdc6-46ff-b7f9-19f034401185
https://grok.com/chat/5a14a7dd-f377-4a30-a1db-c82110e49fb2
https://grok.com/chat/75dcaded-9f4f-4077-abfc-0e0c6bbfef65
https://grok.com/chat/75dcaded-9f4f-4077-abfc-0e0c6bbfef65
https://grok.com/chat/75dcaded-9f4f-4077-abfc-0e0c6bbfef65
https://grok.com/chat/2f7a5821-963f-494f-bd18-eba987489b7a
https://grok.com/chat/5b680ad2-0b45-4ac7-9334-286e5f5616b5
https://grok.com/chat/48f7fa8d-7438-4909-af7a-8286ee5deeb4
https://grok.com/chat/4ce26a72-df7c-4576-a8f1-a3f9c1d84256
https://grok.com/chat/75dcaded-9f4f-4077-abfc-0e0c6bbfef65
https://x.com/i/grok?conversation=1892295159951302905
https://x.com/tedx_ai/status/1893464426071589210


https://grok.com/chat/5bf3919f-fe23-4354-81e4-c63cd9b9db18
https://grok.com/chat/1d83a1f7-539a-4cfe-875c-ad0cfd55edc4

https://saashammer.com/blog/send-turbo-stream-over-websocket/
