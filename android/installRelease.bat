@echo off
cls

set path_prj=com.rejh.callscreenoff
set package=callscreenoff.rejh.com.callscreenoff
set name_prj=CallScreenOff
set name_act=CsoActivity

if exist C:\android\android-sdk\tools\android.bat set androidsdk=C:\android\android-sdk\
if exist C:\android\sdk\tools\android.bat set androidsdk=C:\android\sdk\

cd %path_prj%
%androidsdk%platform-tools\adb -d install -r %cd%\app\app-release.apk
%androidsdk%platform-tools\adb -d shell am start %package%/.%name_act%

echo.
pause