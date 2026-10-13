# OnWeight Coach — final app source

This is the final working copy of Thomas Bush's CS 499 Android artifact. The Milestone Two, Three, and Four source snapshots remain in their respective folders; their test files received brief comments for the final portfolio, but their behavior is unchanged. The original CS 360 source remains untouched. Open [`source/`](source/) in Android Studio as its own Gradle project.

## What the app does

A coach can add fictional students, edit their names and belt ranks, record attendance, set dated training goals, mark goals complete, add session notes, and flag a student for follow-up. The dashboard shows a top-three review order with reasons. Records persist in four related local SQLite tables. Deleting a student requires confirmation and also removes that student's goals, check-ins, and notes through foreign-key cascade.

The final polish moves database reads and writes to a single background worker, keeps the screen responsive, handles storage failures, and adds tests for editing and deleting students. Short comments explain key behavior without restating every line.

## Why this meets the three enhancement categories

- **Software design and engineering:** Separate activities, a view model, service rules, and repository implementations keep screens, coaching logic, and storage distinct. Input validation and error states are testable.
- **Algorithms and data structures:** The follow-up ranker combines recent attendance, notes, overdue goals, and manual flags with hash maps and a bounded priority queue. It uses a deterministic tie-break order and shows the reasons for each nonzero score.
- **Databases:** SQLite tables use primary and foreign keys, cascades, date indexes, bound query arguments, and transactions. The app preserves records between launches. The final version retains the Milestone Four schema, so existing version-1 app data does not require migration.

## Build and verify

In Android Studio, open `source/`, sync Gradle, and run the `app` configuration on an Android 14 or newer emulator/device. The project targets API 36 and requires the Android SDK and a compatible JDK. From `source/`, run `gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug` on Windows. Run `SqliteCoachRepositoryTest` on a device/emulator for the database tests; they use a separate test database.

For a manual check: add a fictional student; edit the name and belt; save a goal and note; record a check-in; reopen the app to confirm persistence; then delete the student and verify the roster no longer lists them. The coach-review order should update after each change.

## Privacy boundary

This is a capstone prototype, not a production student-record system. It has no sign-in, access control, or database encryption. Backup is disabled and data-extraction rules exclude the database, but those controls do not make it safe for actual student information. Use fictional data only. A production version would need authentication, access-control tests, encryption, retention rules, and a reviewed migration strategy before real records could be stored.
