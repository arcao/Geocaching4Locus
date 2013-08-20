Geocaching4Locus - Android application
======================================

Geocaching4Locus is a Locus add-on allows you to download and import caches directly from Geocaching.com site.

How to compile
==============
Application can be easily build by Gradle. If you don't have Gradle, you can use gradlew batch scripts instead (included in a repository).

    gradle assembleDebug - create a debug APK
    gradle assembleRelease - create a release APK

Assembly release task will try to sign APK with a private key. To sign it, create `gradle.properties` configuration in your user gradle directory (usually `~/.gradle/gradle.properties` or `C:\Users\username\.gradle\gradle.properties`):

    storeFile=file:///path/to/keystore.keys or file://c:/path/to/keystore.keys 
    storePassword=mykeystorepassword
    keyPassword=mykeypassword  

The key name in a keystore must be `geocaching4locus` (or modify application `build.gradle` script).

**The created APKs will not work, because for sign in to Geocaching Live API you need OAuth key and secret. The Geocaching Live API license agreement doesn't allow distribute OAuth key and secret with sources.**

But if you get them, simply copy `ProductionConfiguration` and `StaggingConfiguration` classes from `com.arcao.geocaching.api.configuration.impl_sample` package to `com.arcao.geocaching.api.configuration.impl` and change `YOUR_OAUTH_KEY` and `YOUR_OAUTH_SECRET` with your OAuth key and secret. The `StaggingConfiguration` class is for a test server provided by Grounspeak. OAuth key and secret for this server is different than for production server. Which of these configuration has to be used, you can select by `AppConstants.USE_PRODUCTION_CONFIGURATION`.

The configuration instances of these classes are automatically resolved by `GeocachingApiConfigurationResolver` on application start, see `Geocaching4LocusApplication` class.

Dependencies
============

* [ACRA][07]
* [Apache Commons Lang][02]
* [Geocaching API for Java][01]
* [Jtpl][04]
* [Locus Addon Public Library][06]
* [OAuth Signpost][03]
* [Simple Logging Facade for Java (SLF4J)][05]


Developed by
============

* [Martin Sloup (Arcao)](http://arcao.com)

Other links
============

* [Official Website](http://geocaching4locus.eu/)
* [Page on Google Play](https://play.google.com/store/apps/details?id=com.arcao.geocaching4locus)
* [Page on AndroidPIT](http://www.androidpit.com/en/android/market/apps/app/com.arcao.geocaching4locus/Locus-addon-Geocaching)
* [Manual](http://geocaching4locus.eu/manual/)
* [Discussion forum](http://forum.asamm.cz/viewtopic.php?f=26&t=549)
* [Page on Google+](https://plus.google.com/104753360614230872185)

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




 [01]: https://github.com/arcao/geocaching-api
 [02]: http://commons.apache.org/lang/
 [03]: http://code.google.com/p/oauth-signpost/
 [04]: http://jtpl.sourceforge.net/
 [05]: http://www.slf4j.org/
 [06]: http://code.google.com/p/android-locus-map/
 [07]: http://code.google.com/p/acra/
