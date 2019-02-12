@echo off
rem --- Windows command file to copy files (References.txt, Chem_Diagr_Help.jar, etc)
rem     to the build directory, in order to test DataMaintenance behaviour within
rem     Netbeans, during program development

set _4=DataMaintenance
set _sc_=..\..\trunk
set _d_=..\..\dist
REM set _wf_=..\..\Windows-files
set _T_=build

if NOT exist "!Copy_files_to_%_4%_%_T_%.cmd" (
	echo ****  ERROR - file NOT found: "!Copy_files_to_%_4%_%_T_%.cmd"
	goto quit
)
if NOT exist "%_sc_%" (
	echo ****  ERROR - folder NOT found: "%_sc_%"
	goto quit
)
if NOT exist "%_d_%" (
	echo ****  ERROR - folder NOT found: "%_d_%"
	goto quit
)
REM if NOT exist "%_wf_%" (
REM		echo ****  ERROR - folder NOT found: "%_wf_%"
REM		goto quit
REM )

echo.--------------------------------------------------------
echo.  Copying diverse files to:  %_4%\%_T_%\*.*
echo.  (so that the jar file may be tested)
echo.--------------------------------------------------------
if NOT exist "%_sc_%\%_4%\%_T_%" (
	echo.   folder "%_T_%" not found!
	goto xit
)
set prompt=$G
@echo on
REM if exist "%_d_%\Spana.cfg" xcopy  /R /Y /D "%_d_%\Spana.cfg"  "%_sc_%\%_4%\%_T_%\*.*"
REM xcopy  /R /Y /D "%_d_%\lib\*.jar"  "%_sc_%\%_4%\%_T_%\lib\*.*"
xcopy  /R /Y /D "%_d_%\Chem_Diagr_Help.jar"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_d_%\References.txt"  "%_sc_%\%_4%\%_T_%\*.*"
REM if exist "%_d_%\DataBase.cfg" xcopy  /R /Y /D "%_d_%\DataBase.cfg"  "%_sc_%\%_4%\%_T_%\*.*"
REM xcopy  /R /Y /D "%_d_%\Spana.jar"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_d_%\Reactions.db"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_d_%\Reactions.elb"  "%_sc_%\%_4%\%_T_%\*.*"
REM xcopy  /R /Y /D "%_wf_%\Database.exe"  "%_sc_%\%_4%\%_T_%\*.*"
REM xcopy  /R /Y /D "%_wf_%\Spana.exe"  "%_sc_%\%_4%\%_T_%\*.*"
REM xcopy /R /Y  "%_sc_%\NSIS\src\ini\.Spana.ini"  "%_sc_%\%_4%\%_T_%\*.*"
REM xcopy /R /Y  "%_sc_%\NSIS\src\ini\.DataBase.ini"  "%_sc_%\%_4%\%_T_%\*.*"
@echo off
:xit
echo.
echo All done.
:quit
set _sc_=
set _d_=
REM set _wf_=
set _T_=
set _4=
prompt $p$g
pause
