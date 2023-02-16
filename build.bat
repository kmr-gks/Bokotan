@echo off
pushd %userprofile%\AppData\Local\Android\Sdk\tools
emulator -avd Pixel_6_API_33
pushd %~dp0
gradlew assembleDebug
pause
adb install app\build\outputs\apk\debug\app-debug.apk
pause