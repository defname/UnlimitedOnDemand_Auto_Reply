# UnlimitedOnDemand Auto Reply

This simple Android app was created with one specific purpose in mind: to automatically respond to incoming SMS messages with a customizable text.
As the app's name suggests, it's designed to automatically reply to those persistent SMS messages from your mobile network provider, which you need
to respond to in order to maintain high-speed data connection.

Due to limitations in modern Android, where receiving SMS in the background is not possible unless the app is set as the default SMS app, this
application utilizes notifications to check for incoming messages.
*This means the app will only function if your default SMS app provides notifications for incoming messages.*

When a notification from a specific app (ideally your default SMS app) with a specific title and text containing a predefined string is detected,
the app automatically sends an SMS with a customizable message to a designated number.

Please be aware that your network operator may prohibit the use of such automation in their terms and conditions. To make the automation less noticeable, the app will introduce a randomized delay before sending the reply, with the delay time customizable within a specified range.

I should also note that while this app can automate the described task, it should be regarded strictly as a testing tool and a proof of concept. Surely, no one should rebel against the intentions of network operators...

<img alt="Screenshot_20250504_134921_UnlimitedOnDemand Auto Reply" src="https://github.com/user-attachments/assets/9ca4ba17-42b1-414b-9ff1-4e6e584b5fb5" width="300" />

## Needed Permissions

### Send SMS
As the primary function of the app is to send an SMS, it naturally requires the necessary permission to do so.

### Post Notifications
If a notification matching the configured parameters is detected and an SMS is sent, the app will notify the user with a notification.
This permission is not required for the core functionality of the app, but it helps verify that everything is working as expected.

### Notification Listener Access
The app must be manually configured to have permission to listen for notifications. Once enabled, the service runs in the background and performs its task;
otherwise, the app remains inactive. This allows you to turn the service on or off as needed.

## Installation

If you trust me, you can simply download the APK from the [releases section](https://github.com/defname/UnlimitedOnDemand_Auto_Reply/releases) and install it on your Android device (you should not trust random people on the internet though).

Alternatively get Android Studio running, clone the repository and build it yourself. The sourcecode is not really that complicated and reviewing it should not be too hard
(there is just the main activity, and the notification listener).

## Usage
After installing the app, some configuration is required.

### Give Permissions
Tap the first two buttons and confirm the dialogs. This grants the app permission to send SMS and post notifications.

### Enable the Notification Listener Service
Tap the third button and select this app from the list. Ensure it has all required permissions, especially to read messaging notifications.
This is how the service is enabled and where you can disable it again.

### Set the Criteria to Identify the Relevant Notification

- **App Package:** Select your default SMS app (use the fourth button for automatic detection).
- **Notification Title:** Typically, the sender's number (e.g., your provider).
- **Notification Text:** A unique keyword or phrase from the SMS body is sufficient.

### Configure the outgoing SMS

- **Target Number:** Usually the same as the sender's number.
- **Reply Text:** The message you want to send back (e.g., "2" to request 2 GB).
- **Delay Range:** To avoid looking automated, the app waits a random number of seconds within this range before replying.

When the app is configured and the service is running, the first outgoing SMS still requires manual confirmation.  
However, you can choose to allow all future messages to be sent automatically.  
From that point on, the app will operate without any further user interaction.

## License
This project is licensed under the GNU General Public License v3.0 or later – see the [LICENSE](LICENSE) file for details.
