<div style="width:100%">
	<div style="width:50%; display:inline-block">
		<p align="center">
         <img align="center" src="https://avatars2.githubusercontent.com/u/45484907?s=200&v=4"/>
		</p>	
	</div>	
</div>

# Android Java Push Notification App

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](#)
[![Platform](https://img.shields.io/badge/Language-Java-yellowgreen.svg)](#)
![GitHub repo size](https://img.shields.io/github/repo-size/cometchat-pro/android-java-chat-push-notification-app)
![GitHub contributors](https://img.shields.io/github/contributors/cometchat-pro/android-java-chat-push-notification-app)
![GitHub stars](https://img.shields.io/github/stars/cometchat-pro/android-java-chat-push-notification-app?style=social)
![Twitter Follow](https://img.shields.io/twitter/follow/cometchat?style=social)

<img align="left" src="https://raw.githubusercontent.com/cometchat-pro/android-java-chat-push-notification-app/master/ConnectionService%20%2B%20Firebase/Screenshot/screenshot.png">


CometChat Push Notification Sample App is a fully functional push notification app capable of one-on-one (private) and group messaging as well as Calling. This sample app enables users to send and receive push notifications for text and multimedia messages like **images, videos, documents as well as  Custom Messages** . Also, users can make push notifications for Audio and Video calls to other users or groups.

<hr/>

## Table of Contents

1. [Pre-requisite](#Pre-requisite)

2. [Installation ](#installtion)

3. [Run the Sample App ](#run-the-sample-app)

4. [Documentation](#documentation)

5. [Contribute](#contribute)



## Pre-requisite :star:
1. Login to the <a href="https://app.cometchat.io/" target="_blank">CometChat Dashboard</a>.
2. Select an existing app or create a new one.
3. Go to "API & Auth Keys" section and copy the `REST API` key from the "REST API Keys" tab.
4. Go to the "Extensions" section and Enable the Push Notifications extension.
5. Go to the "Installed" tab in the same section and open the settings for this extension and Set the version to `V2`.
6. Also, save the `FCM Server key` in the Settings and click on Save.
7. Copy the `APP_ID`, `REGION` and `AUTH_KEY` for your app.
</br>

</br>
 
 
## Installation :wrench:

   Simply Clone the project from android-java-push-notifications-app repository and open in Android Studio.
   Build the Demo App and it will be ready to Run



## Run the Sample App

To Run to sample app you have to do the following changes by Adding **APP_ID**, **AUTH_KEY** , **REGION** and **google-services.json**

   You can obtain your  *APP_ID*, *AUTH_KEY* and *REGION* from [CometChat-Pro Dashboard](https://app.cometchat.io/)

   You can Obtain your  *google-services.json* from [Firebase Console](https://console.firebase.google.com/)

   - Open the project in Android Studio.

   - Modify `APP_ID` and `AUTH_KEY` and `REGION` with your own .
   
   - Add `google-services.json` in **app** directory

   - Select demo users or enter the **UID** at the time of login once the app is launched.

<img align="center" width="100%" height="auto"
src="https://github.com/cometchat-pro-samples/android-java-chat-app/blob/master/Screenshot/Screen%20Shot%202020-01-30%20at%206.39.08%20PM.png">

Build and run the Sample App.
       

## Note
  
   You can Obtain your  *APP_ID* and *API_KEY* from [CometChat-Pro Dashboard](https://app.cometchat.com/)

   For more information read [CometChat-Pro Android SDK](https://prodocs.cometchat.com/docs/android-quick-start)
   Documentation

   You can Obtain your  *google-services.json* from [Firebase Console](https://console.firebase.google.com/)


## 📝 Documentation

We are providing two ways to implement push notification for your app.

 1. [Firebase](https://prodocs.cometchat.com/docs/android-extensions-enhanced-push-notification)

 2. [ConnectionService + Firebase](https://prodocs.cometchat.com/docs/android-push-notification-with-connectionservice) 

Please refer our documentation to intergrate push notification inside your app.   

## Contributors

Thanks to the following people who have contributed to this project:

[👨‍💻 @darshanbhanushali 💻](https://github.com/darshanbhanushali) <br>
[👨‍💻 @yadavmangesh 💻](https://github.com/yadavmangesh) <br>
[👨‍💻 @prathamesh-majgaonkar 💻](https://github.com/prathamesh-majgaonkar) <br>

[Contribution guidelines for this project](CONTRIBUTING.md)

---

## :mailbox: Contact

Contact us via real time support present in [CometChat Dashboard.](https://app.cometchat.io/)

---

## License

This project uses the following license: [License.md](LICENSE).
