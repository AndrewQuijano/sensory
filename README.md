# sensory
This repository contains the Swift 5 version of William Falcon's sensory iOS app.  

Also, it contains its Android counterpart which can collect the same features as sensory. The Android version can collect Wi-Fi Access Point MAC address and their corresponding RSSI signals for indoor localization.

The server code for both data collection and processing is located in this repository:
https://github.com/AndrewQuijano/Room_Level_Localization_Server.git  

## Installation
** Prerequisites
This guide will assume you have MySQL installed on your server. Also, it requires the following python packages:  

pandas  
sqlalchemy  
configparser  
flask  

** iOS Installation
This was sucessfully tested on the following versions of XCode and iPhones

XCode 11.3 -> iPhone 7 with iOS 12.3  
Xcode 12.3 -> iPhone 12 with iOS 14.3  

1. You need to set the IP Address of where the server will collect sensory's data. 
Edit the file:
iOSApp/MapTemplate/config/AppSettings.swift  
Replace <YOUR IP HERE> with the IP Address of your server.

2. Open Xcode, and open the iOS/911.xcworkspace file. Opening this file ensures all the cocoapods are loaded as expected.

3. Connect to your developer account. (This requires $99/year to Apple for personal account).  
* Go to Xcode -> Preferences  
* Sign in with your Apple ID that is a Developer Account  
* Click Manage Certificates, ensure you have your developer certificates installed
* Open 911.xcodeproj and go to Signing Capabilities tab for each entry and input yor developer account.
* Have XCode set the install target to be your phone and you will have sensory on your iOS device!  

** Android Installation
Please note the Android installation has only been tested on Samsung Galaxy 3. But it should work with all other Android devices.

Upon downloading the repository, open the sensory folder using Android Studio. Follow the instructions here on how to load an application onto your phone:
https://developer.android.com/training/basics/firstapp/running-app


## Usage
The application will open a world map and zoom in on your GPS coordinates. It will make a guess in which address you are located in.

At this point it will also every second send the sensor information to the designated server. 

On the very bottom, there is large red "Contact 911" button. This will switch to another ![Contact](https://github.com/AndrewQuijano/sensory/blob/master/images/contact.jpg) screen.

From here it will allow you to text 911 your address and indoor location information if you select "Text 911" option. Alternatively you can automatically call 911.

## Authors and Acknowledgment
Code Author: Andrew Quijano  
I would like to thank the Internet Real Time (IRT) lab at Columbia University for funding the work to upgrade sensory and building its Android counterpart.  

If you use this code please cite the following papers:  
Falcon, William, and Henning Schulzrinne. "Predicting Floor-Level for 911 Calls with Neural Networks and Smartphone Sensor Data." International Conference on Learning Representations. 2018.  

## License
[MIT](https://choosealicense.com/licenses/mit/)

## Project status
Currently the following issues need to resolved
* The Android version of sensory is still in progress of being refactored before being put on this repository
* iOS sensory can't collect Wi-Fi MAC Addresses and RSSI signal strength. This may prove to be near impossible as Apple have no equivalent to Wi-Fi manager class as in Android?
* Documentation needs improvement.
* Henning requested to investigate if sensory rssi feature is obtained from Wi-Fi or cell tower. Ensure rssi is from Wi-Fi and record in each row the MAC address of the AP.
* Henning requested to get the barometric pressure at sea level alongside the measurement from the sensors.

Currently, as I have a new iPhone device, this will require some further testing. 

