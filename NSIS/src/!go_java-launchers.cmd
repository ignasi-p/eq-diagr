@echo off
rem --- Windows script to make java launchers
rem     (exe-files) for the jar-file

prompt -$g

echo =========================================================================
echo    Updating java-launchers (exe-files)
echo ========== current working directory:
cd
set _sc_=..\..\..\trunk
set _wf_=..\..\..\Windows-files

if NOT exist "\PortableApps\NSISPortable\App\NSIS\makensisw.exe" (
	echo.****  ERROR:  file  "\PortableApps\NSISPortable\App\NSIS\makensisw.exe"
	echo.              does not exist!?
	goto xit
)

if NOT exist "\bin\nmake.exe" (
	echo.****  ERROR - file not found: "\bin\nmake.exe"  ****
	goto xit
)
if NOT exist ".\makefile_java-launchers" (
	echo ****  ERROR:  file  ".\makefile_java-launchers"  does not exist!?
	goto xit
)
REM check the source for the PortableApps launchers
if NOT exist ".\PortableApps\Other\Source" (
	echo.****  ERROR:  folder  ".\PortableApps\Other\Source"
	echo.              does not exist!?
	goto xit
)
REM check the destination folders:
if NOT exist "%_wf_%" (
    echo.creating folder "%_wf_%"
	mkdir "%_wf_%"
	if ERRORLEVEL 1 (echo can NOT create folder. Terminating... & goto xit:)
)
if NOT exist "%_wf_%\PortableApps_exe" (
    echo creating folder "%_wf_%\PortableApps_exe"
	mkdir "%_wf_%\PortableApps_exe"
	if ERRORLEVEL 1 (echo can NOT create folder. Terminating... & goto xit:)
)

echo ======== running: nMake -f makefile_java-launchers
\bin\nmake -f makefile_java-launchers /noLogo
echo ======== nMake finished.
echo. "exe"-files (java-launchers) are in folder: "Windows-files"
if "%1"=="" echo.All done.

:xit
prompt $p$g
if "%1"=="" pause
