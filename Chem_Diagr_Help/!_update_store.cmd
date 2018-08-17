@echo off
prompt -$g

echo =========================================================================
echo    Updates the javaHelp jar-file in folder "store"
echo ========== current working directory:
cd

if NOT exist .\store (
	echo ****  ERROR:  folder ".\store"  does not exist!?  ****
	goto xit
)
if NOT exist .\store\Chem_Diagr_Help.jar (
	echo ****  ERROR:  file ".\store\Chem_Diagr_Help.jar"  does not exist!?  ****
	goto xit
)
if NOT exist ".\src\javahelp" (
	echo ****  ERROR:  folder ".\src\javahelp"  does not exist!?  ****
	goto xit
)
if NOT exist .\makefile_store (
	echo *****  ERROR:  ".\makefile_store"  does not exist!?  *****
	goto xit
)
if NOT exist "\bin\nmake.exe" (
	echo ****  ERROR - file not found: "\bin\nmake.exe"  ****
	goto xit
)

echo ========== running nMake -f makefile_store ...
\bin\nmake -f makefile_store /noLogo
echo ========== nMake finished.
if "%1"=="" echo.All done.

echo.
echo.xcopy  .\store\Chem_Diagr_Help.jar  ..\..\dist\*.*
xcopy /d /f /y store\Chem_Diagr_Help.jar ..\..\dist\*.* >nul
:xit
prompt $p$g
if "%1"=="" pause
