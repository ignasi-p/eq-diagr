@echo off
prompt -$g
mode con:cols=80 lines=20
rem java -jar "store\Chem_Diagr_Help.jar"
rem goto :xit

if not exist "\PortableApps\Eq-Diagr Portable" (
  echo. ERROR - folder does not exist: \PortableApps\Eq-Diagr Portable
  echo. All done.
  goto xit
)
echo.xcopy  store\Chem_Diagr_Help.jar
echo.            "\PortableApps\Eq-Diagr Portable\App\Eq-Diagr\*.*"
xcopy /d /f /y "store\Chem_Diagr_Help.jar" "\PortableApps\Eq-Diagr Portable\App\Eq-Diagr\*.*" >nul
echo.
echo.start Chem_Diagr_Help.exe
start "Chem_Diagr_Help" "\PortableApps\Eq-Diagr Portable\App\Eq-Diagr\Chem_Diagr_Help.exe"

:xit
if "%1"=="" pause
