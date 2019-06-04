Geocaching4Locus - Android application
======================================

Geocaching4Locus is a Locus add-on allows you to download and import caches directly from Geocaching.com site.

The latest stable release is under `master` branch. In `dev` branch are committed new features and bugfixes for upcoming version.

How to compile
==============
The App require to have correct Geocaching API key and secret for OAuth2 authorization. Unfortunately Grounspeak (Geocaching API author) prohibits to store testing or production keys in a source code repository. Thus please contact me I can provide testing keys for you if you want to participate on app development.

Anyway the app require to set this in your global `gradle.properties` file (the file is placed usually in `~/.gradle/gradle.properties` or `C:\Users\username\.gradle\gradle.properties`):

    geocachingApiKey=<production_api_key>
    geocachingApiSecret=<production_api_secret>
    geocachingApiStaging=false

The `<production_api_key>` and `<production_api_secret>` is provided via [Groundspeak's Geocaching Authorized Developers program](https://apidevelopers.geocaching.com/)

The App can be easily build by Gradle:

    gradlew assembleDebug - create a debug APK
    gradlew assembleRelease - create a release APK

AssembleRelease task will try to sign APK with a private key. To sign it, create `gradle.properties` configuration in your user gradle directory (usually `~/.gradle/gradle.properties` or `C:\Users\username\.gradle\gradle.properties`):

    storeFile=file:///path/to/keystore.keys or file://c:/path/to/keystore.keys 
    storePassword=mykeystorepassword
    keyPassword=mykeypassword  

The key name in a keystore must be `geocaching4locus` (or modify application `build.gradle` script). **If the configuration for signing is missing, Gradle build script use ADK debug private key for signing.**

Dependencies
============
* [Kotlin](https://kotlinlang.org/)
* [Jetpack AndroidX libraries](https://developer.android.com/jetpack/androidx)
* [Crashlytics](https://www.crashlytics.com)
* [Locus API - Core library for Android "Locus Map" application](https://github.com/asamm/locus-api)
* [OkHttp - An HTTP & HTTP/2 client for Android and Java applications](http://square.github.io/okhttp/)
* [Scrible Java - Simple OAuth library for Java](https://github.com/fernandezpablo85/scribe-java)
* [SLF4J - Simple Logging Facade for Java](http://www.slf4j.org/)
* [SLF4J-Timber - SLF4J binding for Jake Wharton's Timber logging library](https://github.com/arcao/slf4j-timber)
* [Material Dialogs](https://github.com/afollestad/material-dialogs)
* [Timber - A logger with a small, extensible API which provides utility on top of Android's normal Log class](https://github.com/JakeWharton/timber)
* [Koin](https://insert-koin.io/)

Developed by
============
* [Martin Sloup (Arcao)](http://arcao.com)
* and [other contributors](https://github.com/arcao/Geocaching4Locus/graphs/contributors)

Other links
===========
* [Official Website](http://geocaching4locus.eu/)
* [Google Play](https://play.google.com/store/apps/details?id=com.arcao.geocaching4locus)
* [User's guide](http://geocaching4locus.eu/users-guide/)
* [Facebook page](https://www.facebook.com/Geocaching4Locus)
* [App translations @ Crowdin](https://crowdin.com/project/geocaching4locus)

Licenses
========
Source code
-----------

    Copyright (C) 2012 Martin Sloup, arcao.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

Graphics
--------
* Some icon graphics are made by [Freepik](http://www.freepik.com/) from www.flaticon.com
