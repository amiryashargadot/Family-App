# Production activation — not yet performed

Project `familyapp-b33ef`; Android package `com.amiryashargadot.familyapp`; function region `europe-west1`.

1. Sign in as the Firebase project owner. Enable Anonymous Authentication. Do not enable automatic cleanup of anonymous users: they are persistent family accounts.
2. Create the default Firestore database if absent. Cloud Functions requires an appropriate billing plan; account/billing changes require the owner's acceptance.
3. From the source root, after `npm ci` in `functions`:

```sh
firebase login
firebase use familyapp-b33ef
firebase deploy --only firestore:rules,firestore:indexes,functions
```

Recovery uses Admin SDK createCustomToken. The function runtime account may need the narrowly scoped IAM permission to sign custom tokens. Do not send or create an administrator JSON key for this chat.

Then install on two physical phones, create an adult family profile, save its private recovery code, issue a role-bound invitation for the second person, and test tasks, privacy, purchases, redemption and a shared decision. Grant notification permission.

## Limits

Privacy rules protect answers from other family users, including adults, but do not provide end-to-end encryption against project administrators or someone with access to the unlocked phone. FCM/WorkManager delivery depends on connectivity, permissions and battery policy; the scheduled server check runs once a minute. The active screen uses the shared end timestamp. App Check is not enforced. V1 has no member-removal UI or photo upload; avatars use emoji.

Official references:
- https://firebase.google.com/docs/auth/android/anonymous-auth
- https://firebase.google.com/docs/functions/get-started
- https://firebase.google.com/docs/auth/admin/create-custom-tokens
