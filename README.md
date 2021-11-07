KeyLocker
=========

KeyLocker is a password manager for Android. It is derived from the Windows Phone app and uses the same file format.

Using this app you can store passwords and notes, small snippets of text, in a safe container using secure AES encryption. A notable feature of KeyLocker is the ability to synchronize keylocker files.
The Windows Phone app used to use OneDrive for this, the Android for now only supports WebDAV.

Installation
------------

Eventually I hope this app might make it into an app store, however, I'm not willing to pay for a Google developer account and so the Play store is out of question. For now, you can install KeyLocker by
using F-Droid and then add the private [KeyLocker repository](https://www.hekkelman.net/~maarten/fdroid/repo)

Usage
-----

The use of this app should be pretty straight forward. Use the drawer (accessible through the hamburger menu) to switch between Keys and Notes. Add new Keys or Notes by tapping the plus button.

The password generator generates passwords of 12 characters, you can change this and other parameters in the settings.

Backups can be made to the SD card or to a WebDAV location. The information to access this WebDAV location is stored in a special Key card.

Backups are in fact synchronizations. That means, if you update a key on another phone and synchronize to the WebDAV location, you can pick up that change with another phone. A time stamp defines which change will eventually overwrite the other.

