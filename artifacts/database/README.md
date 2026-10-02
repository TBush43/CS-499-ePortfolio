# OnWeight Coach - Milestone Four

This is the database enhancement of the CS 360 OnWeight Android artifact. It builds on the Milestone Two coaching workflow and the Milestone Three follow-up ranking.

## What changed

- `SqliteCoachRepository` replaces the temporary repository used by the screens. The roster, goals, notes, attendance, and follow-up flags now survive an app restart.
- `CoachDatabaseHelper` creates four related SQLite tables: `students`, `goals`, `attendance`, and `session_notes`. Student IDs connect the tables with foreign keys. Related-date indexes support activity lookups.
- Goal text and its due date/completion state are stored together. Notes and attendance are stored once as dated rows, rather than as duplicate counters or serialized lists.
- Queries bind student IDs as parameters. The service still validates user-entered fields. Database writes use transactions where a coaching action spans a related-record update.
- Android instrumentation tests cover reopening the database, goal removal and completion, names containing an apostrophe, and foreign-key enforcement. Existing JVM tests cover service rules and ranking.

## Open and verify

Open `source/` as the project in Android Studio. Sync Gradle and run the `app` configuration. With the Android SDK and JDK available, `gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug` builds the app and both test APKs. Run `SqliteCoachRepositoryTest` on an Android emulator or device to execute the database tests. Those tests use a separate database and do not erase the app's ordinary data.

To see persistence yourself, add a fictional student, goal, note, and attendance check-in. Fully close and reopen the app; the records should remain.

## Scope and privacy

This is a local SQLite implementation of the planned persistent repository. It uses Android's built-in SQLite tools rather than Room, keeping the relational schema and queries visible for review. Version 1 is the first database schema; a future schema change will need an explicit migration. No migration from the earlier in-memory app is needed because it never saved records.

This remains a capstone prototype. There is no coach sign-in, access control, or database encryption. The manifest disables backup, and Android 12+ extraction rules exclude the database from cloud backup and device transfer. These controls alone do not make the app suitable for real student information. Use fictional records only.
