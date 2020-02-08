; ************************************************************************
; Uppdate_AppInfo-ini.nsi
; The exe-file will uppdade version/date in file "appinfo.ini"
;
; ************************************************************************
; *  Set basic information
; ************************************************************************
Name 'Uppdate "AppInfo.ini"'
Caption 'Uppdate "AppInfo.ini" - ${__DATE__}'
OutFile "Uppdate_AppInfo-ini.exe"
Unicode true

;*** Language
LoadLanguageFile "${NSISDIR}\Contrib\Language files\English.nlf"

; ************************************************************************
; *  Runtime Switches
; ************************************************************************
CRCCheck off
WindowIcon off
SilentInstall silent
AutoCloseWindow true
RequestExecutionLevel "user"

; Best Compression
SetCompress auto
SetCompressor /SOLID lzma
SetCompressorDictSize 32
SetDatablockOptimize on

!define /date DATE "%Y.%m.%d.01"
!define /date DATE2 "%Y-%m-%d"

; ************************************************************************
; *  Main section
; ************************************************************************
Section "Main"
MessageBox  MB_YESNO|MB_DEFBUTTON2 `Uppdade version/date in file$\n\Eq-Calc_Java\PortableApps\App\AppInfo$\nto ${DATE2}?` IDNO end
        WriteINIStr "\Eq-Calc_Java\PortableApps\App\AppInfo\appinfo.ini" "Version" "PackageVersion" "${DATE}"
        WriteINIStr "\Eq-Calc_Java\PortableApps\App\AppInfo\appinfo.ini" "Version" "DisplayVersion" "${DATE2}"
end:
        ;MessageBox MB_OK `The End`
SectionEnd  ;"Main"
