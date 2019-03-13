#! /bin/sh

spec='system-images;android-21;google_apis;armeabi-v7a'

case "$TARGET" in
    unit)
        ./gradlew ktlint test --stacktrace
        ;;
    instrumentation)
        # Install the system image
        sdkmanager "$spec"
        # Create and start emulator for the script.
        echo no | avdmanager create avd --force -n test -k "$spec"
        $ANDROID_HOME/emulator/emulator -avd test -no-audio -no-window &
        android-wait-for-emulator
        adb shell input keyevent 82
        ./gradlew connectedCheck --stacktrace
        ;;
esac
