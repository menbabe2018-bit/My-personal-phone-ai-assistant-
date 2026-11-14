# My personnel phone AI — Android starter (v0.1.0)

Native Android starter for Samsung Galaxy S23 Ultra and Android 8+.

## Included
- Voice input using Android speech recognition (microphone permission required).
- Spoken replies using Android Text-to-Speech.
- Basic local commands: `open Settings`, `open Camera`, or `open <installed app name>`.
- Shortcut to Android Accessibility settings for a future user-authorized phone-control feature.
- GitHub Actions workflow that builds a debug APK and uploads it as an artifact.

## Current limitations
This is an installable foundation, not yet the complete autonomous AI agent. It does not connect to an AI model, plan arbitrary jobs, tap/type/scroll through other apps, send messages, make purchases, or run invisibly in the background. The accessibility service is a placeholder. Later capabilities must be developed and tested with user approval. Android and individual apps restrict some actions.

## Build the APK
1. Open the repository Actions tab.
2. Select **Build My AI APK**.
3. Press **Run workflow**, then confirm.
4. Wait for a green check mark.
5. Open the successful run, scroll to **Artifacts**, and download `MyAI-debug-apk`.
6. Extract the ZIP; the APK is `app-debug.apk`.

## Install on Samsung Galaxy S23 Ultra
1. Download the APK to the phone.
2. Open **My Files → Downloads** and tap `app-debug.apk`.
3. If prompted, allow **Install unknown apps** for the browser or My Files app used to open it. Only install APKs built from this source repository.
4. Tap **Install**, then open **My personnel phone AI**.
5. Allow microphone access for voice input.
6. Test `open Settings` or `open Camera`.

## Build configuration
GitHub Actions uses Java 17, Gradle 8.9, Android SDK platform 35 and build-tools 35.0.0. Output: `app/build/outputs/apk/debug/app-debug.apk`.

## Security roadmap
- Add an AI task planner through a secure backend; never embed a private API key in the APK.
- Use an allow-list of supported actions, not arbitrary code execution.
- Require explicit confirmation before sending, posting, deleting, purchasing, transferring money, or changing account/security settings.
- Keep a visible task log and one-tap pause/stop control.
- Enable Accessibility only through Android system settings and only when the user chooses.

## Next milestones
1. AI task planner and structured action proposals.
2. User approval queue and task history.
3. Limited accessibility actions (inspect visible UI, tap, scroll, type) with confirmation and audit log.
4. Foreground task mode, notifications, allow-listed apps, and stop controls.
