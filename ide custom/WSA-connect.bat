@rem open in ANSI
@echo off
echo WSAの設定から開発者モードを有効にしてください。
adb connect 127.0.0.1:58526
timeout /t 5 /nobreak