# Kids.Talk Android

A supervised VoIP calling app for children, built on [Linphone Android](https://github.com/BelledonneCommunications/linphone-android).

Kids.Talk is a locked-down SIP dialler designed for supervised use by children. It provides a single large call button, guided permission setup, and no access to messaging, contacts, or call history. It connects to the Kids.Talk managed PBX service.

## Features

- Single-contact voice calling with one large Call button
- Guided first-run permission setup (microphone, notifications, full-screen alerts)
- Dark mode support following the system setting
- Ringer mode protection — never overrides silent/vibrate
- No camera, no contacts, no messaging, no call history
- Remote provisioning via the Kids.Talk PBX

## Licensing

This project is a fork of [linphone-android](https://github.com/BelledonneCommunications/linphone-android) and is distributed under the **GNU General Public License v3.0**. See [LICENSE.txt](LICENSE.txt) for the full licence text.

The upstream Linphone Android project is copyright © Belledonne Communications 2010-2025.  
Kids.Talk modifications are copyright © Computer Wholesale.

## Building

### Prerequisites

- Android Studio Hedgehog or later
- JDK 21
- Android SDK with API level 36 build tools

### Build

```bash
git clone https://github.com/Computer-Wholesale/kids.talk-android.git
cd kids.talk-android
./gradlew assembleDebug
```

The APK will be output to `app/build/outputs/apk/debug/`.

### Release builds

A `google-services.json` file is required for Firebase Cloud Messaging (incoming call push notifications). This file is not included in the repository. Contact the Kids.Talk service operator to obtain credentials for your deployment.

## Source publication

This repository constitutes the complete corresponding source code for all Kids.Talk Android releases, as required by GPL-3.0. Each tagged release corresponds to a published APK version.

| Tag | Version | Notes |
|-----|---------|-------|
| v1.14 | 1.14 | Current release |

## Privacy

Kids.Talk collects the minimum data necessary to operate a VoIP calling service. See the [Privacy Policy](https://kids.talk/privacy) for full details.

## Support

For service support, visit [kids.talk/help](https://kids.talk/help).
