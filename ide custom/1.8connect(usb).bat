@echo off
adb devices
adb -s P3PDU17A28004382 tcpip 5555
echo �P�[�u���𔲂��Ă�������
timeout /t 5
adb connect 192.168.1.8:5555
echo �I�����܂�
timeout /t 5
exit