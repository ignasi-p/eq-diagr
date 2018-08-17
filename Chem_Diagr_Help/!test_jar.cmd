@echo off
prompt -$g
if not exist \PortableApps\Eq-Diagr_Portable (
  echo. ERROR - folder does not exist: \PortableApps\Eq-Diagr_Portable
  echo. All done.
  goto xit
)
echo.xcopy  store\Chem_Diagr_Help.jar
echo.            \PortableApps\Eq-Diagr_Portable\App\Eq-Diagr\*.*
xcopy /d /f /y store\Chem_Diagr_Help.jar \PortableApps\Eq-Diagr_Portable\App\Eq-Diagr\*.* >nul
echo.
echo.start Chem_Diagr_Help.exe
start \PortableApps\Eq-Diagr_Portable\App\Eq-Diagr\Chem_Diagr_Help.exe

:xit
prompt $p$g
if "%1"=="" pause
