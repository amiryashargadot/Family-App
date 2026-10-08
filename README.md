# הבית שלנו — Family App

Native Kotlin / Jetpack Compose Android client and Firebase backend. Hebrew RTL, Android 8+, target API 35, version 0.2.0.

## Current status

The V1 source is implemented. A successful build of this new version has NOT yet been verified. Production Firebase functions/rules have NOT been deployed. This source archive is not an operational app delivery.

The last Android compiler run reported two issues: the Compose function type annotation and access to HttpsCallableResult.data. They have been changed to `@Composable () -> Unit` and `getData()`. A successful subsequent build has not been observed. Test sources were restored after the temporary build environment disappeared; five pure Node tests were rerun successfully. The current Android and integration suites still require a full rerun.

## Included features

Onboarding and role-bound invitations; personal weekday routines and daily snapshots; family tasks with server-awarded points; private two-person lists; owner-only daily answers and public family moods; store, ledger, atomic purchases and voucher redemption; shared decision countdown, winner, end time and app lock; FCM and WorkManager notification implementation.

## Build and verify

Use JDK 17, Android SDK 35 and Gradle 8.9. Place the supplied Firebase Android configuration at `app/google-services.json` outside version control.

```sh
gradle assembleDebug testDebugUnitTest lintDebug
cd functions
npm ci
npm test
npx firebase emulators:exec --only firestore --project demo-familyapp 'npm run test:integration'
```

GitHub Actions is prepared for `main`, `codex/**`, pull requests and manual runs. For a connected CI APK, configure the repository secret `GOOGLE_SERVICES_JSON`. Without it, the build shows setup guidance and cannot connect to the family project. The APK output is `app/build/outputs/apk/debug/app-debug.apk`.

Do not commit Firebase client configuration, administrator credentials or signing keys. See `docs/DEPLOYMENT.md` and `docs/STATUS-he.md`.
