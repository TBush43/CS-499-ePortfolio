# OnWeight Coach — Milestone Two

This is the software design and engineering enhancement of the original CS 360 OnWeight Android prototype. It remains a Java Android app, but the false login, static weight dashboard, and unused SMS screen have been replaced with a coach-facing workflow.

## What works

- Create a student with a name and belt rank.
- Open a student profile, save a training goal, add session notes, record an attendance check-in, and flag follow-up.
- See an empty roster or notes message when there is no content.
- Validate required fields and length limits before updating records.

The screen classes handle Android views and navigation. `CoachViewModel` passes requests to `CoachService`, which applies validation and uses the `CoachRepository` interface. `InMemoryCoachRepository` is a temporary implementation; it is deliberately replaceable by a persistent Room repository in the later database milestone. `Student` is an immutable record so the UI cannot alter repository data by accident.

## Important limitation

There is no authentication or permanent storage yet. Data disappears when the app process ends. Do not enter real student information. The app displays this warning on the opening screen. Backup is disabled and the original SMS permission has been removed, but those changes alone do not make the prototype suitable for real private records.

## Build and test

Open `source` in Android Studio and run the `app` configuration. The project uses the Android SDK and JDK bundled with Android Studio. From a terminal with the SDK and JDK configured, run `gradlew.bat testDebugUnitTest assembleDebug`. Seven `CoachServiceTest` tests cover the empty state, student creation, validation, profile updates, missing IDs, and immutable notes. An emulator was not available during Milestone Two preparation, so the screens still need a manual device pass.

## Next milestones

The algorithms milestone will add explainable follow-up prioritization. The database milestone will replace the temporary repository with Room entities, related records, migrations, tests, and appropriate protection for student data before real use.
