# UnlimitedOnDemand Auto Reply

This simple Android app was created with one specific purpose in mind: to automatically respond to incoming SMS messages with a customizable text.
As the app's name suggests, it's designed to automatically reply to those persistent SMS messages from your mobile network provider, which you need
to respond to in order to maintain high-speed data connection.

Due to limitations in modern Android, where receiving SMS in the background is not possible unless the app is set as the default SMS app, this
application utilizes notifications to check for incoming messages.
*This means the app will only function if your default SMS app provides notifications for incoming messages.*

When a notification from a specific app (ideally your default SMS app) with a specific title and text containing a predefined string is detected,
the app automatically sends an SMS with a customizable message to a designated number.

<img alt="Screenshot_20250504_134921_UnlimitedOnDemand Auto Reply" src="https://github.com/user-attachments/assets/9ca4ba17-42b1-414b-9ff1-4e6e584b5fb5" width="300" />

## Needed Permissions

### Send SMS
As the primary function of the app is to send an SMS, it naturally requires the necessary permission to do so.

### Post Notifications
If a notification matching the configured parameters is detected and an SMS is sent, the app will notify the user with a notification.
This permission is not required for the core functionality of the app, but it helps verify that everything is working as expected.

### Be a notification listener
The app must be manually configured to have permission to listen for notifications. Once enabled, the service runs in the background and performs its task;
otherwise, the app remains inactive. This allows you to turn the service on or off as needed.
