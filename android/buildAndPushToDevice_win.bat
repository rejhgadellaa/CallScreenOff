@echo off

:config
set path_prj=com.rejh.callscreenoff
set name_prj=CallScreenOff
set android_sdk_extras=\Android-sdk-extras\

:findandroidbat
if exist C:\android\android-sdk\tools\android.bat set androidsdk=C:\android\android-sdk\
if exist C:\android\sdk\tools\android.bat set androidsdk=C:\android\sdk\
REM --> Add more paths here :)

:checkandroidbat
if exist %androidsdk% goto clean_stuff
echo.
echo Error: could not locate android.bat
echo Please edit this batch file and under 'findandroidbat' add the path to your copy of [android-sdk]/tools/android.bat
goto error

:sdkfound
cd %path_prj%
echo.
echo Updating project
echo.
call %androidsdk%tools\android.bat update project -p %cd% -s -t android-21
if not errorlevel 0 goto error

:askifcleanbuild
echo.
echo Run a clean build (this will take longer)?
choice /C YN /N /T 2 /D N /M "(Y/N)"
if errorlevel 2 goto buildincr
if errorlevel 1 goto buildclean
goto error

:buildincr
echo.
echo Building project (INCR)
echo.
call ant debug
REM ant -S debug
if errorlevel 1 goto buildclean
if not errorlevel 0 goto buildclean
goto installapp

:buildclean
echo.
echo Building project (CLEAN)
echo.
call ant clean debug
REM ant -S debug
if errorlevel 1 goto error
if not errorlevel 0 goto error
goto installapp

REM pause

:installapp
echo.
echo Installing app...
echo.

%androidsdk%platform-tools\adb devices
%androidsdk%platform-tools\adb -d install -r bin\%name_prj%-debug.apk
REM emulator %androidsdk%platform-tools\adb -e install -r bin\%name_prj%-debug.apk
if not errorlevel 0 goto error

%androidsdk%platform-tools\adb -d shell am start %path_prj%/.%name_prj%
REM emulator %androidsdk%platform-tools\adb -e shell am start %path_prj%/.%name_prj%
if not errorlevel 0 goto error

goto end

:error
echo.
echo Error!
echo.
pause
goto end

:end
cd ..
echo.
choice /C QYN /N /T 10 /D N /M "Press 'Q' to quit"