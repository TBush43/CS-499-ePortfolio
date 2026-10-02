# OnWeight Coach - Milestone Three

This Android project is the algorithms and data structures enhancement of the original CS 360 OnWeight app. Milestone Two added the student roster and coaching workflow; this milestone adds a reproducible coach review order to that workflow.

## New behavior

- Attendance check-ins and notes now carry dates. Each student can have an optional goal due date and a complete/incomplete state.
- The dashboard shows the three students with the highest review scores and the reasons behind each score.
- The score is 5 for a manual follow-up flag, 3 for no attendance in the last 30 days, 2 for no note in the last 14 days, and 4 for an incomplete goal past its due date. A goal due today is not overdue. These are coaching reminders, not medical or performance judgments.
- Ties are resolved by student name, then ID, so the order is stable.

`FollowUpRanker` scans attendance, note, and goal records into maps keyed by student ID, then uses a bounded priority queue to keep the top `k` students. For `s` students and `r` related records, time is O(s + r + s log k) and additional space is O(s + r + k) in the current service call because the repository returns record copies; the ranker itself uses O(s + r + k) map/heap space in the worst case. Unit tests cover empty input, `k` larger than the roster, ties, missing history, future dates, boundary days, completed goals, scoring, and service integration.

## Build and limitations

Open `source` in Android Studio, sync Gradle, and run the `app` configuration. For command-line verification, run `gradlew.bat testDebugUnitTest assembleDebug lintDebug` with the Android SDK and JDK configured.

This is still an in-memory prototype. Records disappear when the process ends, and there is no authentication. Use fictional student information only. The later database milestone will add permanent storage; the app must not be used for real private student data yet.
