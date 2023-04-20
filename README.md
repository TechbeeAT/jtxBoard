# jtx Board

## Get the most out of journals, notes & tasks
Elevate the power of the iCalendar standard to the next level, use the potential of the combination of journals (VJournal), notes (VJournal) and tasks (VTodo) out of one app and use DAVx5 to synchronize your entries with the CalDAV-server of your choice!

### iCal standard compliant
Using the iCal standard ensures compatibility and interoparability with other apps and services independent of a dedicated provider or infrastructure. Journals and Notes are compliant to the definition of the VJOURNAL component, Tasks are compliant to the VTODO component. Future features will also include import and export functionalities to and from .ics files :-)

### Combine journals, notes & tasks
Instead of using separate apps for journals, notes & tasks you can use them out of one hand, combine and link them to each other, e.g. create meeting minutes and link your tasks to them. 

### Sync with DAVx5
Synchronize your entries with any compatible CalDAV server by using DAVx5 (https://www.davx5.com/). By using DAVx5 you are free to choose your preferred provider for CalDAV, you can even use your local server to store and synchronize your data.
Note: DAVx5 is an independent app and must be acquired separately.

**Find out more on https://jtx.techbee.at/**

---

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
    alt="Get it on Google Play"
    height="80">](https://play.google.com/store/apps/details?id=at.techbee.jtx)
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
    alt="Get it on F-Droid"
    height="80">](https://f-droid.org/de/packages/at.techbee.jtx/)

---



### Contributing

[![Crowdin](https://badges.crowdin.net/jtx-board/localized.svg)](https://crowdin.com/project/jtx-board)

Contributions are always welcome! Whether financial support, translations, code changes, bug reports, feature requests, or otherwise, your help is appreciated. For more information please have a look at [Contribute](https://jtx.techbee.at/app/contribute) on our website.

[![PayPal donate button](https://img.shields.io/badge/paypal-donate-yellow.svg?logo=paypal)](https://www.paypal.com/donate/?hosted_button_id=KNCKKUUYN4FMJ)

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/K3K2GSCFJ)

### Communication

For communication with the team and other people, please use the forums to get in touch either directly on Gitlab or through the support form on [https://jtx.techbee.at](https://jtx.techbee.at)

<img src="https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/featureGraphic.jpg" alt="jtx Board Banner with Screenshot" width="70%">


### Flavors

jtx Board provides different flavors: 
- **gplay** is the flavor for the Google Play store that comes in a standard and pro version. The pro version is paid and enables editing options for remote entries.
- **huawei** is the flavor used for is the flavor for the Huawei app gallery that comes in a standard and pro version. 
- **amazon** is the flavor used for is the flavor for the amazon app store that comes in a standard and pro version. 
- **generic** deprecated - was the flavor other app stores like Amazon. This flavor contains only open source libraries and is meant to be paid before downloading. 
- **ose** is the open source edition. **If you would like to create build the app from source, this is the recommended flavor.** This flavor contains only open source libraries. Instead of an in app-purchase this flavor has an additional page for donations visible. As Google Maps is not open source, the ose flavor uses OpenStreetMap.


### Permissions
jtx Board uses/requests the following permissions:
- GET_ACCOUNTS is used to determine if there are accounts set up in DAVx5 and show them in the UI
- RECORD_AUDIO can be used to access the microphone for adding audio notes and to use the speech-to-text engine
- READ_CONTACTS can be used to get suggestions when selecting attendees or a contact for an entry
- READ_SYNC_STATS is used to show a progress bar when a synchronization through DAVx5 is currently in progress
- INTERNET is used to retrieve the list of contributors for translations from POEditor.com/Crowdin.com and release notes from GitHub.com
- VIBRATE gives you haptic feedback when moving an entry on the Kanban-Board
- POST_NOTIFICATIONS let's you receive the notifications when an alarm is due
- ACCESS_COARSE_LOCATION & ACCESS_FINE_LOCATION can move the map to your current location (gplay flavor only), can set the current location (latitude, longitude) for an entry
- SCHEDULE_EXACT_ALARM used to show a notification when an alarm is due


### Troubleshooting
If you have any troubles with the synchronization, please check first the following article [https://jtx.techbee.at/troubles-with-the-synchronization-see-what-could-go-wrong]  before opening an issue.


### Screenshots
[<img src="https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot1.jpeg"
    height="200">](https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot1.jpeg)
[<img src="https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot2.jpeg"
    height="200">](https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot2.jpeg)
[<img src="https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot3.jpeg"
    height="200">](https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot3.jpeg)
[<img src="https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot4.jpeg"
    height="200">](https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot4.jpeg)
[<img src="https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot5.jpeg"
    height="200">](https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot5.jpeg)
[<img src="https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot6.jpeg"
    height="200">](https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot6.jpeg)
[<img src="https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot7.jpeg"
    height="200">](https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot7.jpeg)
[<img src="https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot8.jpeg"
    height="200">](https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot8.jpeg)
[<img src="https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot9.jpeg"
    height="200">](https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot9.jpeg)
[<img src="https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot10.jpeg"
    height="200">](https://github.com/TechbeeAT/jtxBoard/blob/develop/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot10.jpeg)
