; Eq-Diagr_Java_Setup.nsi
; It will install "Chemical Equilibrium Diagrams (java)" into a directory that the user selects,
;
;--------------------------------
; The name of the installer
Name "Chemical Equilibrium Diagrams (Java) (KTH)"
Caption "Chemical Equilibrium Diagrams (java) - ${__DATE__} - (KTH)"
Icon "images/Eq-Diagr_Java.ico"
OutFile "Eq-Diagr_Java_Setup-KTH.exe"  ;The file to write
Unicode true

; Directories containing the files to be installed
; the NSIS scripts directory
!define S_ "."
; the Source directories
!define S "C:\!M\Ignasi\Eq-Calc_Java\dist"
!define SW "C:\!M\Ignasi\Eq-Calc_Java\Windows-files"
; the Source directory for PortableApps
!define SPA ".\PortableApps"
!define SPAE "C:\!M\Ignasi\Eq-Calc_Java\Windows-files\PortableApps_exe"
;--------------------------------
; Libraries and macros needed in this script:
!include "LogicLib.nsh"
!include "FileFunc.nsh"
!insertmacro GetDrives
!insertmacro GetExeName
!insertmacro un.Locate
!include "WordFunc.nsh"
!insertmacro un.WordFind
!insertmacro "GetRoot"
;--------------------------------
;Include Version Information in the "setup.exe" installer
LoadLanguageFile "${NSISDIR}\Contrib\Language files\English.nlf"
!define /date DATE "%Y.%m.%d.01"
  VIProductVersion "${DATE}"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName" "Chemical Equilibrium Diagrams (Java) (KTH)"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "Comments" "Installs Chemical Equilibrium Diagrams (Java) (KTH)"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright" "(c)I.Puigdomenech 2012-2019"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "Chemical Equilibrium Diagrams (Java) (KTH)"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion" "${__DATE__}"
;--------------------------------
;Things that need to be extracted on startup
; (keep these lines before any File command!)
; Use ReserveFile for your own InstallOptions INI files too!
ReserveFile "${NSISDIR}\Plugins\x86-unicode\InstallOptions.dll"
ReserveFile "ini\Eq-Diagr_Setup_InstType.ini"
ReserveFile "ini\Eq-Diagr_Setup_FileAssn.ini"
;--------------------------------
; The default installation directory
; normally $PROGRAMFILES = "C:\Program Files"
InstallDir "$PROGRAMFILES\Eq-Diagr"

; Registry key to check for directory
; (so if you install again, it will overwrite the old one automatically)
InstallDirRegKey HKCU "Software\Eq-Diagr" "Install_Dir"

;--------------------------------
; script parameters
CRCCheck on
ShowInstDetails show
ShowUninstDetails show
SetOverwrite ifnewer
; requested execution level for Windows Vista or later
RequestExecutionLevel user ;user|highest|admin

XPStyle on
LicenseText "Chemical Equilibrium Diagrams (Java)  (${__DATE__})$\r$\nNo warranties of any kind"
LicenseData "disclaimer.txt"
;--------------------------------
; variables
Var Inst_HDD      ;Type of Installation: Hard-Disk-Drive (or else Removable Memory)
Var reg_DAT       ;for file-association custom page
Var reg_PLT       ;for file-association custom page
Var Message       ;for file-association custom page
Var Install_Start_Menu_ShortCuts
Var Start_Menu_Folder
Var Start_Menu_Folder_Def
Var Installed_Folder
Var PortableApps
Var notAdmin
;----------------------------------------------------------
Function .onInit
    ; ---- make sure only one instance of the setup program
    ;      is running simultaneously
    System::Call 'kernel32::CreateMutexA(i 0, i 0, t "EqDiagr_Java_installer") i .r1 ?e'
    Pop $R0
    StrCmp $R0 0 +3
        MessageBox MB_OK|MB_ICONEXCLAMATION|MB_TOPMOST "The installer is already running."
        Abort
    ; ---- Extract InstallOptions files (check options for file association)
    ;$PLUGINSDIR will automatically be removed when the installer closes
    InitPluginsDir
    File "/oname=$PLUGINSDIR\InstType_J.ini" "ini\Eq-Diagr_Setup_InstType.ini"
    File "/oname=$PLUGINSDIR\FileAssn_J.ini" "ini\Eq-Diagr_Setup_FileAssn.ini"
    ; ---- set variables for menu shortcuts
    ;default- install Start-Menu shortcuts
    StrCpy $Install_Start_Menu_ShortCuts  1
    StrCpy $Start_Menu_Folder_Def "Equilibrium Diagrams (java)"
    StrCpy $Start_Menu_Folder "$Start_Menu_Folder_Def"
    ; ---- default install type
    StrCpy  $Inst_HDD  "1"
    StrCpy  $PortableApps ""
    ; ----
    ; call UserInfo plugin to get user info.  The plugin puts the result in the stack
    UserInfo::getAccountType
    ; pop the result from the stack into $0
    Pop $0
    ; compare (case-insensitive) with the string "Admin" to see if the user is admin.
    StrCmp $0 "Admin" +4
    ; if user is not admin, installation should be to an alternative folder
    ;     will not be able to write to "C:\Program files"
    StrCpy $notAdmin "1"
    StrCpy $INSTDIR "$PROFILE\Eq-Diagr"  ;if not admin
    Return
    StrCpy $notAdmin "0"
FunctionEnd  ;.onInit

;--------------------------------
; Pages
;--------------------------------
Page license
;Custom page. InstallOptions gets called in SetInstType.
Page custom  SetInstType  InstTypePageLeave  ": Type of installation"
Page components  "PreFn_Components"
Page directory "" "" ""
Page custom  StartMenuGroupSelect  ""  ": 'Start Menu' Folder"
;Custom page. InstallOptions gets called in SetFileAssn.
Page custom  SetFileAssn  FileAssnPageLeave  ": File Associations"
Page instfiles  "" "" "" /ENABLECANCEL   ;execute the "sections" chosen in page "components"

UninstPage uninstConfirm
UninstPage instfiles

;----------------------------------------------------------
;     Sections (selected in Page components)
;----------------------------------------------------------
Section "Jar and exe-files  (required)"
    SectionIn RO  ;RO = read-only
    ;the real installation in performed in Section ""
SectionEnd

;----------------------------------------------------------
; Optional sections (may be disabled by the user)
SectionGroup /e "Shortcuts" Shortcuts_id

    ;--------------------------------
    Section "Start Menu Shortcuts" Start_Menu_Shortcuts_id
      StrCmp  "$Inst_HDD" "0"  end   ;if not HDD then no ShortCuts
      StrCpy $Install_Start_Menu_ShortCuts 1
      end:
    SectionEnd

    Function .onSelChange  ;Capture changes in the "components" Page
      ;Note: the use of variable Start_Menu_Shortcuts_id
      ;   requires this function to be placed *after*
      ;   the corresponding "Section Start Menu Shortcuts"
      ;Get the SF_SELECTED flag of section ${Start_Menu_Shortcuts_id}
      ;  into variable $Install_Start_Menu_ShortCuts
      SectionGetFlags  ${Start_Menu_Shortcuts_id}  $Install_Start_Menu_ShortCuts
    FunctionEnd

    ;--------------------------------
    Section /o "Desktop Shortcut" Desktop_Shortcut_id
      StrCmp  "$Inst_HDD" "0"  end   ;if not HDD then no ShortCuts
      DetailPrint "------ Creating the Desktop shortcut"
      CreateShortCut "$DESKTOP\Spana.lnk" "$INSTDIR\Spana.exe" "" "$INSTDIR\Spana.exe" 0
      end:
    SectionEnd

    ;--------------------------------
    Section /o "'Send to' Shortcut" Send_to_Shortcut_id
      StrCmp  "$Inst_HDD" "0"  end   ;if not HDD then no ShortCuts
      DetailPrint "------ Creating the 'Send to' shortcut"
      CreateShortCut "$SENDTO\Spana.lnk" "$INSTDIR\Spana.exe" "" "$INSTDIR\Spana.exe" 0
      end:
    SectionEnd

SectionGroupEnd
;----------------------------------------------------------

;----------------------------------------------------------
;     Functions associated with pages
;----------------------------------------------------------
Function "PreFn_Components"   ; Skip component page if not needed
    StrCmp  "$Inst_HDD"  "1" end   ;if not HDD then no Components
        Abort     ;do not show page
    end:
FunctionEnd

Function SetInstType    ;Display the InstallOptions dialog
    Push $0
    Push $0
    InstallOptions::dialog "$PLUGINSDIR\InstType_J.ini"
    Pop $0
    Pop $0
FunctionEnd

Function InstTypePageLeave
    ReadINIStr $Inst_HDD "$PLUGINSDIR\InstType_J.ini" "Field 2" "State"
    StrCmp  "$Inst_HDD"  "1"  end ; HDD=1 means standard install
        StrCpy $Inst_HDD "0"      ; HDD=0 means portable install
        StrCpy $Install_Start_Menu_ShortCuts  0
        StrCpy $reg_DAT 0
        StrCpy $reg_PLT 0
        SectionSetText  ${Start_Menu_Shortcuts_id}  ""
        SectionSetText  ${Send_to_Shortcut_id}  ""
        SectionSetText  ${Desktop_Shortcut_id}  ""
        SectionSetText  ${Shortcuts_id}  ""
        ; ---- Check if there is a "\PortableApps" folder somewhere
        StrCpy $PortableApps ""
		; ---- This will choose the highest drive letter with a
		;      \PortableApps folder.  If two USB drives are available,
		;      for example E: and F:, both with a \PortableApps folder,
		;      then $PortableApps will be "F:\PortableApps".
        ${GetDrives} "HDD+FDD" GetDrivesFn  ; get hard and floppy disks
        StrCmp $PortableApps "" end
		; ---- a \PortableApps folder was found
		;      use it as a default installation folder
            StrCpy $INSTDIR "$PortableApps\Eq-Diagr_Portable"
    end:
FunctionEnd

Function GetDrivesFn ; ${GetDrives} puts drive letter in $9, drive type in $8
    StrCmp $8 "FDD" "" CheckPortableApps
    StrCmp $9 "A:\" end
    StrCmp $9 "B:\" end
    CheckPortableApps:
        IfFileExists "$9PortableApps" 0 End
                StrCpy $PortableApps "$9PortableApps"
    end:
    Push $0
FunctionEnd

Function SetFileAssn    ;Display the InstallOptions dialog
    StrCmp  "$Inst_HDD" "1" +2   ;if not HDD then no File Associations
        Abort     ;do not show page
    ;StrCmp  "$notAdmin" "0" +2   ;if not admin then no File Associations
    ;    Abort     ;do not show page
    Push $0
    Push $0
    InstallOptions::dialog "$PLUGINSDIR\FileAssn_J.ini"
    Pop $0
    Pop $0
FunctionEnd

Function FileAssnPageLeave
    ReadINIStr $reg_DAT "$PLUGINSDIR\FileAssn_J.ini" "Field 2" "State"
    ReadINIStr $reg_PLT "$PLUGINSDIR\FileAssn_J.ini" "Field 3" "State"
    StrCpy $Message ""
    ${If} $reg_DAT = 1
        StrCpy $Message "DAT"
    ${EndIf}
    ${If} $reg_PLT = 1
        ${If} $reg_DAT = 1
            StrCpy $Message "$Message and PLT"
        ${Else}
            StrCpy $Message "PLT"
        ${EndIf}
    ${EndIf}
    ${If} $reg_DAT = 1
    ${OrIf} $reg_PLT = 1
        MessageBox MB_OKCANCEL|MB_ICONQUESTION|MB_TOPMOST 'Are you sure that you wish to associate$\n"Chemical Equilibrium Diagrams (java)"$\nwith $Message files?' IDOK +2
        Abort
        StrCpy $Message ""
    ${EndIf}
FunctionEnd

Function StartMenuGroupSelect
;Display the select start-menu directory dialog  (uses startMenu.dll)
    StrCmp  "$Inst_HDD" "1"  +2   ;if not HDD then no Start Menu
        Abort     ;do not show page
    StrCmp  $Install_Start_Menu_ShortCuts 1  +2
        Abort     ;do not show page
    Push $R1
    StartMenu::Select /autoadd /lastused "$Start_Menu_Folder" "$Start_Menu_Folder_Def"
    Pop $R1
    StrCmp $R1 "success" success
    StrCmp $R1 "cancel" done
    ; error
        MessageBox MB_OK|MB_ICONEXCLAMATION|MB_TOPMOST $R1
        StrCpy "$Start_Menu_Folder" "$Start_Menu_Folder_Def" ;use default
        Return
    success:
        Pop "$Start_Menu_Folder"
    done:
        Pop $R1
FunctionEnd

;----------------------------------------------------------
;     Main Section
;----------------------------------------------------------
;     The stuff to install
;----------------------------------------------------------
Section  ""

  DetailPrint '------ Installation directory = "$INSTDIR"'
  ; Set output path to the installation directory.
  SetOutPath "$INSTDIR"
  ; for portable install
  StrCmp  "$Inst_HDD"  "1" noPA   ;if HDD=1 use standard installation
	; portable installation
	;StrCpy user_var(destination) str [maxlen] [start_offset]
	StrCpy $R5 "$INSTDIR" 12 3	; Extract 12 characters from $INSTDIR
								; leaving out 3 first characters (for example C:\)
	StrCmp "$R5" "PortableApps" +1 noPA
    ; $INSTDIR is equal to "N:\PortableApps\Eq-Diagr_Portable"
    SetOutPath "$INSTDIR\App\Eq-Diagr"
    Goto cont
  noPA:
	; portable installation but not in a \PortableApps folder
    SetOutPath "$INSTDIR"
  cont:
  ; -------------  copy files to installation directory
  ; ${S}  directory containing the files to be installed
  DetailPrint "------ Copying files:"

  File "${S}\Test.plt"
  IfFileExists "$OUTDIR\Test.plt" installation_Path_OK
        DetailPrint '------ Error: Could not write files to path "$OUTDIR"'
        MessageBox MB_OK|MB_ICONSTOP|MB_TOPMOST 'Error: Could not write files to "$OUTDIR"'
        Abort
  installation_Path_OK:

  File "${SW}\Chem_Diagr_Help.exe"
  File "${S}\Chem_Diagr_Help.jar"
  File "${S}\Reactions.db"
  File "${S}\Reactions.elb"
  File "${S}\KD1280.db"
  File "${S}\KD1280.elb"
  File "${S}\References.txt"
  File "${S}\LICENSE"
  File "${S}\README.txt"
  File "${SW}\Database.exe"
  File "${S}\Database.jar"
  File "/oname=.Database.ini" "ini\.Database_KTH.ini" 
  File "${SW}\DataMaintenance.exe"
  File "${S}\DataMaintenance.jar"
  File "${SW}\AddShowReferences.exe"
  File "${S}\AddShowReferences.jar"
  File "images\Icon_dat.ico"
  File "images\Icon_plt.ico"
  File "${SW}\Spana.cmd"
  File "${SW}\Spana.lnk"
  File "${SW}\Spana.exe"
  File "${S}\Spana.jar"
  File "/oname=.Spana.ini" "ini\.Spana_KTH.ini" 
  File "${SW}\Predom.exe"
  File "${S}\Predom.jar"
  File "${SW}\SED.exe"
  File "${S}\SED.jar"
  File "${SW}\ShellChangeNotify.exe"
  File "${S}\SIT-coefficients.dta"
  File "${S}\PlotPDF.jar"
  File "${S}\PlotPS.jar"
  File "${SW}\README_Windows.txt"
  DetailPrint "--- Icons:"
  StrCpy  $R0  "$OUTDIR"
  SetOutPath "$R0\icons"
  File "${SW}\icons\*.*"
  SetOutPath "$R0"  
  DetailPrint "--- Additional databases:"
  StrCpy  $R0  "$OUTDIR"
  SetOutPath "$R0\other_databases"
  File "${S}\other_databases\*.*"
  SetOutPath "$R0"
  DetailPrint '--- Writing default database in'
  DetailPrint '       "$OUTDIR\.Database.ini"'
  StrCpy $3 "$OUTDIR\KD1280.db"
  ${WordReplace} "$3" "\" "\\" '+' $3
  ${WordReplace} "$3" ":" "\:" '+' $3
  ${WordReplace} "$3" "!" "\!" '+' $3
  ${WordReplace} "$3" "#" "\#" '+' $3
  ${WordReplace} "$3" "=" "\=" '+' $3
  WriteINIStr "$OUTDIR\.Database.ini" "Database" "DataBase[1]" "$3"
  FlushINI "$OUTDIR\.Database.ini"
  DetailPrint "------ Installing library jar-files:"
  StrCpy  $R0  "$OUTDIR"
  SetOutPath "$R0\lib"
  File "${S}\lib\*.*"
  SetOutPath "$R0"
  DetailPrint "------ Installing Example files:"
  StrCpy  $R0  "$OUTDIR"
  SetOutPath "$R0\Examples"
  File "${S}\Examples\*.*"
  SetOutPath "$R0"

    ; -------------  write file associations in Registry
    StrCmp "$Inst_HDD" "0" skip_file_associatgion  ;if no HDD no File Associations
    ${If} $reg_DAT = 1
        DetailPrint "------ Associating Spana with DAT-files"
        ReadRegStr $R9 HKCU "Software\Classes\.dat" ""
        ${IfNot} $R9 == "Eq-Diagr_DAT_File"
        ${IfNot} $R9 == ""
        WriteRegStr HKCU "Software\Classes\.dat\Backup_by_Eq-Diagr" "" "$R9"
        ${EndIf}
        ${EndIf}
        WriteRegStr HKCU "Software\Classes\.dat" "" "Eq-Diagr_DAT_File"
        WriteRegStr HKCU "Software\Classes\Eq-Diagr_DAT_File" "" "Eq-Diagr_DAT_File"
        WriteRegStr HKCU "Software\Classes\Eq-Diagr_DAT_File\DefaultIcon" "" "$INSTDIR\Icon_dat.ico"
        WriteRegStr HKCU "Software\Classes\Eq-Diagr_DAT_File\shell" "" "open"
        WriteRegStr HKCU "Software\Classes\Eq-Diagr_DAT_File\shell\open\command" "" '$INSTDIR\Spana.exe "%1"'
        System::Call 'Shell32::SHChangeNotify(i 0x8000000, i 0, i 0, i 0)'
    ${EndIf}

    ${If} $reg_PLT = 1
        DetailPrint "------ Associating Spana with PLT-files"
        ReadRegStr $R9 HKCU "Software\Classes\.plt" ""
        ${IfNot} $R9 == "Eq-Diagr_PLT_File"
        ${IfNot} $R9 == ""
        WriteRegStr HKCU "Software\Classes\.plt\Backup_by_Eq-Diagr" "" "$R9"
        ${EndIf}
        ${EndIf}
        WriteRegStr HKCU "Software\Classes\.plt" "" "Eq-Diagr_PLT_File"
        WriteRegStr HKCU "Software\Classes\Eq-Diagr_PLT_File" "" "Eq-Diagr_PLT_File"
        WriteRegStr HKCU "Software\Classes\Eq-Diagr_PLT_File\DefaultIcon" "" "$INSTDIR\Icon_plt.ico"
        WriteRegStr HKCU "Software\Classes\Eq-Diagr_PLT_File\shell" "" "open"
        WriteRegStr HKCU "Software\Classes\Eq-Diagr_PLT_File\shell\open\command" "" '$INSTDIR\Spana.exe "%1"'
        System::Call 'Shell32::SHChangeNotify(i 0x8000000, i 0, i 0, i 0)'
    ${EndIf}
    skip_file_associatgion:

    ; -------------  Start menu ShortCuts
    StrCmp "$Inst_HDD" "0" skip_start_menu_shortcuts   ;if no HDD then no ShortCuts
    StrCmp $Install_Start_Menu_ShortCuts "0" skip_start_menu_shortcuts
    DetailPrint "------ Creating Start Menu shortcuts"
    ;StrCpy $R1 "$Start_Menu_Folder" 1
    ;StrCmp $R1 ">" skip
        CreateDirectory "$SMPROGRAMS\$Start_Menu_Folder"
        CreateShortCut "$SMPROGRAMS\$Start_Menu_Folder\Chemical Diagrams Help.lnk" "$INSTDIR\Chem_Diagr_Help.exe" "" "$INSTDIR\Chem_Diagr_Help.exe" 0
        CreateShortCut "$SMPROGRAMS\$Start_Menu_Folder\Database.lnk" "$INSTDIR\Database.exe" "" "$INSTDIR\Database.exe" 0
        CreateShortCut "$SMPROGRAMS\$Start_Menu_Folder\Spana.lnk" "$INSTDIR\Spana.exe" "" "$INSTDIR\Spana.exe" 0
        CreateShortCut "$SMPROGRAMS\$Start_Menu_Folder\Uninstall.lnk" "$INSTDIR\Uninstall.exe" "" "$INSTDIR\Uninstall.exe" 0
    skip_start_menu_shortcuts:

; -------------  write Uninstall information in the Registry
    StrCmp "$Inst_HDD" "0" skip_uninstaller   ;if no HDD then no UnInstaller
    DetailPrint "------ Writing the installation path into the registry"
    ;Write the installation path into the registry
    WriteRegStr HKCU "Software\Eq-Diagr" "Install_Dir" "$INSTDIR"
    DetailPrint "------ Writing the uninstall keys for Windows in the registry"
    ;Write the uninstall keys for Windows
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Eq-Diagr" "DisplayName" "Chemical Equilibrium Diagrams (Java)"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Eq-Diagr" "UninstallString" '"$INSTDIR\uninstall.exe"'
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Eq-Diagr" "DisplayIcon" "$INSTDIR\Spana.exe,0"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Eq-Diagr" "DisplayVersion" "${__DATE__}"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Eq-Diagr" "HelpLink" "https://sites.google.com/site/chemdiagr/"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Eq-Diagr" "InstallLocation" '"$INSTDIR"'
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Eq-Diagr" "URLInfoAbout" "https://sites.google.com/site/chemdiagr/"
    WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Eq-Diagr" "NoModify" 1
    WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Eq-Diagr" "NoRepair" 1

; -------------  write the uninstaller
    DetailPrint "------ Making the Uninstaller"
    WriteUninstaller "uninstall.exe"
    skip_uninstaller:

; -------------  for a portable installation, perhaps at \PortableApps
    StrCmp  "$Inst_HDD" "1" end   ;if HDD then end
    ;get current drive letter
    ${GetRoot} "$EXEDIR" $3
    DetailPrint "------ Installing the Portable Launchers"
	;StrCpy user_var(destination) str [maxlen] [start_offset]
	StrCpy $R5 "$INSTDIR" 12 3	; Extract 12 characters from $INSTDIR
								; leaving out 3 first characters (for example C:\)
	StrCmp "$R5" "PortableApps" +1 noPortableApps ;if $INSTDIR starts with "N:\PortableApps\..."
		; $INSTDIR is equal to "N:\PortableApps\Eq-Diagr_Portable"
		SetOutPath "$INSTDIR"
		File /r /x Thumbs.db "${SPA}\*.*"
		File "${SPAE}\*.exe"
		DetailPrint 'Writing current drive into'
		DetailPrint '    "$INSTDIR\Data\settings\PortableSettings.ini"'
		WriteINIStr "$INSTDIR\Data\settings\DatabasePortableSettings.ini" "DatabasePortableSettings" "LastDrive" "$3"
		FlushINI "$INSTDIR\Data\settings\DatabasePortableSettings.ini"
		WriteINIStr "$INSTDIR\Data\settings\SpanaPortableSettings.ini" "SpanaPortableSettings" "LastDrive" "$3"
		FlushINI "$INSTDIR\Data\settings\SpanaPortableSettings.ini"
		StrCpy  $0  "$INSTDIR\App\Eq-Diagr"
		StrCpy  $R0  "$0"
		Goto PortableAppsCont
    noPortableApps:
        SetOutPath "$INSTDIR"
        File "/oname=SpanaPortable_help.html" "${SPA}\help.html"
        File "${SPAE}\*.exe"
        StrCpy  $R0  "$OUTDIR"
        SetOutPath "$R0\Other\Source"
        File /r /x Thumbs.db "${SPA}\Other\Source\*.*"
        SetOutPath "$R0\Other\Help"
        File /r /x Thumbs.db "${SPA}\Other\Help\*.*"
        DetailPrint 'Writing current drive into'
        DetailPrint '    "$INSTDIR\*PortableSettings.ini"'
        WriteINIStr "$INSTDIR\DatabasePortableSettings.ini" "DatabasePortableSettings" "LastDrive" "$3"
        FlushINI "$INSTDIR\DatabasePortableSettings.ini"
        WriteINIStr "$INSTDIR\SpanaPortableSettings.ini" "SpanaPortableSettings" "LastDrive" "$3"
        FlushINI "$INSTDIR\SpanaPortableSettings.ini"
        StrCpy  $0  "$INSTDIR"
    PortableAppsCont:
        DetailPrint 'Writing "$INSTDIR\DatabasePortable.exe"'
        DetailPrint '     in "$0\.Spana.ini"'
        WriteINIStr "$0\.Spana.ini" "Spana" "createDataFileProg" "$INSTDIR\DatabasePortable.exe"
        FlushINI "$0\.Spana.ini"
        DetailPrint 'Writing "$INSTDIR\SpanaPortable.exe"'
        DetailPrint '     in "$0\.Database.ini"'
        WriteINIStr "$0\.Database.ini" "Database" "diagramProgram" "$INSTDIR\SpanaPortable.exe"
        FlushINI "$0\.Database.ini"
    DetailPrint "------ Copying cfg-files:"  ;this will set SaveIniFileToApplicationPathOnly=true
        SetOutPath "$R0"
        File "cfg\*.cfg"
    DetailPrint "------"
    end:
SectionEnd

; -----------------------------------------------------------------------------
; * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
; -----------------------------------------------------------------------------
;     Uninstaller
Section "Uninstall"

  ; -------------  Remove files and uninstaller
  DetailPrint "------ Removing files:"
  Delete "$INSTDIR\Examples\*.*"
  RMDir "$INSTDIR\Examples"
  Delete "$INSTDIR\lib\*.*"
  RMDir "$INSTDIR\lib"
  Delete "$INSTDIR\icons\*.*"
  RMDir "$INSTDIR\icons"
  Delete "$INSTDIR\other_databases\!Readme.txt"
  Delete "$INSTDIR\other_databases\Medusa-Hydra.db"
  Delete "$INSTDIR\other_databases\Medusa-Hydra.elb"
  Delete "$INSTDIR\other_databases\Medusa-Hydra_References.txt"
  Delete "$INSTDIR\other_databases\MintEQ-v4.db"
  Delete "$INSTDIR\other_databases\MintEQ-v4.elb"
  Delete "$INSTDIR\other_databases\MintEQ-v4_References.txt"
  Delete "$INSTDIR\other_databases\Wateq4F.db"
  Delete "$INSTDIR\other_databases\Wateq4F.elb"
  Delete "$INSTDIR\other_databases\Wateq4F_References.txt"
  Delete "$INSTDIR\other_databases\Soltherm.txt"
  Delete "$INSTDIR\other_databases\Soltherm.elt"
  Delete "$INSTDIR\other_databases\Soltherm_References.txt"
  RMDir "$INSTDIR\other_databases"
  Delete "$INSTDIR\Chem_Diagr_Help.exe"
  Delete "$INSTDIR\Chem_Diagr_Help.ini"
  Delete "$INSTDIR\.ChemDiagrHelp.ini"
  Delete "$INSTDIR\Chem_Diagr_Help.cfg"
  Delete "$INSTDIR\Chem_Diagr_Help.jar"
  Delete "$INSTDIR\Reactions.db"
  Delete "$INSTDIR\Reactions.elb"
  Delete "$INSTDIR\KD1280.db"
  Delete "$INSTDIR\KD1280.elb"
  Delete "$INSTDIR\References.txt"
  Delete "$INSTDIR\LICENSE"
  Delete "$INSTDIR\README.txt"
  Delete "$INSTDIR\README_Windows.txt"
  Delete "$INSTDIR\Database.exe"
  Delete "$INSTDIR\Database.jar"
  Delete "$INSTDIR\Database.cfg"
  Delete "$INSTDIR\Database.ini"
  Delete "$INSTDIR\.Database.ini"
  Delete "$INSTDIR\DataMaintenance.exe"
  Delete "$INSTDIR\DataMaintenance.jar"
  Delete "$INSTDIR\DataMaintenance.cfg"
  Delete "$INSTDIR\DataMaintenance.ini"
  Delete "$INSTDIR\.DataMaintenance.ini"
  Delete "$INSTDIR\AddShowReferences.exe"
  Delete "$INSTDIR\AddShowReferences.jar"
  Delete "$INSTDIR\Icon_dat.ico"
  Delete "$INSTDIR\Icon_plt.ico"
  Delete "$INSTDIR\Spana.cfg"
  Delete "$INSTDIR\Spana.cmd"
  Delete "$INSTDIR\Spana.lnk"
  Delete "$INSTDIR\Spana.exe"
  Delete "$INSTDIR\Spana.jar"
  Delete "$INSTDIR\Spana.ini"
  Delete "$INSTDIR\.Spana.ini"
  Delete "$INSTDIR\Predom.exe"
  Delete "$INSTDIR\Predom.jar"
  Delete "$INSTDIR\SED.exe"
  Delete "$INSTDIR\SED.jar"
  Delete "$INSTDIR\ShellChangeNotify.exe"
  Delete "$INSTDIR\SIT-coefficients.dta"
  Delete "$INSTDIR\PlotPDF.jar"
  Delete "$INSTDIR\PlotPS.jar"
  Delete "$INSTDIR\Test.plt"

    ; -------------  Start of restore registry scripts
    ; -------------  Read the installation path from the registry
    StrCpy $Installed_Folder ""
    ReadRegStr $Installed_Folder HKCU "SOFTWARE\Eq-Diagr" "Install_Dir"
    ; -------------  Remove un-install registry keys
    DetailPrint "------ Removing registry entries"
    DetailPrint "Removing un-install registry keys"
    DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Eq-Diagr"
    DeleteRegKey HKCU "Software\Eq-Diagr"

    ; ------------- remove file association DAT
    !define Index "Line${__LINE__}"
    ReadRegStr $1 HKCU "Software\Classes\.dat" ""
    StrCmp $1 "Eq-Diagr_DAT_File" 0 "${Index}-NoOwn" ;only do this if we own it
        DetailPrint "Removing file association for DAT-files"
        ;If Eq-Diagr created a backup, restore it.
        ReadRegStr $1 HKCU "Software\Classes\.dat\Backup_by_Eq-Diagr" ""
        ${If} $1 == ""
            DeleteRegValue HKCU "Software\Classes\.dat" ""
        ${Else}
            WriteRegStr  HKCU "Software\Classes\.dat"  ""  "$1"
            DeleteRegKey HKCU "Software\Classes\.dat\Backup_by_Eq-Diagr"
        ${EndIf}
        ReadRegStr $1 HKCU "Software\Classes\.dat" ""
        StrCmp $1 "" 0 +2
            DeleteRegKey /ifempty HKCU "Software\Classes\.dat"
    "${Index}-NoOwn:"
    DeleteRegKey HKCU "Software\Classes\Eq-Diagr_DAT_File" ;Delete key with association settings
    !undef Index
    ; ------------- remove file association PLT
    !define Index "Line${__LINE__}"
    ReadRegStr $1 HKCU "Software\Classes\.plt" ""
    StrCmp $1 "Eq-Diagr_PLT_File" 0 "${Index}-NoOwn" ;only do this if we own it
        DetailPrint "Removing file association for PLT-files"
        ;If Eq-Diagr created a backup, restore it.
        ReadRegStr $1 HKCU "Software\Classes\.plt\Backup_by_Eq-Diagr" ""
        ${If} $1 == ""
            DeleteRegValue HKCU "Software\Classes\.plt" ""
        ${Else}
            WriteRegStr  HKCU "Software\Classes\.plt"  ""  "$1"
            DeleteRegKey HKCU "Software\Classes\.plt\Backup_by_Eq-Diagr"
        ${EndIf}
        ReadRegStr $1 HKCU "Software\Classes\.plt" ""
        StrCmp $1 "" 0 +2
            DeleteRegKey /ifempty HKCU "Software\Classes\.plt"
    "${Index}-NoOwn:"
    DeleteRegKey HKCU "Software\Classes\Eq-Diagr_PLT_File" ;Delete key with association settings
    !undef Index
    ; ------------- remove "garbage" from registry
    DetailPrint "Removing diverse registry keys"
    ;Remove extra File Explorer keys written by Windows itself
    ;remove "OpenWithList"
        StrCpy $9 "Spana.exe"
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.bmp\OpenWithList"
        Call un.DeleteHKCURegistryValue_Data
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.dat\OpenWithList"
        Call un.DeleteHKCURegistryValue_Data
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.plt\OpenWithList"
        Call un.DeleteHKCURegistryValue_Data
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.wmf\OpenWithList"
        Call un.DeleteHKCURegistryValue_Data
        StrCpy $9 "Database.exe"
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.dat\OpenWithList"
        Call un.DeleteHKCURegistryValue_Data
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.db\OpenWithList"
        Call un.DeleteHKCURegistryValue_Data
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.skv\OpenWithList"
        Call un.DeleteHKCURegistryValue_Data
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.txt\OpenWithList"
        Call un.DeleteHKCURegistryValue_Data
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.dat\OpenWithList"
        StrCpy $9 "Predom.exe"
        Call un.DeleteHKCURegistryValue_Data
        StrCpy $9 "SED.exe"
        Call un.DeleteHKCURegistryValue_Data
    ;remove "OpenWithProgids"
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.plt\OpenWithProgids"
        StrCpy $9 "Eq-Diagr_PLT_File"
        Call un.DeleteHKCURegistryValue
        DeleteRegKey /ifempty HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.plt\OpenWithProgids"
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.dat\OpenWithProgids"
        StrCpy $9 "Eq-Diagr_DAT_File"
        Call un.DeleteHKCURegistryValue
        DeleteRegKey /ifempty HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.dat\OpenWithProgids"

    StrCmp "$Installed_Folder" ""  "No_Installed_Folder"  0
        StrCpy $9 "$Installed_Folder\KD1280.DB"
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\ComDlg32\OpenSaveMRU\DB"
        Call un.DeleteHKCURegistryValue_Data

        DeleteRegValue HKCU "Software\Microsoft\Windows\ShellNoRoam\MUICache" "$Installed_Folder\uninstall.exe"
        DeleteRegValue HKCU "Software\Microsoft\Windows\ShellNoRoam\MUICache" "$Installed_Folder\Spana.exe"
        DeleteRegValue HKCU "Software\Microsoft\Windows\ShellNoRoam\MUICache" "$Installed_Folder\ShellChangeNotify.exe"
        DeleteRegValue HKCU "Software\Microsoft\Windows\ShellNoRoam\MUICache" "$Installed_Folder\Chem_Diagr_Help.exe"
        DeleteRegValue HKCU "Software\Microsoft\Windows\ShellNoRoam\MUICache" "$Installed_Folder\Database.exe"
        DeleteRegValue HKCU "Software\Microsoft\Windows\ShellNoRoam\MUICache" "$Installed_Folder\PREDOM.exe"
        DeleteRegValue HKCU "Software\Microsoft\Windows\ShellNoRoam\MUICache" "$Installed_Folder\SED.exe"
        DeleteRegKey  HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\App Management\ARPCache\Eq-Diagr"
    "No_Installed_Folder:"

    System::Call 'Shell32::SHChangeNotify(i 0x8000000, i 0, i 0, i 0)'

  ; -------------  Remove shortcuts, if any
  DetailPrint "------ Removing shortcuts:"
  DetailPrint "Searching Desktop and 'Send To' shortcuts"
  Delete "$DESKTOP\Spana.lnk"
  Delete "$SENDTO\Spana.lnk"

    SetShellVarContext All
    StrCpy $Message "all users"
    DetailPrint "Searching Spana-shortcuts in the Start Menu folders for $Message"
    ClearErrors
    StrCpy $R1 0 ;keep track of how many shortcuts are found
    StrCpy $R2 0 ;will be "StopLocate" if the user selects the Cancel button
    loop_all:
    StrCpy $R3 "" ;R3 will contain the name of the folder to Delete
    ${un.Locate} "$SMPROGRAMS" "/L=F /M=Spana.lnk" "un.ListShortcuts"
        StrCmp $R2 StopLocate end_loop_all ;the user selected Cancel
        StrCmp $R3 "" end_loop_all ;is there a folder to delete?
            RMDir "$R3"
            ClearErrors
        Goto loop_all
    end_loop_all:
        IfErrors 0 +3
            MessageBox MB_OK|MB_ICONEXCLAMATION|MB_TOPMOST "Error using file function 'Locate'"
            StrCpy $R1 0
        ${If} $R1 = 0
            DetailPrint "Found no Spana-shortcuts in Start Menu folders for $Message"
        ${Else}
            DetailPrint "Found Spana-shortcuts in $R1 Start Menu folders for $Message"
        ${EndIf}

    SetShellVarContext current
    StrCpy $Message "the current user"
    DetailPrint "Searching Spana-shortcuts in the Start Menu folders for $Message"
    ClearErrors
    StrCpy $R1 0 ;keep track of how many shortcuts are found
    StrCpy $R2 0 ;will be "StopLocate" if the user selects the Cancel button
    loop_current:
    StrCpy $R3 "" ;R3 will contain the name of the folder to Delete
    ${un.Locate} "$SMPROGRAMS" "/L=F /M=Spana.lnk" "un.ListShortcuts"
        ;Locate:       option "/L=F" locate files only
        ; $R9    "path\name"
        ; $R8    "path"
        ; $R7    "name"
        ; $R6    "size"  ($R6="" if directory, $R6="0" if file with /S=)
        ; $R0-$R5  are not used (save data in them).
        StrCmp $R2 StopLocate end_loop_current ;the user selected Cancel
        StrCmp $R3 "" end_loop_current ;is there a folder to delete?
            RMDir "$R3"
            ClearErrors
        Goto loop_current
    end_loop_current:
        IfErrors 0 +3
            MessageBox MB_OK|MB_ICONEXCLAMATION|MB_TOPMOST "Error using file function 'Locate'"
            StrCpy $R1 0
        ${If} $R1 = 0
            DetailPrint "Found no Spana-shortcuts in Start Menu folders for $Message"
        ${Else}
            DetailPrint "Found Spana-shortcuts in $R1 Start Menu folders for $Message"
        ${EndIf}

; -------------  Remove INI-files
  DetailPrint "------ Deleting INI-files"
  ClearErrors
  ExpandEnvStrings $R2 "%APPDATA%"
  IfErrors "No-APPDATA"
    ;;MessageBox MB_OK|MB_TOPMOST "%APPDATA%=$R2"
    ${If} "$R2" != "%APPDATA%"
      DetailPrint "Checking %APPDATA%"
      DetailPrint "         ($R2)"
      StrCpy $R1 $R2 1 -1   ;add "\" at the end if needed
      ${If} $R1 == "\"
          StrCpy $R1 $R2 -1
        ${Else}
          StrCpy $R1 $R2
        ${EndIf}
      Delete "$R1\Spana.ini"
      Delete "$R1\Database.ini"
      ${EndIf}
No-APPDATA:
  ClearErrors
  ExpandEnvStrings $R2 "%HOMEDRIVE%"
  IfErrors "No-HomeDrive"
    ;;MessageBox MB_OK|MB_TOPMOST "%HOMEDRIVE%=$R2"
    ${If} "$R2" != "%HOMEDRIVE%"
      DetailPrint "Checking %HOMEDRIVE% ($R2)"
      StrCpy $R1 $R2 1 -1   ;add "\" at the end if needed
      ${If} $R1 == "\"
          StrCpy $R1 $R2 -1
        ${Else}
          StrCpy $R1 $R2
        ${EndIf}
      Delete "$R1\Spana.ini"
      Delete "$R1\Database.ini"
      Delete "$R1\DataMaintenance.ini"
      Delete "$R1\Chem_Diagr_Help.ini"
      Delete "$R1\.config\eq-diagr\.Spana.ini"
      Delete "$R1\.config\eq-diagr\.Database.ini"
      Delete "$R1\.config\eq-diagr\.DataMaintenance.ini"
      Delete "$R1\.config\eq-diagr\.ChemDiagrHelp.ini"
      RMDir  "$R1\.config\eq-diagr"
      RMDir  "$R1\.config"
      ${EndIf}
No-HomeDrive:
  ClearErrors
  ExpandEnvStrings $R5 "%HOMEPATH%"
  IfErrors "No-HOMEPATH"
    ;;MessageBox MB_OK|MB_TOPMOST "%HOMEPATH%=$R5"
    ${If} "$R5" != "%HOMEPATH%"
        ${If} "$R2" != "%HOMEDRIVE%"
            DetailPrint "Checking %HOMEDRIVE%%HOMEPATH%"
            StrCpy $R4 $R5 1 -1   ;add "\" at the end if needed
            ${If} $R4 == "\"
                    StrCpy $R4 $R5 -1
                ${Else}
                    StrCpy $R4 $R5
                ${EndIf}
            StrCpy $R3 $R4 1   ;add "\" at the start if needed
            ${If} $R3 == "\"
                    StrCpy $R3 $R4
                ${Else}
                    StrCpy $R3 "\$R4"
                ${EndIf}
            DetailPrint "         ($R1$R3)"
            Delete "$R1$R3\Spana.ini"
            Delete "$R1$R3\Database.ini"
            Delete "$R1$R3\DataMaintenance.ini"
            Delete "$R1$R3\Chem_Diagr_Help.ini"
            Delete "$R1$R3\.config\eq-diagr\.Spana.ini"
            Delete "$R1$R3\.config\eq-diagr\.Database.ini"
            Delete "$R1$R3\.config\eq-diagr\.DataMaintenance.ini"
            Delete "$R1$R3\.config\eq-diagr\.ChemDiagrHelp.ini"
            RMDir  "$R1$R3\.config\eq-diagr"
            RMDir  "$R1$R3\.config"
        ${EndIf}  ;"$R2" != "%HOMEDRIVE%"
      ${EndIf}    ;"$R4" != "%HOMEPATH%"
No-HOMEPATH:
  DetailPrint "Checking $WINDIR\Application data"
    Delete "$WINDIR\Application data\Spana.ini"
    Delete "$WINDIR\Application data\Database.ini"
    Delete "$WINDIR\Application data\DataMaintenance.ini"
    Delete "$WINDIR\Application data\Chem_Diagr_Help.ini"

  DetailPrint "Checking $WINDIR"
    Delete "$WINDIR\Spana.ini"
    Delete "$WINDIR\Database.ini"
    Delete "$WINDIR\DataMaintenance.ini"
    Delete "$WINDIR\Chem_Diagr_Help.ini"

  DetailPrint "Checking C:\"
    Delete "C:\Spana.ini"
    Delete "C:\Database.ini"
    Delete "C:\DataMaintenance.ini"
    Delete "C:\Chem_Diagr_Help.ini"
    Delete "C:\.Spana.ini"
    Delete "C:\.Database.ini"
    Delete "C:\.DataMaintenance.ini"
    Delete "C:\.ChemDiagrHelp.ini"
    Delete "C:\.config\eq-diagr\.Spana.ini"
    Delete "C:\.config\eq-diagr\.Database.ini"
    Delete "C:\.config\eq-diagr\.DataMaintenance.ini"
    Delete "C:\.config\eq-diagr\.ChemDiagrHelp.ini"
    RMDir  "C:\.config\eq-diagr"
    RMDir  "C:\.config"

; -------------  Remove the Uninstaller
  DetailPrint "------ Deleting the Uninstaller"
  Delete "$INSTDIR\uninstall.exe"
; -------------  Remove the directory
  DetailPrint "------ Removing the installation directory"
  RMDir "$INSTDIR"
SectionEnd  ;"Uninstall"

Function un.DeleteHKCURegistryValue
;Open a registry key ($8) an loop through all values
;and delete them if they fit a given string ($9)
;     ;Input:     $9 = value name to delete;    $8 = subkey to search into
Push $0
Push $1
        ClearErrors
        ;MessageBox MB_OK|MB_SETFOREGROUND|MB_TOPMOST `"$$8" = "$8"$\n"$$9" = "$9"`
        !define Index "Line${__LINE__}"
            StrCpy $0 0      ;$0: index for "EnumRegValue"
            "loop-${Index}:"
            ClearErrors
            EnumRegValue $1 HKCU "$8" $0
            StrCmp  "$1"  ""  "done-${Index}"
            IntOp $0 $0 + 1    ;increase the counter for "EnumRegValue"
            ;MessageBox MB_YESNO|MB_ICONQUESTION|MB_TOPMOST `EnumRegValue "$8"$\nValue "$1"$\n$\nMore?` IDNO "done-${Index}"
            StrCmp  "$1"  "$9"  0  "loop-${Index}"
                    ;MessageBox MB_YESNO|MB_ICONQUESTION|MB_TOPMOST `EnumRegValue "$8"$\nValue "$1"$\n$\nDelete value?` IDNO "loop-${Index}"
                    DeleteRegValue  HKCU  "$8"  "$1"
            ;Goto "loop-${Index}"         ;only one value may have this name
            "done-${Index}:"
        !undef Index
ClearErrors
Pop $1
Pop $0
FunctionEnd  ;DeleteHKCURegistryValue

Function un.DeleteHKCURegistryValue_Data
;Open a registry key ($8) an loop through all values,  read the data
;in these values and delete them if they contain a given string ($9)
;     ;Input:     $9 = data to search in values;    $8 = subkey to search into.
Push $0
Push $1
Push $2
Push $3
        ClearErrors
        !define Index "Line${__LINE__}"
            StrCpy $0 0      ;$0: index for "EnumRegValue"
            "loop-${Index}:"
            ClearErrors
            EnumRegValue $1 HKCU "$8" $0
            StrCmp  $1  ""  "done-${Index}"
            IntOp $0 $0 + 1    ;increase the counter for "EnumRegValue"
            ReadRegStr $2 HKCU "$8" "$1"
            ;MessageBox MB_YESNO|MB_ICONQUESTION|MB_TOPMOST `ReadRegStr "$8"$\nValue "$1" = "$2"$\n$\nMore?` IDNO "done-${Index}"
            ${un.WordFind}  "$2"  "$9"  "E+1{"  $3    ;contains the text?
            IfErrors  "loop-${Index}"  0
                    ;MessageBox MB_YESNO|MB_ICONQUESTION|MB_TOPMOST `ReadRegStr "$8"$\nValue "$1" = "$2"$\n$\nDelete value?` IDNO "loop-${Index}"
                    DeleteRegValue  HKCU  "$8"  "$1"
            Goto "loop-${Index}"    ;continue looking, there might be more.
            "done-${Index}:"
        !undef Index
ClearErrors
Pop $3
Pop $2
Pop $1
Pop $0
FunctionEnd  ;un.DeleteHKCURegistryValue_Data

Function un.ListShortcuts
;function called by "Locate". Returns:
        ; $R9    "path\name"
        ; $R8    "path"
        ; $R7    "name"
        ; $R6    "size"  ($R6="" if directory, $R6="0" if file with /S=)
        ; $R0-$R5  are not used (save data in them).
    ;MessageBox MB_OK|MB_TOPMOST "$$R9=$R9$\n$$R8=$R8$\n$$R7=$R7"
    IntOp $R1 $R1 + 1 ;keep track of how many shortcuts are found
    ;get the name of the Start Menu folder
      StrLen $R2 $SMPROGRAMS    ;get the length of the user's Start Menu folder name
      IntOp $R2 $R2 + 1         ;add 1 (for the back-slash)
      StrCpy $R2 $R8 "" $R2     ;get the last part of the Start Menu's folder
    StrCpy $R0 0 ;will be StopLocate to stop the Locate function
    StrCpy $R3 "" ;R3 will contain the name of the folder to Delete
    ;MessageBox MB_YESNOCANCEL|MB_ICONQUESTION|MB_TOPMOST "Remove shortcuts from the Start Menu folder $\"$R2$\" for $Message?$\n    YES = delete any Spana and Database shortcuts in this folder$\n    NO = skip this Start Menu folder$\n    CANCEL = stop searching for Start Menu shortcuts" IDNO end IDCANCEL cancel
        Delete "$R8\Chemical Diagrams Help.lnk"
        Delete "$R8\Spana.lnk"
        Delete "$R8\Database.lnk"
        Delete "$R8\Uninstall.lnk"
        StrCpy $R3 $R8 ;save the name of the Start Menu folder to Delete
        StrCpy $R0 StopLocate ;stop the search in order to delete the folder
        Goto end
;    cancel:
;        StrCpy $R2 StopLocate
;        StrCpy $R0 StopLocate
    end:
        Push $R0
FunctionEnd       ;un.ListShortcuts
