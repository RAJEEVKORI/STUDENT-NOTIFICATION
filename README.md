# Student Department

An Android app for notice-board and Q&A communication between a college
department and its students. Department users can post notices and answer
student questions; students can read notices and ask questions, both with
optional image/link attachments.

Originally built in April 2024 as a B.Tech final year project.

## Features

- **Role-based access** -- Student and Department each get their own login and dashboard.
- **Notice board** -- Department can post, edit, and delete notices (title, description, date, image). Updates live for everyone via Firebase Realtime Database.
- **Q&A / Comments** -- Students post questions; Department answers them. Both can attach images (camera or gallery) and links.
- **Image viewing** -- Pinch-to-zoom preview for attached images.

## Tech stack

- Java, Android (min SDK 23 / target SDK 33)
- Firebase Realtime Database -- notices and Q&A data
- Firebase Storage -- attached images
- Firebase Auth (SDK only, not connected to a UI flow yet -- see below)
- Glide -- image loading
- FirebaseUI Database -- live-updating notice list
- A few small third-party UI libraries: Clans FAB (floating action menu), ReadMoreTextView, an image pinch-zoom view, and a splash screen library (Splashy)

## Project structure

```
app/src/main/java/com/example/studentdepartment/
├── AuthManager.java              # local login + password hashing (see "Login" below)
├── MainActivity.java             # role picker / splash / auto-login
├── StudentActivity.java, DepartmentActivity.java        # login screens
├── StudentDashboard.java, DepartmentDashborad.java       # post-login home screens
├── NoticeActivity.java           # notice board (list/add/edit/delete)
├── CommentActivity.java          # question list
├── PostCommentActivty.java       # ask a question
├── AnsQuestionActivity.java      # view an answered question
├── AnswerActivity.java           # answer a question
├── NoticeModel.java, CommentModel.java, AttachmentModel.java   # data classes
└── AttachmentAdapter.java, CommentAdapter.java, ImageAdapter.java, LinkAdapter.java  # RecyclerView adapters
```

## Login (currently local, not Firebase)

`AuthManager` handles login with two demo accounts and no backend:

| Role       | Username  | Password |
|------------|-----------|----------|
| Department | `dept123` | `123456` |
| Student    | `std123`  | `123456` |

Passwords are hashed with SHA-256 before being compared or stored -- nothing
is kept in plain text. This is deliberately simple (built-in to Java, no
extra libraries) rather than production-grade, since the goal is something
easy to explain in a project viva, not a real auth system.

`AuthManager`'s method names (`signIn`, `signOut`, `isLoggedIn`) are written
to match what a Firebase Authentication version would look like, so
swapping it in later only means changing the *inside* of `signIn()` --
nothing in `MainActivity`, `StudentActivity`, or `DepartmentActivity` needs
to change.

## Connecting Firebase

Notices, questions, answers, and attached images all go through Firebase
Realtime Database and Firebase Storage already -- that part of the code is
complete and doesn't need edits. It just needs a real Firebase project
connected. Takes about 10 minutes:

### 1. Create a Firebase project

1. Go to the [Firebase console](https://console.firebase.google.com/) and click **Add project**.
2. Give it any name (e.g. "student-department") and finish the setup wizard.

### 2. Register the Android app

1. In the project, click the Android icon to add an app.
2. **Package name** must be exactly `com.example.studentdepartment` (this has to match `applicationId` in `app/build.gradle`).
3. Download the `google-services.json` file it gives you.
4. Place that file at `app/google-services.json` in this project (same folder as `app/build.gradle`). It's already listed in `.gitignore`, so it won't get committed if you push this repo publicly.

### 3. Turn on the Gradle plugin

Open `app/build.gradle` and uncomment the last line:

```gradle
apply plugin: 'com.google.gms.google-services'
```

### 4. Enable Realtime Database

1. In the Firebase console, go to **Build → Realtime Database → Create Database**.
2. Start in **test mode** for development (open read/write, no login required -- fine since this app doesn't use Firebase Auth yet). Switch to real security rules before sharing the app with anyone outside your own testing.

### 5. Enable Storage

1. Go to **Build → Storage → Get Started**.
2. Same as above -- test mode is fine for development.

### 6. Build and run

Sync Gradle, then run the app. Notices and Q&A posts should now save to (and load from) your Firebase project instead of doing nothing.

### Later: real authentication

Right now login is local-only (see above). To switch to real Firebase
Authentication:

1. Enable the sign-in methods you want (Email/Password, Google, etc.) under **Build → Authentication** in the console.
2. Rewrite the inside of `AuthManager.signIn()` to call `FirebaseAuth.getInstance().signInWithEmailAndPassword(...)` (or `FirebaseUI`'s sign-in flow) instead of checking the hardcoded demo accounts.
3. Nothing else needs to change -- every screen already calls `authManager.signIn(...)` / `isLoggedIn()` / `signOut()` rather than touching Firebase directly.

## Getting started

**Prerequisites:** Android Studio (a recent version, which bundles a
compatible JDK), an Android device or emulator running Android 6.0 (API 23) or newer.

1. Clone this repo and open it in Android Studio.
2. Let Gradle sync -- it'll download the dependencies listed in `app/build.gradle`.
3. (Optional, see above) Connect Firebase if you want notices/Q&A to actually persist.
4. Run on a device or emulator.

The app builds and runs without Firebase connected -- notice/Q&A screens
just won't save or load anything until you complete the steps above.

## Known limitations

- **Login is local and unauthenticated** -- fine for a class project demo, not for real users. See "Later: real authentication" above.
- **Dependency versions are reconstructed.** This project was rebuilt from a compiled APK (the original `build.gradle` isn't recoverable from a compiled app), so library versions in `app/build.gradle` are current stable releases rather than whatever the original exact versions were. Everything here is compatible as of this writing, but if a future library update breaks something, pinning to an older version of that one dependency should fix it.
- A couple of small third-party UI libraries (Clans FAB, the image zoom view) come from JitPack rather than Maven Central, since that's where they're published.

## License

MIT -- see [LICENSE](LICENSE).
