![](https://tec.citius.usc.es/calendula/github-assets/calendula_promo_google_play.png)
# Calendula [![Gitlab CI](https://gitlab.com/calendula-ci/calendula/badges/develop/build.svg)](https://gitlab.com/calendula-ci/calendula/pipelines)

Calendula is an Android assistant for personal medication management, aimed at those who have trouble following their medication regimen, forget to take their drugs, or have complex schedules that are difficult to remember.

The app is available for download in Google Play, F-Droid and Github.
<table>
    <tr>
        <td align="center"><a href="https://play.google.com/store/apps/details?id=es.usc.citius.servando.calendula"><img src="https://play.google.com/intl/en_us/badges/images/badge_new.png" alt="Get it on Google Play" ></a></td>
        <td align="center"><a href="https://f-droid.org/packages/es.usc.citius.servando.calendula/"><img src="https://gitlab.com/fdroid/artwork/raw/master/badge/get-it-on.png" alt="Get it on F-Droid" height="68"></a></td>
        <td align="center"><a href="https://github.com/citiususc/calendula/releases/latest"><img src="https://user-images.githubusercontent.com/663460/26973090-f8fdc986-4d14-11e7-995a-e7c5e79ed925.png" alt="Get it on Github" height="68"></a></td>
    </tr>
</table>

Visit our web page for more info  [https://citius.usc.es/calendula/](https://citius.usc.es/calendula/)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine ready for development. If you want to help developing the app take a look to the contributing section, at the end.

### Development environment setup

We use [Android Studio](https://developer.android.com/studio/index.html) (the official Android IDE) for development, so we recommend it as the IDE to use in your development environment. Once you install Android Studio, you can use the Android SDK Manager to obtain the SDK tools, platforms, and other components you will need to start developing. The most important are:

* Android SDK Tools and Android SDK Platform-tools (upgrade to their last versions is usually a good idea).
* Android SDK Build-Tools 27.0.3.
* Android 8.1 (API Level 27) SDK Platform.
* Android Support Repository

You can also install other packages like emulators for running the app, if you don't have or don't want to use a real device. The minimum supported Android version is *4.1, Jelly Bean (API level 16).*

### Building and installing the app

First of all you need to get the source code, so clone this repository  on your local machine:

```bash
git clone https://github.com/citiususc/calendula.git
cd calendula
```

Android Studio uses Gradle as the foundation of the build system, but it's not necessary to install it separately. Instead, you can use the included [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html). To build the app, open a terminal in the repository folder and run:

```bash
./gradlew clean assembleDevelopDebug
```
*Note: "developDebug" is the [build variant](https://developer.android.com/studio/build/build-variants.html) that we use for development. To see other variants, please check `Calendula/build.gradle`.*

Then you may install the app on a device or emulator:

```bash
adb install Calendula/build/apk/develop/debug/developDebug-[version].apk
```

These tasks can also be executed from Android Studio with a few clicks.

## App versions

We maintain releases of Calendula on Google Play, F-Droid and here on Github.

 * The latest version of the app available on those pages reflects the code of the `master` branch.
 * Release branches are usually deployed through the *Google Play BETA channel* before they are made available to everyone. If you want to be a member of the testing community, join the testing group on Google Groups, and you will automatically receive the updates from the BETA channel like normal updates from Google Play.

> Join the  BETA channel: [click here!](https://play.google.com/apps/testing/es.usc.citius.servando.calendula)

Check out the [contributing guidelines](CONTRIBUTING.md) for more info about the branching model.

## How does it look?

We try to follow [Material Design](https://material.google.com/#) principles. Take a look at the result!

  | <img src="https://tec.citius.usc.es/calendula/github-assets/home.png" width="230px"/>  | <img src="https://tec.citius.usc.es/calendula/github-assets/agenda.png" width="230px"/> | <img src="https://tec.citius.usc.es/calendula/github-assets/schedules.png" width="230px"/>
  |:---:|:---:|:---:|
  | <img src="https://tec.citius.usc.es/calendula/github-assets/aviso.png" width="230px"/> | <img src="https://tec.citius.usc.es/calendula/github-assets/navdrawer.png" width="230px"/> | <img src="https://tec.citius.usc.es/calendula/github-assets/profile.png" width="230px"/>

## Future work

We have a lot of development ideas, and we are open to newer ones. Below are some interesting features that could be very useful:

* Information about nearby pharmacies, their locations and timetables
* Trip assistant (how many pills I need for this weekend?)
* Introducing [gamification](https://en.wikipedia.org/wiki/Gamification) concepts to improve adherence.

## Artwork attribution

We are using the the following resources in the app:

* [People Vector Pack](http://www.freepik.com/free-vector/people-avatars_761436.htm) by [Freepik](http://www.freepik.com)
* [Baby](http://www.flaticon.com/free-icon/baby_136272), [Dog](http://www.flaticon.com/free-icon/dog_194178) and [cat](http://www.flaticon.com/free-icon/cat_194179) icons by <a href="https://www.flaticon.com/" title="Flaticon">Flaticon</a> (<a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>)
* [Alarm clock animation](https://dribbble.com/shots/1114887-Alarm-Clock-GIF) by  [Daan De Deckere](http://daandd.be/)

## Contributing

Feel free to fork and send a pull request if you want to contribute to this project! Notice that Calendula is licensed under the terms of the [GNU General Public License (v3)](LICENSE.md), so by submitting content to the Calendula repository, you release your work under the terms of this license.

Before starting, take a look at our [contribution guidelines](CONTRIBUTING.md).

### I would like to contribute, but I'm not a developer...

If you're not a developer but you want to help, don't worry! You can help [with app translations](CONTRIBUTING.md#help-with-app-translations), by [joining the BETA group](#app-versions), and [much more](CONTRIBUTING.md#i-would-like-to-contribute-but-im-not-a-developer)! Everyone is welcome!

## License

Copyright 2018 CITIUS - USC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
