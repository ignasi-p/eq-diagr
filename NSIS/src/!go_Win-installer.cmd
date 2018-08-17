@echo off
prompt -$g
echo.=========================================================================
echo.   Making Windows "exe"-setup file ...
echo.
echo.current working directory:
cd
echo.=========================================================================
echo.
set _bdl_=..\..\..\bundles
set _dst_=..\..\..\dist
set _wnf_=..\..\..\Windows-files
if NOT exist \PortableApps\NSISPortable\App\NSIS\makensisw.exe (
	echo.****  ERROR:  file  "\PortableApps\NSISPortable\App\NSIS\makensisw.exe"
	echo.              does not exist!?
	goto xit
)
if NOT exist "\bin\nmake.exe" (
	echo.****  ERROR - file not found: "\bin\nmake.exe"  ****
	goto xit
)
if NOT exist .\makefile_Win-installer (
	echo ****  ERROR:  file  ".\makefile_Win-installer"  does not exist!?
	goto xit:
)
if NOT exist "%_dst_%" (
	echo.****  ERROR:  folder  "%_dst_%"
	echo.              does not exist!?
	goto xit
)
if NOT exist "%_wnf_%" (
	echo.****  ERROR:  folder  "%_wnf_%"
	echo.              does not exist!?
	goto xit
)
if NOT exist "%_wnf_%\PortableApps_exe" (
	echo.****  ERROR:  folder  "%_wnf_%\PortableApps_exe"
	echo.              does not exist!?
	goto xit
)
REM the target directory must exist
if NOT exist "%_bdl_%" (
    echo.creating folder "%_bdl_%"
	mkdir "%_bdl_%"
	if ERRORLEVEL 1 (echo.can NOT create folder. Terminating... & goto xit)
)

if NOT "%1" == "" goto cont

echo.
echo.
echo.Did you check that the DATABASE and REFERENCE LIST are up-to-date ?
echo.(press Ctrl-C to quit this script and check the database,
echo. or [Enter] to continue)
pause>nul
echo.
echo.OK
echo.
echo.
echo.Did you check that the java-launchers (EXE-FILES) are up-to-date ?
echo.(press Ctrl-C to quit this script and check the database,
echo. or [Enter] to continue)
pause>nul
echo.
echo.OK
echo.
echo.
echo.Did you check that the JAR-FILES are up-to-date ?
echo.(press Ctrl-C to quit this script and check the database,
echo. or [Enter] to continue)
pause>nul
echo.
echo.OK
echo.
echo.
echo.Did you check that the JAVA-HELP is up-to-date ?
echo.(press Ctrl-C to quit this script and check the database,
echo. or [Enter] to continue)
pause>nul
echo.
echo.OK
echo.

echo.========== deleting  .cfg  and .ini  files
echo.              in folder:  "%_dst_%" ...
if exist "%_dst_%\.database.ini" del /q /f "%_dst_%\.database.ini" >nul
if exist "%_dst_%\database.cfg" del /q /f "%_dst_%\database.cfg" >nul
if exist "%_dst_%\.datamaintenance.ini" del /q /f "%_dst_%\.datamaintenance.ini" >nul
if exist "%_dst_%\datamaintenance.cfg" del /q /f "%_dst_%\datamaintenance.cfg" >nul
if exist "%_dst_%\.spana.ini" del /q /f "%_dst_%\.spana.ini" >nul
if exist "%_dst_%\spana.cfg" del /q /f "%_dst_%\spana.cfg" >nul
if exist "%_dst_%\.chemdiagrhelp.ini" del /q /f "%_dst_%\.chemdiagrhelp.ini" >nul
if exist "%_dst_%\chem_diagr_help.cfg" del /q /f "%_dst_%\chem_diagr_help.cfg" >nul

echo ========== deleting "Thumbs.db"
del /a:h /q /s  "%_dst_%\Thumbs.db" >nul 2>&1

:cont


echo ========== running:  nMake -f makefile_Win-installer
\bin\nmake -f makefile_Win-installer /noLogo
echo ========== nMake finished.
if exist "%_bdl_%\Eq-Diagr_Java_Setup.exe" (
	echo.set-up exe-file is found in folder "bundles"
) ELSE (
	echo.**** ERROR: file "%_bdl_%\Eq-Diagr_Java_Setup.exe"
	echo.            was NOT created!
)
if "%1"=="" echo.All done.

:xit
prompt $p$g
set _bdl_=
set _dst_=
set _wnf_=
if "%1"=="" (
	echo.
	pause
)
