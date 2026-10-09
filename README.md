# הבית שלנו — Family App

Native Kotlin / Jetpack Compose Android client and Firebase backend. Hebrew RTL, Android 8+, target API 35, version 0.2.0.

## Current status

The native Android app and Firebase backend have passed CI at source commit `6d707009568e2a9ed958cdd0d2f61462489bfff5`:
https://github.com/amiryashargadot/Family-App/actions/runs/37856893422

`assembleDebug`, `testDebugUnitTest` (4 tests), `lintDebug` (0 errors, 9 warnings), 5 Node tests and 11 Firestore Emulator tests passed. The artifact contains the debug APK and reports.

**The current APK has no Firebase configuration and displays setup guidance. It is not ready for family use.** Configure `GOOGLE_SERVICES_JSON` as a repository secret and rebuild, then deploy Firebase functions/rules and enable Anonymous Authentication. Production deployment, FCM delivery and two physical phones have not been verified. Preserve a private signing key before real use; CI-generated debug keys are not stable across runs.

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
