
This repository contains the Swift 5 version of William Falcon's sensory iOS app.  

Also, it contains its Android counterpart which can collect the same features as sensory. The Android version can collect Wi-Fi Access Point MAC address and their corresponding RSSI signals for indoor localization.

The server.py code is necessary to collect the data from iOS version of sensory.
The eclipse project in the *LocalizationServer* directory has the Java code to both collect and process the sensor data.

## Installation
**Prerequisites**:  
1. Please create a *database.properties* file. This file will be read by the server code for setting up the database. The content should be:  
[Database]
user=|Username|  
password=|User password|  
schema=|database/schema to store sensory|  
table=|table storing iOS sensory data|  
android=|table starting Android sensory data|  

2. Be sure to set up port forwarding for your Server IP address for the phone to successfully send data.

3. Please install Python and the necessary packages on the *requirements.txt* file. You can use the following to install:  
**pip install -r requirements.txt**

4. Be sure to have an installation of Java on your server to run the code to interact with the Android version of sensory and print/pre-process data.

5. Please install MySQL and ensure the MySQL service is running. Please run version 5.5+ as it can support Unicode UTF-8 SSID names.


Note:  
* Please update the JDBC driver as necessary:  
https://www.mysql.com/products/connector/
* The android.jar file (SDK 30.3) located in the Eclipse project was obtained from:  
https://androidsdkmanager.azurewebsites.net/SDKPlatform
* The current installation assumes port 9254 for Andriod sensory server and 3000 for the iOS sensory server. You can feel free to modify this.
* You can extract the Server Code from **sensory_server_v1.zip** located in the root directory, with a partially completed database.properties file.

**iOS Installation**  
This was successfully tested on the following versions of XCode and iPhones

XCode 11.3 -> iPhone 7 with iOS 12.3  
Xcode 12.3 -> iPhone 12 with iOS 14.3  

1. You need to set the IP Address of where the server will collect sensory data. 
Edit the file:  
iOSApp/MapTemplate/config/AppSettings.swift  
Replace |YOUR IP HERE| with  
**https://SERVER-IP:PORT-NUMBER**

2. Open Xcode, and open the iOS/911.xcworkspace file. Opening this file ensures all the cocoa pods are loaded as expected.

3. Connect to your developer account. (This requires $99/year to Apple for a personal account).  
* Go to Xcode -> Preferences  
* Sign in with your Apple ID that is a Developer Account  
* Click Manage Certificates, ensure you have your developer certificates installed
* Open 911.xcodeproj and go to the Signing Capabilities tab for each entry and input your developer account.
* Have XCode set the install target to be your phone and you will have sensory on your iOS device!  

**Android Installation**  
This was successfully tested on a Samsung Galaxy 3 Android Phone and using Android Studio 4.1.2

Upon downloading the repository, open the *sensory* folder on Android Studio. Follow the instructions here on how to load an application onto your phone:  
https://developer.android.com/training/basics/firstapp/running-app

To set the IP Address and port number edit:  
*CollectionFragment.java*

On Lines 54/55, place the IP Address and Port number of the Android sensory's server.

**API Keys**  
This installation requires two API keys:

1. Google Map

The following file uses a Maps SDK for Android API Key:  
sensory/app/src/main/res/values/strings.xml

This API Key is required for the GPS localization/Google Maps in Android sensory. Also, this is required for the GPS Altimeter to work correctly as well. 
Please follow instructions here on obtaining your own API Key:  
https://developers.google.com/maps/documentation/android-sdk/get-api-key

2. OpenWeather API

The following file requires an OpenWeather API Key:  
sensory/app/src/main/java/columbia/irt/sensors/BarometricAltimeter.java

To obtain an API Key, create an account at this website:  
https://openweathermap.org/api

This API allows for free occasional queries to obtain barometric pressure readings from nearby weather stations. This would permit more accurate barometric altitude readings.

NOTE: There are plenty of other sensors implemented, but not used in Android's version of sensory (e. g. Bluetooth, temperature, etc.). While it won't be used, I figure it would be convenient to provide code should others need an implementation to start from.

## Usage - iOS
The application will open a world map and zoom in on your GPS coordinates. It will make a guess in which address you are located in the [Main Menu](https://github.com/AndrewQuijano/sensory/blob/master/images/main_menu.jpg)

At this point, it will also every second send the sensor information to the designated server. 

On the very bottom, there is a large red "Contact 911" button. This will switch to another [Contact](https://github.com/AndrewQuijano/sensory/blob/master/images/contact.jpg) screen.

From here it will allow you to text 911 your address and indoor location information if you select the "Text 911" option. Alternatively, you can automatically call 911.

## Usage - Android

[Android_Map](https://github.com/AndrewQuijano/sensory/blob/master/images/android_map.png)
screen appears on booting the Android sensory app, you will see a world map, which will zoom into your current location. 

[Android_Collection](https://github.com/AndrewQuijano/sensory/blob/master/images/android_collection.png)

If you need to label the data click the *Collection Settings* button. The off switch corresponds to a 0 value and on corresponds to a 1 value for Indoors, Center of Room, and Start/Stop scanning.

The first text box corresponds to the "env_context" field in the features.  

The second text box corresponds to the Room label, where the users should label the room they are located in the building. 

The number picker allows selecting the mean building floor feature as in the original sensory application and the current floor you are at. The floor is limited from 1 - 20, but can be easily changed in the *CollectionFragment.java* code if needed.  

Finally, the second to bottom Text field allows you to label the building you conducted your scan in. The bottom field contains the current datagram that would be sent if you start sending data to the server.

## Usage - Server
To set up the server, run the **server.py** file. It will create the sensory schema and create a table used by the iOS sensory to store the data.

Then, run the server.jar file from the command prompt. This will provide a shell from which a few operations could be completed. If you need to kill this, CTRL + C will suffice.

* AP  
TODO, This would obtain the manufacturer of each access point detected.
* sensory  
This would print the iOS sensory table into a CSV file. It would also print the Android iOS sensory table in a CSV file.
* wifi  
Android devices can scan Wi-Fi access points and their RSSI signal strength. This would create a Wi-Fi lookup table, where the columns match with access points detected. This needs a bit more tuning to filter out rarely seen Access Points.
* exit  
Exit the shell, close the Java server.

## Features  
The list of features is almost identical to the original sensory one. 

| feature                      | description                                                                                                |
|------------------------------|------------------------------------------------------------------------------------------------------------|
| indoors                      | 1- indoors 0- outside                                                                                      |
| created at                   | Timestamp scan completed                                                                                   |
| session id                   | Timestamp of initial scan in session                                                                       |
| floor                        | Floor of where user was at                                                                                 |
| RSSI strength                | RSSI signal strength of AP connected to via Wi-Fi                                                          |
| GPS latitude                 | Get the latitude in degrees.                                                                               |
| GPS longitude                | Get the longitude in degrees.                                                                              |
| GPS vertical accuracy        | Get the estimated vertical accuracy of this location in meters                                             |
| GPS horizontal accuracy      | Get the estimated horizontal accuracy of this location (radial) in meters as the radius of 68% confidence. |
| GPS course                   | Get the bearing in degrees.                                                                                |
| GPS speed                    | Get the speed if it is available in meters/second over ground.                                             |
| barometric relative altitude | Computes the Altitude in meters from the atmospheric pressure and the pressure at sea level.               |
| barometric pressure          | Barometric pressure reading                                                                                |
| environment context          | Details of scan                                                                                            |
| environment mean bldg floors | Unknown                                                                                                    |
| environment activity         | Determine a User's motion activity                                                                         |
| city name                    | Name of city user is at                                                                                    |
| country name                 | Name of country user is at                                                                                 |
| magnet x                     | magentic field strength in X direction                                                                     |
| magnet y                     | magentic field strength in Y direction                                                                     |
| magnet z                     | magentic field strength in Z direction                                                                     |
| magnet total                 | Total magnetic field strength                                                                              |

NOTE: 
* iOS sensory doesn't have the complete sensory table like Android does. See TODO
* iOS sensory and Android Phones use different units when measuring barometric preassure from its sensors!  

https://developer.apple.com/documentation/coremotion/cmaltitudedata  
https://developer.android.com/guide/topics/sensors/sensors_environment  

iOS devices read in kilopascals and Android reads in hectopascals.
It seems that 1 hectopascal [hPa] = 0.1 kilopascal [kPa]

## Authors and Acknowledgment
Code Author: Andrew Quijano  
I would like to thank the Internet Real-Time (IRT) lab at Columbia University for funding the work to upgrade sensory and building its Android counterpart.  

If you use this code please cite the following papers:  
Falcon, William, and Henning Schulzrinne. "Predicting Floor-Level for 911 Calls with Neural Networks and Smartphone Sensor Data." International Conference on Learning Representations. 2018.  

To generate the tables, I used the following Link:  
https://www.tablesgenerator.com/markdown_tables

## License
[MIT](https://choosealicense.com/licenses/mit/)

## Project status
Currently, the following issues need to resolve
* iOS sensory can't collect Wi-Fi MAC Addresses and RSSI signal strength. This may prove to be near impossible as Apple has no equivalent to Wi-Fi manager class as in Android?
* iOS sensory needs the collection storyboard fixed. Also, it needs the features Henning requested.