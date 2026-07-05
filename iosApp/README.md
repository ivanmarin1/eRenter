# iOS host app

This folder holds the SwiftUI shell that embeds the shared Compose UI.

The `.xcodeproj`/`.xcworkspace` is intentionally **not** committed as raw text because it is
tooling-generated and easy to corrupt by hand. Generate it the standard KMP way:

1. Open the whole project in **Android Studio** with the **Kotlin Multiplatform** plugin
   (or use the KMP project wizard once) — it creates/links `iosApp.xcodeproj` and wires the
   `ComposeApp` framework via the `embedAndSignAppleFrameworkForXcode` Gradle task.
2. Alternatively run the app from Android Studio's run configurations (iosApp) on a simulator.

The two Swift files here (`iOSApp.swift`, `ContentView.swift`) are the only hand-written iOS
source you need; everything else comes from the shared `:composeApp` framework.
