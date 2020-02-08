; ShellChangeNotify.nsi
;-----------------------------------------------

Name "Shell32.dll:SHChangeNotify"
Caption "Shell32.dll :: SHChangeNotify"
Icon "images/Folder.ico"
OutFile "ShellChangeNotify.exe"
Unicode true

;--------------------------------
;Include Version Information
LoadLanguageFile "${NSISDIR}\Contrib\Language files\English.nlf"
!define /date DATE "%Y.%m.%d.01"
  VIProductVersion "${DATE}"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName" "Shell32.dll:SHChangeNotify"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "Comments" "calls Shell32.dll:SHChangeNotify"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright" "I.Puigdomenech"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "Shell32.dll:SHChangeNotify"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion" "${__DATE__}"
;--------------------------------

XPStyle on
RequestExecutionLevel user ;user|highest|admin
SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow

;!include "LogicLib.nsh"

Section ""
  SetOutPath $EXEDIR

  System::Call 'Shell32::SHChangeNotify(i 0x8000000, i 0, i 0, i 0)'
  ;Call RefreshShellIcons
SectionEnd
