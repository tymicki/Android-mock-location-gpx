# Android mock location gpx
Application for easy locations mocking  
## Getting Started
After installation set this app device developer options to be mock location app.
Application has functionality of mocking either one location or mocking routes. Example usage with mocking of one location from commandline (with use of Android Debug Bridge )
```
adb  shell am broadcast -a send.mock -e lat 52.169 -e lon 21.068
```
App has also option for mocking location track with use of gpx created with 
[GPSies](https://www.gpsies.com)

Testing file needs to be in format of 
```
https://www.gpsies.com/GPX/1/0
```
Example testing file can be found here
[mock track](https://github.com/tymicki/Android-mock-location-gpx/blob/master/mock_track.gpx), should be put on the device under test here:
```
/sdcard/Download/mock_track.gpx
```
App should be also granted read external storage permission in this use case.
Mock route could be started from the app UI by tapping on "RUN MOCK ROUTE"
 or by sending below action from the commandline:
```
adb  shell am broadcast -a send.mock.route 
```
