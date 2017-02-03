![Calendula](https://dl.dropboxusercontent.com/u/4213618/calendula/calendula_promo_google_play.png)

[![Build Status](https://travis-ci.org/citiususc/calendula.svg?branch=develop)](https://travis-ci.org/citiususc/calendula)

# Calendula

Calendula is an Android assistant for personal medication management, aimed at those who have trouble following their medication regimen, forget to take their drugs, or have complex schedules that are difficult to remember.

> The app is available for download on Google Play
>
>[![](https://play.google.com/intl/en_us/badges/images/badge_new.png)](https://play.google.com/store/apps/details?id=es.usc.citius.servando.calendula)
>

Visit our web page for more info  [https://citius.usc.es/calendula/](https://citius.usc.es/calendula/)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine ready for development. If you want to help developing the app take a look to the contributing section, at the end.

### Development environment setup

We use [Android Studio](https://developer.android.com/studio/index.html) (the official Android IDE) for development, so we recommend it as the IDE to use in your development environment. Once you install Android Studio, you can use the Android SDK Manager to obtain the SDK tools, platforms, and other components you will need to start developing. The most important are:

 * Android SDK Tools and Android SDK Platform-tools (upgrade to their last versions is usually a good idea).
 * Android SDK Build-Tools 24.0.1.
 * Android 7.0 (API Level 24) SDK Platform.
 * Android Support Repository

You can also install other packages like emulators for running the app, if you don't have or don't want to use a real device. The minimum supported Android version is *4.0.1, Ice Cream Sandwich (API level 14).*

### Building and installing the app

First of all you need to get the source code, so clone this repository  on your local machine. If you want to contribute it is better to **fork the repository** by clicking on the *Fork* option on the top right corner and
 [keep it synced](https://help.github.com/articles/syncing-a-fork) by adding the original repository as a remote.

```
git clone https://github.com/[citiususc|yourgithubusername]/calendula.git
git remote add upstream https://github.com/citiususc/calendula.git
```

Android Studio uses Gradle as the foundation of the build system, but It is not necessary that you install it in your pc. You can use the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) included in the project, as recommended by Google. Building the app with
Gradle is very easy: in a terminal, go to the directory that contains the repository, and run the `clean` and `build` tasks using the gradle wrapper (you may need to run `chmod +x gradlew` before):
```
./gradlew clean build
```

Then, for running the app on a device or emulator:
```
adb [-d] install path/to/calendula.apk
```
*The -d flag specifies that you want to use the attached device (in case you also have an emulator running.*

This tasks can also be done from Android Studio with a few clicks.

## App versions
We only maintain the Calendula app at Google Play:

 * The latest version of the app available on Google Play reflects the code of the `master` branch.
 * Release branches are usually deployed through the *Google Play BETA channel* before they are made available to everyone. If you want to be a member of the testing community, join the testing group on Google Groups, and you will automatically receive the updates from the BETA channel like normal updates from Google Play.

> Join the  BETA channel: [https://groups.google.com/d/forum/calendula-testing](https://groups.google.com/d/forum/calendula-testing)

Check out the [contributing guidelines](CONTRIBUTING.md) for more info about the branching model.

## How does it look? ##

We try to follow [Material Design](https://material.google.com/#) principles. Take a look at the result!
   
*The screenshots below are taken from the development version (branch `develop`).*

  <img src="https://dl.dropboxusercontent.com/u/4213618/calendula/multipat/home.png" width="250px" />
  <img src="https://dl.dropboxusercontent.com/u/4213618/calendula/multipat/agenda.png" width="250px" />
  <img src="https://dl.dropboxusercontent.com/u/4213618/calendula/multipat/schedules.png" width="250px" />
  <img src="https://dl.dropboxusercontent.com/u/4213618/calendula/multipat/aviso.png" width="250px" />
  <img src="https://dl.dropboxusercontent.com/u/4213618/calendula/multipat/navdrawer.png" width="250px" />
  <img src="https://dl.dropboxusercontent.com/u/4213618/calendula/multipat/profile.png" width="250px" />

We use <a href="http://www.freepik.com/free-vector/people-avatars_761436.htm">this vector pack at Freepik</a>, [an icon](http://www.flaticon.com/free-icon/baby_13627) made by <a href="http://www.flaticon.com/authors/madebyoliver" title="Madebyoliver">Madebyoliver</a>, and some animals icons ([dog](http://www.flaticon.com/free-icon/dog_194178) and [cat](http://www.flaticon.com/free-icon/cat_194179)) from Freepick for avatars.
## Future work ##

We have a lot of development ideas, and we are open to newer ones. Below are some interesting features that could be very useful:

 * Information about nearby pharmacies, their locations and timetables
 * Trip assistant (how many pills I need for this weekend?)
 * Introducing [gamification](https://en.wikipedia.org/wiki/Gamification) concepts to improve adherence. 


## Contributing ##
Feel free to fork and send a pull request if you want to contribute to this project! Notice that Calendula is licensed under the terms of the [GPL v3 license](LICENSE.md), so by submitting content to the Calendula repository, you release your work to the public domain.

Before starting, take a look at our [contribution guidelines](CONTRIBUTING.md). 

### I would like to contribute, but I'm not a developer...
If you're not a developer but you want to help, don't worry! You can help [with app translations](CONTRIBUTING.md#help-with-app-translations), by [joining the BETA group](#app-versions), and [much more](CONTRIBUTING.md#i-would-like-to-contribute-but-im-not-a-developer)!. Everyone is welcome!

## License

Copyright 2016 CITIUS - USC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
