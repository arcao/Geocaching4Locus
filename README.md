Geocaching4Locus - Android application
======================================

Geocaching4Locus is a Locus add-on allows you to download and import caches directly from Geocaching.com site.

The latest stable release is under `master` branch. The branch `v1.x` contains latest version for Andorid 2.x. In `dev` branch are commited new features and bugfixes for upcomming version.

How to compile
==============
Application can be easily build by Gradle. If you don't have Gradle, you can use gradlew batch scripts instead (included in a repository).

    gradle assembleDebug - create a debug APK
    gradle assembleRelease - create a release APK

AssembleRelease task will try to sign APK with a private key. To sign it, create `gradle.properties` configuration in your user gradle directory (usually `~/.gradle/gradle.properties` or `C:\Users\username\.gradle\gradle.properties`):

    storeFile=file:///path/to/keystore.keys or file://c:/path/to/keystore.keys 
    storePassword=mykeystorepassword
    keyPassword=mykeypassword  

The key name in a keystore must be `geocaching4locus` (or modify application `build.gradle` script). **If the configuration for signing is missing, Gradle build script use ADK debug private key for signing.**

The created APKs will use **Geocaching Live Staging server** (OAuth key and secret are placed in `build.gradle` file). This is OK for a **testing purposes only** (server is really slow and has old database). For production use it must be switched to production server creating `gradle.properties` file in a project root with bellow content:

    geocachingApiKey=<production_api_key>
    geocachingApiSecret=<production_api_secret>
    geocachingApiStaging=false

The `<production_api_key>` and `<production_api_secret>` is provided via [Groundspeak's Geocaching Live Service program](http://www.geocaching.com/mobile/apidevelopers/) 

Dependencies
============
* [Apache Commons Lang](http://commons.apache.org/lang/)
* [Apache Commons IO](http://commons.apache.org/io/)
* [Butter Knife](https://github.com/JakeWharton/butterknife)
* [Crashlytics](https://www.crashlytics.com)
* [Geocaching API for Java](https://github.com/arcao/geocaching-api)
* [Locus API](http://docs.locusmap.eu/doku.php?id=manual:advanced:locus_api)
* [Scrible - Simple OAuth library for Java](https://github.com/fernandezpablo85/scribe-java)
* [SLF4J - Simple Logging Facade for Java](http://www.slf4j.org/)
* [SLF4J-Timber - SLF4J binding for Jake Wharton's Timber logging library](https://github.com/arcao/slf4j-timber)
* [Material Dialogs](https://github.com/afollestad/material-dialogs)
* [Timber](https://github.com/JakeWharton/timber)

Developed by
============
* [Martin Sloup (Arcao)](http://arcao.com)

Other links
===========
* [Official Website](http://geocaching4locus.eu/)
* [Google Play](https://play.google.com/store/apps/details?id=com.arcao.geocaching4locus)
* [Manual](http://geocaching4locus.eu/manual/)
* [Discussion forum](http://forum.asamm.cz/viewtopic.php?f=26&t=549)
* [Google+ page](https://plus.google.com/+Geocaching4LocusEu)
* [Facebook page](https://www.facebook.com/Geocaching4Locus)

License
=======
    Copyright (C) 2012 Martin Sloup, arcao.com

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
    
    This program comes with ABSOLUTELY NO WARRANTY!