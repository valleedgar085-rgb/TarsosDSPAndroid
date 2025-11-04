# Build Instructions for TarsosDSP-Android

This document provides detailed instructions for building TarsosDSP-Android from source.

## Prerequisites

### Required Software

1. **Android Studio** 
   - Version: Arctic Fox (2020.3.1) or newer
   - Download: https://developer.android.com/studio

2. **Java Development Kit (JDK)**
   - Version: JDK 8 or higher (JDK 11 recommended)
   - Download: https://adoptium.net/

3. **Android SDK**
   - API Level 21 (Android 5.0) minimum
   - API Level 34 (Android 14) target
   - Installed automatically with Android Studio

4. **Git**
   - For cloning the repository
   - Download: https://git-scm.com/

### Optional Software

- **Gradle**: Included via Gradle Wrapper (./gradlew)
- **Android Emulator**: For testing without physical device

## Clone the Repository

```bash
git clone <repository-url>
cd TarsosDSP-Android
```

## Build Using Android Studio

### 1. Open the Project

1. Launch Android Studio
2. Click "Open" or File → Open
3. Navigate to `TarsosDSP-Android` folder
4. Click "OK"

### 2. Sync Project

Android Studio will automatically sync Gradle. If not:

1. Click "Sync Now" in the notification bar
2. Or File → Sync Project with Gradle Files

### 3. Build the Library

1. Select Build → Make Project (Ctrl+F9 / Cmd+F9)
2. Wait for build to complete
3. Check "Build" tab for any errors

### 4. Build the Example App

1. Select `tarsosdsp-android-example` from the run configuration dropdown
2. Click Build → Build APK
3. APK will be in `tarsosdsp-android-example/build/outputs/apk/debug/`

## Build Using Command Line (Gradle)

### Build Library Only

```bash
./gradlew :tarsosdsp-android:build
```

Output: `tarsosdsp-android/build/libs/tarsosdsp-android-2.5.aar`

### Build Example App

```bash
./gradlew :tarsosdsp-android-example:assembleDebug
```

Output: `tarsosdsp-android-example/build/outputs/apk/debug/tarsosdsp-android-example-debug.apk`

### Build Everything

```bash
./gradlew build
```

### Generate Javadoc

```bash
./gradlew :tarsosdsp-android:javadoc
```

Output: `tarsosdsp-android/build/docs/javadoc/`

### Generate Sources JAR

```bash
./gradlew :tarsosdsp-android:sourcesJar
```

Output: `tarsosdsp-android/build/libs/tarsosdsp-android-2.5-sources.jar`

## Clean Build

Remove all build artifacts:

```bash
./gradlew clean
```

Then rebuild:

```bash
./gradlew build
```

## Running Tests

### Unit Tests

```bash
./gradlew test
```

### Android Instrumentation Tests

```bash
./gradlew connectedAndroidTest
```

Requires a connected device or running emulator.

## Installing on Device

### Install Example App (Debug)

```bash
./gradlew :tarsosdsp-android-example:installDebug
```

### Install Example App (Release)

First, configure signing in `tarsosdsp-android-example/build.gradle`, then:

```bash
./gradlew :tarsosdsp-android-example:installRelease
```

### Uninstall

```bash
./gradlew :tarsosdsp-android-example:uninstallDebug
```

## Publishing to Local Maven

Publish the library to your local Maven repository:

```bash
./gradlew :tarsosdsp-android:publishToMavenLocal
```

Then use in other projects:

```gradle
repositories {
    mavenLocal()
}

dependencies {
    implementation 'be.hogent.tarsos:tarsosdsp-android:2.5'
}
```

## Build Variants

### Debug Build

```bash
./gradlew assembleDebug
```

Features:
- Debugging enabled
- No ProGuard/R8 obfuscation
- Larger APK size

### Release Build

```bash
./gradlew assembleRelease
```

Features:
- Optimized code
- ProGuard/R8 enabled (if configured)
- Smaller APK size
- Requires signing configuration

## Gradle Tasks

View all available tasks:

```bash
./gradlew tasks
```

### Common Tasks

| Task | Description |
|------|-------------|
| `build` | Build all modules |
| `clean` | Delete build directories |
| `assemble` | Build all APKs |
| `assembleDebug` | Build debug APK |
| `assembleRelease` | Build release APK |
| `install` | Install APK on device |
| `test` | Run unit tests |
| `connectedAndroidTest` | Run instrumentation tests |
| `lint` | Run Android Lint |
| `javadoc` | Generate Javadoc |

## Troubleshooting

### Gradle Sync Failed

**Problem**: Gradle sync fails with error

**Solutions**:
1. Check internet connection (for dependency downloads)
2. Invalidate caches: File → Invalidate Caches / Restart
3. Update Gradle wrapper: `./gradlew wrapper --gradle-version=8.0`
4. Check `local.properties` has correct SDK path

### Build Failed - Missing SDK

**Problem**: Android SDK not found

**Solution**:
1. Open Android Studio → SDK Manager
2. Install required SDK levels (API 21-34)
3. Check `local.properties` file:
```properties
sdk.dir=/path/to/Android/Sdk
```

### Out of Memory Error

**Problem**: Build fails with OutOfMemoryError

**Solution**:

Edit `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

### Dependency Resolution Error

**Problem**: Cannot resolve dependencies

**Solutions**:
1. Check internet connection
2. Clear Gradle cache:
   ```bash
   rm -rf ~/.gradle/caches/
   ./gradlew build --refresh-dependencies
   ```
3. Check `repositories` in build.gradle

### APK Not Installing

**Problem**: APK install fails

**Solutions**:
1. Enable USB debugging on device
2. Uninstall old version: `adb uninstall be.hogent.tarsos.dsp.example`
3. Check device storage space
4. Try different USB port/cable

### Tests Failing

**Problem**: Tests fail to run

**Solutions**:
1. For instrumentation tests, ensure device/emulator is running
2. Check logcat for detailed errors: `adb logcat`
3. Verify test dependencies in build.gradle
4. Clean and rebuild: `./gradlew clean test`

## Build Configuration

### Changing Version

Edit `tarsosdsp-android/build.gradle`:

```gradle
android {
    defaultConfig {
        versionCode 2
        versionName "2.6"
    }
}
```

### Changing SDK Versions

Edit `tarsosdsp-android/build.gradle`:

```gradle
android {
    compileSdk 34
    defaultConfig {
        minSdk 21
        targetSdk 34
    }
}
```

### Adding Dependencies

Edit `tarsosdsp-android/build.gradle`:

```gradle
dependencies {
    implementation 'com.example:library:1.0.0'
}
```

## Build Output Locations

### Library Outputs

- **AAR**: `tarsosdsp-android/build/libs/tarsosdsp-android-2.5.aar`
- **Sources JAR**: `tarsosdsp-android/build/libs/tarsosdsp-android-2.5-sources.jar`
- **Javadoc JAR**: `tarsosdsp-android/build/libs/tarsosdsp-android-2.5-javadoc.jar`
- **Classes**: `tarsosdsp-android/build/intermediates/javac/`

### Example App Outputs

- **Debug APK**: `tarsosdsp-android-example/build/outputs/apk/debug/`
- **Release APK**: `tarsosdsp-android-example/build/outputs/apk/release/`
- **Logs**: `tarsosdsp-android-example/build/outputs/logs/`

## Performance Tips

### Faster Builds

1. **Enable Gradle Daemon** (enabled by default in modern Gradle)
   
2. **Parallel Builds**:
   Edit `gradle.properties`:
   ```properties
   org.gradle.parallel=true
   org.gradle.workers.max=4
   ```

3. **Build Cache**:
   ```properties
   org.gradle.caching=true
   ```

4. **Kotlin DSL**: Stick with Groovy DSL for faster configuration

5. **Incremental Builds**: Don't clean unless necessary

### Reducing APK Size

1. Enable ProGuard/R8
2. Use APK splits for different architectures
3. Remove unused resources
4. Use vector drawables instead of PNGs
5. Enable resource shrinking

## Continuous Integration

### GitHub Actions Example

```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Build with Gradle
        run: ./gradlew build
      - name: Run Tests
        run: ./gradlew test
```

## Support

For build issues:
1. Check this document
2. Search existing issues
3. Check Android Studio logs
4. File an issue with build output

## Additional Resources

- [Gradle Documentation](https://gradle.org/docs/)
- [Android Developer Guide](https://developer.android.com/guide)
- [Android Build System](https://developer.android.com/studio/build)

---

Last updated: 2024-11-04
