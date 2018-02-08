# How to contribute

We love contributions from everyone! There are only a few guidelines that we encourage contributors to follow so that we can accept and maintain third party changes.

Notice that Calendula is licensed under the terms of the [GNU General Public License, version 3](LICENSE.md), so by submitting content to the Calendula repository, you release your work under the terms of this license.

Since this repository contains the code for an Android app published in Google Play, which could include part of your contributions in future updates, we’re required to have agreements ([CLA](https://www.clahub.com/agreements/citiususc/calendula)) with everyone who takes part. This is the easiest way for you to give us permission to use your contributions (in effect, you’re giving us a license, but you still own the copyright, so you retain the right to modify your code and use it in other projects).

## Getting Started

Make sure you have a [GitHub account](https://github.com/signup/free) and have signed our [Contributor License Agreement](https://www.clahub.com/agreements/citiususc/calendula). Once that's ready, you can:

* [Submit an issue](https://github.com/citiususc/calendula/issues/new) to propose enhancements or report bugs. Make sure there isn't another isue with the same subject already!

    Try to keep the issue descriptive and include the steps to reproduce the bug if that's the case.

* [Fork the repository](https://github.com/citiususc/calendula/fork), make some changes, and then submit a [pull request](https://github.com/citiususc/calendula/compare). Please see [Making changes](#making-changes) for this.

### Making Changes

We follow the branching model proposed by [Vincent
Driessen](http://nvie.com/about/) on [this article](http://nvie.com/posts/a-successful-git-branching-model/), so we have two main branches: `master` and `develop`.

 * The `master` branch reflects the code of the latest version of the app available on Google Play.
 * The `develop` branch reflects the code of the latest delivered development changes for the next release.
 * Furthermore, when a development version (almost) reflects the desired state of the new release, a release branch is created. Release branches are usually deployed through the *Google Play BETA channel* before they are made available to everyone

Create a topic branch from where you want to work. This may be usually the `master` or `release` branch for bug fixes or `develop` for adding new features. Please avoid working directly on the master branch.

## I would like to contribute, but I'm not a developer...

Non-code contributions are also welcome!. You can do a lot of things:

 * Comment on a issue or start your own to suggest ideas or give your opinion.
 * Fix typos, comments or clarify language to improve the quality of the app.
 * Propose an icon, a better drawable for an specific action, or even a new material app logo ;-).
 * Be a member of the testing community by joining the testing group on Google Groups. You will automatically receive the updates from the BETA channel like normal updates from Google Play. This helps us find bugs before the public release!

> Join the  BETA channel: [click here!](https://play.google.com/apps/testing/es.usc.citius.servando.calendula)


### Help with app translations
Contributing translations is now easier than ever! Just join us at POEditor using [this link](https://poeditor.com/join/project/kIdyqFodDn) and start translating Calendula to one of the existing languages, or suggest a new one. **This is the recommended method for translations**.
You can also contribute with better translations for particular words or sentences.

Alternatively, you can contribute translations via pull request:

 * Add a new folder named `values-{LANG}/` at `Calendula/src/main/res/`
 * Translate the `strings_translatable.xml` file from `values` to your desired language.
 * Send a pull request.

You can also improve an existing `strings_translatable.xml` file and make a PR with that!

