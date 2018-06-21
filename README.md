# Android-mock-location-gpx
After installation set this app to be Mock location app.
App has functionality of mocking either location and mocking routes
Example usage with mocking of one location from commandline (with use of Android Debug Bridge ):
adb  shell am broadcast -a send.mock -e lat 52.169 -e lon 21.068

App has also option for mocking location track with use of gpx created with 
https://www.gpsies.com

Testing file needs to be in format of 
https://www.gpsies.com/GPX/1/0
Example testing file can be found here
https://github.com/tymicki/Android-mock-location-gpx/blob/master/mock_track.gpx
Should be put on device under test here:
/sdcard/Download/mock_track.gpx


