# Android mock location gpx
Application for easy locations mocking  
## Getting Started
After installation set this app in device developer options to be mock location app.
Application has functionality of mocking either one location or mocking routes. Example usage with mocking of one location via adb (with use of Android Debug Bridge)
```
adb shell am broadcast -a send.mock -e lat 52.169 -e lon 21.068
```
App has also option for mocking location track with use of GPX created by [GPSies](https://www.gpsies.com).
Mock location track data needs to comply with
```
https://www.gpsies.com/GPX/1/0
```
Example testing file can be found here
[mock track](https://raw.githubusercontent.com/tymicki/Android-mock-location-gpx/master/mock_track.gpx), should be put on the device under test here:
```
/sdcard/Download/mock_track.gpx
```
App should be also granted read external storage permission in this use case.
Mock route could be started from the app UI by tapping on "RUN MOCK ROUTE"
 or by sending below action from adb:
```
adb shell am broadcast -a send.mock.route 
```
