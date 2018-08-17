@echo off

set _4=Spana
set _sc_=..\..\trunk
set _d_=..\..\dist
set _wf_=..\..\Windows-files
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
if NOT exist "%_wf_%" (
	echo ****  ERROR - folder NOT found: "%_wf_%"
	goto quit
)

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
if exist "%_d_%\Spana.cfg" xcopy  /R /Y /D "%_d_%\Spana.cfg"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_d_%\lib\*.jar"  "%_sc_%\%_4%\%_T_%\lib\*.*"
xcopy  /R /Y /D "%_d_%\Chem_Diagr_Help.jar"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_d_%\PlotPDF.jar"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_d_%\PlotPS.jar"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_d_%\SED.jar"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_d_%\Predom.jar"  "%_sc_%\%_4%\%_T_%\*.*"
if exist "%_d_%\DataBase.cfg" xcopy  /R /Y /D "%_d_%\DataBase.cfg"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_d_%\Database.jar"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_d_%\Reactions.db"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_d_%\Reactions.elb"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_d_%\SIT-coefficients.dta"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_d_%\References.txt"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_wf_%\Database.exe"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_wf_%\Spana.exe"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_wf_%\SED.exe"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_wf_%\Predom.exe"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy  /R /Y /D "%_wf_%\ShellChangeNotify.exe"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy /R /Y  "%_sc_%\NSIS\src\ini\.Spana.ini"  "%_sc_%\%_4%\%_T_%\*.*"
xcopy /R /Y  "%_sc_%\NSIS\src\ini\.DataBase.ini"  "%_sc_%\%_4%\%_T_%\*.*"
@echo off
:xit
echo.
echo All done.
:quit
set _sc_=
set _d_=
set _wf_=
set _T_=
set _4=
prompt $p$g
pause
