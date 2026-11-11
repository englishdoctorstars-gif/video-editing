# Google Play-তে Vocabsolution প্রকাশের গাইড

এই গাইড ধাপে ধাপে বলে দেয় কী করতে হবে। কোডের দিকের প্রস্তুতি সম্পূর্ণ। নিচের যে কাজগুলো শুধু আপনিই করতে পারবেন, সেগুলো ☐ চিহ্ন দেওয়া।

| বিষয় | অবস্থা |
|---|---|
| Package | `com.englishdoctor.vocabsolution` |
| Version | 1.0.0 (versionCode 4) |
| targetSdk / compileSdk | 36 (Android 16), Play-এর বর্তমান নিয়ম অনুযায়ী |
| minSdk | 26 (Android 8.0) |
| বান্ডেল | AAB, R8 দিয়ে সংকুচিত; native lib ১৬ KB page-aligned |
| অনুমতি | শুধু POST_NOTIFICATIONS (আর WorkManager-এর WAKE_LOCK, BOOT_COMPLETED, NETWORK_STATE)। INTERNET নেই। |

## ১. Upload key তৈরি (একবারই, নিজের কম্পিউটারে) ☐

```bash
bash tools/release/create-upload-key.sh
```

- এটি তৈরি করে `vocabsolution-upload.jks` আর `keystore.properties`। দুটোই git-ignored, তাই কখনো রিপোজিটরিতে যাবে না।
- `.jks` ফাইল আর পাসওয়ার্ড পাসওয়ার্ড-ম্যানেজারে এবং একটি অফলাইন ব্যাকআপে রাখুন।
- Play App Signing চালু থাকলে Google নিজের app-signing key রাখে। তাই upload key হারালেও Play Console-এর মাধ্যমে নতুন upload key নেওয়া যায়।

## ২. সাইন করা AAB তৈরি

পদ্ধতি **ক: নিজের কম্পিউটারে**

```bash
./gradlew :app:bundleRelease
# → app/build/outputs/bundle/release/app-release.aab
```

পদ্ধতি **খ: GitHub Actions দিয়ে** (`.github/workflows/release.yml`) ☐

রিপোজিটরির Settings → Secrets and variables → Actions-এ চারটি secret যোগ করুন:

| Secret | মান |
|---|---|
| `VOCAB_UPLOAD_KEYSTORE_BASE64` | `base64 -w0 vocabsolution-upload.jks` কমান্ডের আউটপুট |
| `VOCAB_UPLOAD_STORE_PASSWORD` | keystore পাসওয়ার্ড |
| `VOCAB_UPLOAD_KEY_ALIAS` | `upload` |
| `VOCAB_UPLOAD_KEY_PASSWORD` | key পাসওয়ার্ড (স্ক্রিপ্টে keystore পাসওয়ার্ডই) |

এরপর `v1.0.0` ট্যাগ পুশ করুন, অথবা Actions-এ "Play release bundle" হাতে চালান। ওয়ার্কফ্লো চলার পর artifact হিসেবে `app-release.aab` ও `mapping.txt` পাওয়া যাবে।

## ৩. Play Console-এ অ্যাপ তৈরি ☐

1. https://play.google.com/console → **Create app**
2. App name: `Vocabsolution - English Doctor` · Default language: **বাংলা (bn-BD)** · App · Free
3. Play App Signing: ডিফল্ট (Google-managed key) রাখুন।

> **নতুন ব্যক্তিগত ডেভেলপার অ্যাকাউন্ট হলে:** Production-এ যাওয়ার আগে একটি **Closed testing** ট্র্যাক চালাতে হয়। এ সময়ে নির্দিষ্ট সংখ্যক টেস্টার একটানা নির্দিষ্ট দিন ধরে অ্যাপটি ব্যবহার করবেন। লেখার সময় নিয়ম ছিল ১২ জন টেস্টার, ১৪ দিন। Console-এর Dashboard বর্তমান সংখ্যা দেখায়, সেটি মিলিয়ে নিন। শুরুতে internal/closed ট্র্যাকে AAB আপলোড করুন, শিক্ষার্থী বা সহকর্মীদের টেস্টার হিসেবে যোগ করুন।

## ৪. Store listing ☐

সব লেখা ও ছবি `fastlane/metadata/android/`-এ রাখা আছে। Console-এ কপি করুন অথবা fastlane `supply` দিয়ে আপলোড করুন।

| ঘর | bn-BD | en-US |
|---|---|---|
| App name (≤৩০) | `bn-BD/title.txt` | `en-US/title.txt` |
| Short description (≤৮০) | `bn-BD/short_description.txt` | `en-US/short_description.txt` |
| Full description (≤৪০০০) | `bn-BD/full_description.txt` | `en-US/full_description.txt` |
| App icon 512×512 | `images/icon.png` | একই |
| Feature graphic 1024×500 | `images/featureGraphic.png` | একই |
| Phone screenshots 1080×1920 | `images/phoneScreenshots/` (৮টি, বাংলা ক্যাপশন) | ৮টি, ইংরেজি ক্যাপশন |
| What's new | `changelogs/4.txt` | `changelogs/4.txt` |

ছবিগুলো আসল অ্যাপ স্ক্রিন থেকে তৈরি। অ্যাপের UI বদলালে আবার তৈরি করুন:

```bash
./gradlew :app:recordPaparazziDebug --tests '*store*'
pip install pillow && python3 -I tools/release/collect_store_assets.py
```

- **App category:** Education
- **Tags:** Education, Language learning (Console-এর তালিকা থেকে)
- **Contact email:** englishdoctorbd@gmail.com (ফোন ও ওয়েবসাইট ঐচ্ছিক)

## ৫. Privacy policy URL ☐

Play-তে একটি **পাবলিক URL** দিতে হয়। লেখা প্রস্তুত আছে: [`docs/privacy-policy.md`](privacy-policy.md)। এই পাতাটি যেকোনো পাবলিক জায়গায় প্রকাশ করুন, যেমন:
- English Doctor-এর ওয়েবসাইটের একটি পাতা, অথবা
- রিপোজিটরি পাবলিক হলে GitHub Pages (Settings → Pages → `docs/` ফোল্ডার)।

## ৬. App content ফর্মের উত্তর ☐

অ্যাপের কোডের সাথে মিলিয়ে নিচের উত্তরগুলো ঠিক করা হয়েছে।

**Privacy policy:** ধাপ ৫-এর URL।

**Ads:** No, my app does not contain ads.

**App access:** All functionality is available without special access (কোনো লগইন নেই)।

**Content rating (IARC questionnaire):**
- Category: *Reference, News, or Educational*
- Violence, sexuality, offensive language, controlled substances, gambling: সবকটিতে **No**
- Users can interact or exchange content: **No** (শেয়ার কার্ড ফোনের সাধারণ share sheet দিয়ে যায়, অ্যাপের ভেতরে কোনো যোগাযোগ নেই)
- Shares user's location: **No** · Digital purchases: **No**
- প্রত্যাশিত রেটিং: সব বয়সের জন্য (Everyone / 3+)

**Target audience and content:**
- সুপারিশ: **13–15, 16–17, 18+** নির্বাচন করুন। মূল ব্যবহারকারী HSC, চাকরি ও IELTS শিক্ষার্থী।
- ১৩ বছরের কম বয়স যোগ করলে Google-এর Families নীতি প্রযোজ্য হয়। অ্যাপটি সেই নীতির শর্তও মানে (কোনো বিজ্ঞাপন বা তথ্য সংগ্রহ নেই), তবে রিভিউ দীর্ঘ হতে পারে।
- "Appeals to children?": **No**

**Data safety:**
- Does your app collect or share any of the required user data types? **No**
  - কারণ: অ্যাপে INTERNET অনুমতি নেই। সব তথ্য ফোনে থাকে। Android Auto Backup ব্যবহারকারীর নিজের Google ব্যাকআপে যায়, আর Google-এর নির্দেশনা অনুযায়ী এটি "collection" নয়।
- Is all user data encrypted in transit? N/A (কিছুই পাঠানো হয় না)
- Data deletion: অ্যাপের সেটিংস → "সব অগ্রগতি মুছে ফেলুন" অথবা আনইনস্টল।

**Government app:** No · **Financial features:** None · **Health:** No · **News app:** No

**Foreground service / special permissions:** প্রযোজ্য নয় (FOREGROUND_SERVICE অনুমতি manifest থেকে সরানো হয়েছে)।

## ৭. রিলিজ ☐

1. Testing → Closed testing (বা Internal testing) → Create release → `app-release.aab` আপলোড → release notes `changelogs/4.txt` থেকে।
2. Pre-launch report দেখুন। Google বিভিন্ন ফোনে অ্যাপ চালিয়ে crash বা accessibility সমস্যা দেখায়। এটাই প্রথমবার আসল ডিভাইসে অ্যাপ চলার রিপোর্ট, তাই মনোযোগ দিয়ে দেখুন।
3. শর্ত পূরণ হলে Production → Create release → একই AAB → Review → Roll out।

## ৮. পরের সংস্করণ

- `app/build.gradle.kts`-এ `versionCode` এক বাড়ান (৫, ৬, …) আর `versionName` বদলান।
- `fastlane/metadata/android/*/changelogs/<versionCode>.txt` লিখুন।
- `vX.Y.Z` ট্যাগ পুশ করুন, অথবা `./gradlew :app:bundleRelease` চালান।
