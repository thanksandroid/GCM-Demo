GCM-Demo
========

Android Push Notification with Google Cloud Messaging including PHP Server Code

http://thanksandroid.com/android-push-notifications-with-google-cloud-messaging-part-1/
http://thanksandroid.com/android-push-notifications-with-google-cloud-messaging-part-2/

In the GCMDemo project, set your project number or sender key in Constants.java
In PHP Server code, set your server API key in GCM.php

The demo app sends GCM registration id and message to server and receives it as GCM message from server.

In your app, you may want to save GCM registartion id in the database on your server and send message from server whenever required. 
