# Family App

אפליקציית Android משפחתית פרטית, בנויה ב-Kotlin + Jetpack Compose עם Firebase.

## V1
- רוטינות בוקר וערב אישיות, ניתנות לעריכה ולפי ימים.
- סיכום יום: אימוג'י משפחתי, תשובות פרטיות, 5 נקודות לכל תשובה; מילוי היום או אתמול.
- משימות גלויות למשפחה; נותן המשימה קובע נקודות. משימה עצמית = 0 נקודות.
- רשימות פרטיות משותפות לשני משתמשים, ללא נקודות.
- חנות משפחתית ושוברים. מימוש דורש אישור של משתמש משפחתי נוסף.
- "מי מחליט": בחירת משך, pairing, ספירה 5→0, הגרלה מסונכרנת וטיימר שלא ניתן לבטל מתוך האפליקציה.

## Firebase setup
Create/register Android app with package:
`com.amiryashargadot.familyapp`

Download `google-services.json` and place it at:
`app/google-services.json`

Enable Authentication, Cloud Firestore and Cloud Messaging.

> google-services.json is intentionally ignored by git.

## Status
Initial native Android project scaffold committed. Firebase data model, auth/onboarding and feature logic are next.
