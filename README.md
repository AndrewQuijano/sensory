# sensory
This repository contains the Swift 5 version of William Falcon's sensory iOS app. Also, it contains its Android counterpart which can collect the same features as sensory. Also, the Android version can collect Wi-Fi Access Point MAC address and their corresponding RSSI signals for indoor localization.

The server code for both data collection and processing is located in this repository:
https://github.com/AndrewQuijano/ColumbiaREU2018.git  

## Installation
** iOS Installation
Note this was tested on Xcode 11. 

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
* The Android version of sensory is still in progress of being refactored
* iOS sensory does not have a magnetometer
* iOS sensory can't collect Wi-Fi MAC Addresses and RSSI signal strength. This may prove to be near impossible as Apple have no equivalent to Wi-Fi manager class as in Android?
* Documentation needs improvement as well.

This should be taken care of during the Winter break in December before Spring 2021 semester.


