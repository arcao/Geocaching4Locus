# Changelog

## Version 3.1.0

- New filter: minimum favorite points
- Warning when the limit of full geocache downloads is almost used up
- Listing background image added to geocache images
- Live map runs with a notification and can be resumed from it
- Fix crash for users who disallowed sharing personal data
- Fix Download logs option resetting
- Support for Android 17
- New languages: Danish, Croatian, Hungarian, Italian, Portuguese, Slovenian
- Locus Map sends updates only while Live map is on

## Version 3.0.12

- Fix app crashes

## Version 3.0.11

- Fix app crashes

## Version 3.0.10

- Fix sign in to geocaching.com account

## Version 3.0.9

- Fix endless waiting circle in Geocache Lists
- Remove permission for retrieving advertising ID
- Other bugfixes

## Version 3.0.8

- Improve detecting membership type when user disallow sharing personal data
- Fix updating Geocaches
- Do not offer opening Geocaching forum links with app
- Update app localization

## Version 3.0.7

- Fix bugs in sign in process
- Fix removing Live Map geocaches after disabling Live Map
- Update app localization

## Version 3.0.6

- Rewrote sign in process to support Google Sign-in
- Fix wrong coordinate bug after click on download button in search nearest geocache
- Other bug fixes

## Version 3.0.5

- Fix app crash when progress is canceled
- Fix wrong progress value when updating more Geocaches

## Version 3.0.4

- Fixed Download All action in Bookmark list when Lite data is set in Settings
- Improved handling service outage errors
- Fixed using wrong value for Geocache owner field while import
- Other bug fixes

## Version 3.0.3

- Fixed importing all Geocaches from bookmark list
- Added workaround when Geocache contains invalid timezone ID

## Version 3.0.2

- Fixed not working Geocache type and size filter

## Version 3.0.1

- Fixed loading corrected coordinates for Geocaches
- Fixed updating Geocache for basic membership users
- Fixed Search nearest to use map center coordinates instead of GPS
- Fixed app crashes
- Other bug fixes

## Version 3.0.0

- Integrate new Geocaching API, some calls take less time and consume less data
- The app is rewritten from scratch

## Version 2.2.6

- Returned back support for older Android devices (Android 4.1+) using different sign-in method for them

## Version 2.2.5

- Fixed connection error for older Android 4.4 and 5.0 devices. Unfortunately dropped support for devices with Android 4.3 and lower because doesn't support TLS 1.1+ for secure connections.

## Version 2.2.4

- Fixed not working difficulty and terrain filter for some min-max combinations (thanks Zdeněk and Māris)

## Version 2.2.3

- Fixed crash while sorting geocache logs by date
- Fixed crash in Live Map
- Other bug fixes

## Version 2.2.2

- Fixed not working app crash reporting

## Version 2.2.1

- Fixed crash in Sign in process on older Android devices
- Fixed wrong colors for disabled state on dashboard for older Android devices
- Updated German language file

## Version 2.2

- Added button to downloadt Live map geocaches
- Rewritten Live map feature (Android 8 now supported)
- Importing more geocaches from GC code is now supported
- PM: Choice to flag geocache as not available when contains several Did Not Found, Needs Maintenance and Needs Archived logs
- PM: Added Locus Map share buttons to add geocache to list or watch geocache
- BM: Removed geocache web page from listing tab because it breaks Geocaching API license agreement
- Various bug fixes

## Version 2.1.5.5

- Fixed removing Live Map geocaches after disabling Live Map

## Version 2.1.5.4

- Fixed not working auto-hiding Live map notification after Locus Map exit

## Version 2.1.5.3

- Fixed crashes with a latest Locus Map BETA version

## Version 2.1.5.2

- Live Map works again for newer devices
- The original waypoint is not created for geocache with calculated coordinates

## Version 2.1.5.1

- Fixed not working Download logs action

## Version 2.1.5

- Live map items can be hidden when live map is disabled
- Android N support
- Various bug fixes

## Version 2.1.4

- Support for coordinates in Geocache logs
- Bug fixes for users with basic membership (changes in restrictions from Groundspeak side)
- Other bug fixes

## Version 2.1.3

- Fixed crash when Preferences is opened on some tablet devices

## Version 2.1.2

- Fixed SSL errors
- Fixed Live Map redownloading errors
- Fixed not working sign on via Facebook
- Other bug fixes

## Version 2.1.1

- Fixed filter for found and own hidden caches in search nearest

## Version 2.1

- Importing geocaches from bookmarks (PM feature)
- Partially downloaded geocaches can be imported even if error occurs
- Support for new permission model in Android 6
- UI improvements
- Bug fixes

## Version 2.0.3

- Added Chinese (Taiwan) language
- Properly handling of access token expiration error
- Fixed a lot of critical bugs
- Send Feedback action attach logs to e-mail (Please prefer this way for contact us)

## Version 2.0.2

- Fixed not taking over of map center from Locus Map

## Version 2.0.1

- Fixed not working downloading logs

## Version 2.0

- Dropped support for Android 2.x and Android 3.x
- Less usage limitation for Basic Members (see Manual for more)
- Added downloading hints for Live map (must be enabled in setting)
- Added Send Feedback choice in Settings
- Added Chinese language
- Improved UI
- Bug fixes

## Version 1.5.7.2

- Properly handling of access token expiration error
- Bug fixes

## Version 1.5.7.1

- Fixed not working downloading logs
- Other minor fixes

## Version 1.5.7

- Fixed network unavailable issue in Sign-In for older Android devices
- Fixed wrong order of Geocache logs
- Fixed app crash in selecting count of Geocaches to download
- Improved error messages when basic member use premium member features
- Other minor fixes

## Version 1.5.6.1

- Fixed downloading wrong count of Geocaches

## Version 1.5.6

- Added menu action to download up to 100 logs in Geocache dialog
- Live map notification can be hidden when map in Locus is not visible
- Added preference to change step for Count of Geocache choose dialog

## Version 1.5.5

- Improved settings (geocache type, container type, difficulty and terrain rating values are now displayed without accessing to settings sub-screen)
- In search nearest geocaches dialog you can select count of geocaches up to 500 with step of 10 (Android 2.x and low memory size devices are limited to 200 geocaches to prevent Locus low memory crashes)
- Improved downloading geocaches in search nearest dialog
- Fixed crashes reported by users

## Version 1.5.4.4

- Fixed issue with renamed waypoint types
- Changed Unknown geocache type to Mystery
- Added Giga Event geocache type

## Version 1.5.4.3

- Fixed wrong storing of Corrected coordinates

## Version 1.5.4.2

- Cache and log images are now handled in Locus
- Updated to latest Locus API
- Require Locus in version 2.20.1.8 or newer

## Version 1.5.4.1

- Fixed crashes reported by users

## Version 1.5.4

- Improved Live map support
- Added notification when Live map is working
- Added configuration choice to show notification even if the Live map is disabled
- Permission to access external storage because of sending geocaches to Locus is not required anymore, internal storage used instead (this fix a lot of errors received recently)
- Fixed wrong handling of plain text short/long geocache description
- Fixed App start crashes
- Other minor fixes

## Version 1.5.3.2

- Improved network stability
- Network compression enabled by default

## Version 1.5.3.1

- Import task adapted on new Geocaching.com URLs

## Version 1.5.3

- Improved handling of corrected coordinates
- Fixed bug with downloading Geocaches and pressing cancel button
- Geocaches on ignore list are now excluded
- Dropped support for Android 2.1 (Locus 2.14.0 doesn't support this version too) - decreased application size
- Fixed reported crashes

## Version 1.5.2.2

- Fixed reported app crash (sorry for that)

## Version 1.5.2.1

- Critical fix of crash when you want to download full data for Geocache with basic data
- Fixed other reported crashes

## Version 1.5.2

- Fixed reported crashes

## Version 1.5.1

- Geocaching4Locus now officially use Geocaching Live API

## Version 1.5

- Added dialog to import Geocache from manual input of Geocache code
- Improved Live Map feature to support up to 250 Geocaches per screen
- Redesigned dialog for searching nearest Geocaches. Added count of Geocaches select box.
- Added support for favorite points (Locus can show it)
- Sign in is now handled by custom dialog instead of external browser (should fix a lot of problems with external browsers)
- A lot of changed code, GUI improvements and used new Locus API.
- Many other bug fixes

## Version 1.4.2.5

- Rewritten downloading process to eliminate out of memory errors (Now it's not a problem to update more than 200 caches)
- Fixed Geocaching logs merging issue
- Other minor fixes

## Version 1.4.2.4

- Used Geocaching server time instead of device time to fix problem in a log in process when device has wrong timezone or time

## Version 1.4.2.3

- Fixed a few reported problems in a log in process
- Better error message when server doesn't return valid response in the log in process
- Fixed other reported problems
- Added Slovenian language (by davidpanic1)

## Version 1.4.2.2

- Improved login process for some type of phones
- In login process is now used user preferred browser to fix problem with expired session
- Fixed crash when updating waypoint instead of cache

## Version 1.4.2.1

- Reworked logging in to use OAuth authorization

## Version 1.4.2

- Added updating more caches via Points screen - Tools button
- Corrected coordinates from GSAK are now kept as it
- Added a cache size filter
- Added creating Images tab (experimantal)
- Fixed lot of crashes caused by device orientation changes
- Fixed other reported crashes

## Version 1.4.1.3

- Fixed crash in logging in process
- Fixed reported error about exceeding count of trackable logs
- Fixed not working difficulty and terrain filter

## Version 1.4.1.2

- After many negative reviews about requiring Account permissions, I decided to abandon the idea to store login information in a safer Accounts & Sync and store Account details old way that was used prior to version 1.4.1.
- Fixed lot of crashes caused by device orientation changes
- Fixed other reported crashes

## Version 1.4.1.1

- Fixed reported crashes

## Version 1.4.1

- Keep GSAK cache logs when update the cache
- Use old cache coordinates when the cache is archived after update
- Fixed issue with used date in cache logs
- User credentials are now safely stored in Android Account manager
- Improved error reporting
- Other minor fixes

## Version 1.4

- Added Live map support (limitation: 50 caches per screen)
- Added downloading personal notes (PM only)
- Added menu with most used actions (e.g. Live map toggle button) when you add G4L to Locus sidebar
- Added creating user waypoints from personal cache note (PM only)
- Added count of found caches by user in cache log
- Fixed bug when importing cache from long geocaching.com url
- Other minor fixes

## Version 1.3.9.1

- Fixed not authorized bug while downloading caches
- Added support for AndroidPIT market.

## Version 1.3.9

- Reworked whole idea of simple cache downloading (now it's basic cache info)
- Added difficulty and terrain filter in preference
- Fixed distance filter
- Passing on data to Locus is now performed via file
- Added support for corrected coordinates via waypoint

## Version 1.3.8.2

- Dropped support for Android lower than 2.1 (Locus did the same since 1.16.0).
- Supported Android 4.0 look & feel.
- Added support to update any cache from any source.
- Fixed Preference crash when perform Filter -> back -> change any value and click ok.
- Supports download full cache data only once for a simple cache (since Locus 1.16.2).

## Version 1.3.8.1

- Added Import to Locus for geocaching URL.
- Removed updating cache because it isn't working how I expected.

## Version 1.3.8

- Added filter for a temporarily disabled caches.
- Added possibility to update cache from Locus.
- More preferences around downloading caches (count of logs, trackables and allow to update simple caches when they are displayed in Locus).

## Version 1.3.7.1

- Fixed reported crashes

## Version 1.3.7

- G4L is available for more devices (GPS isn't required now)
- Fixed bug with releasing GPS device (causing high power consumption after close G4L)
- G4L is now in Locus function list and can be added to right panel in Locus.
- Credentials error now open Preferences.

## Version 1.3.6.3

- Added support for a geocache attributes.
- G4L now supports preference backup via Android Backup Service.

## Version 1.3.6.2

- Fixed downloading bug when you use non ascii login or password.
- Fixed problem with multiple dialog when Locus is not installed.

## Version 1.3.6.1

- Fixed reported crash

## Version 1.3.6

- Fixed wrong parsing of difficulty and waypoints.
- G4L should never more crash with NullPointerException in specific circumstances.
- Probably fixed bug with not hidden on-going notification after download.
- Added filter to show own (hidden by you) caches.
- In a cache type preference is possible to check or uncheck all types through a menu.

## Version 1.3.5

- For downloading caches is used a service. Thus it is not problem to download caches on a background.
- While downloading all necessary informations are visible through the notifications.
- G4L use a new Geocaching API.

## Version 1.3.1

- Translations to German, French, Dutch, Portuguese
- Bug corrections

## Version 1.3

- Import of complete information about cache to Locus
- Import instead of show caches in Locus map
