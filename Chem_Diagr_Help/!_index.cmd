@echo off
rem Note: the path of this file can not contain a space because
rem       the JavaHelp indexer can not handle that
prompt -$g

echo =========================================================================
echo    Updates the JavaHelp search indexes
echo ========== current working directory:
cd

if NOT exist .\makefile_index (
	echo *****  ERROR:  ".\makefile_index"  does not exist!?  *****
	goto xit
)
if NOT exist "\bin\nmake.exe" (
	echo ****  ERROR - file not found: "\bin\nmake.exe"  ****
	goto xit
)
if NOT exist .\src\html (
	echo.*****  ERROR:  help contents does not exist!?
	echo.               should be in ".\src\html" ...
	goto xit
)
if NOT exist ".\src\javahelp" (
    echo creating folder ".\src\javahelp"
	mkdir ".\src\javahelp"
	if ERRORLEVEL 1 (echo can NOT create folder. Terminating... & goto xit)
)

echo ========== running nMake -f makefile_index
\bin\nmake -f makefile_index /noLogo
echo ========== nMake finished.
if "%1"=="" echo.All done.

:xit
prompt $p$g
if "%1"=="" pause
