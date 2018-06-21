# Android-mock-location-gpx
After installation set this app to be Mock location app. 
Example usage from commandline  adb  shell am broadcast -a send.mock -e lat 52.169 -e lon 21.068
App has also option for mocking location with use of gpx created with eg https://www.gpsies.com
Testing file needs to be in format of https://www.gpsies.com/GPX/1/0
e.g.
https://github.com/tymicki/Android-mock-location-gpx/blob/master/mock_track.gpx
Should be put on device under test here:
/sdcard/Download/mock_track.gpx

