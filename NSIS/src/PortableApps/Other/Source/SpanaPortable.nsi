; ************************************************************************
; Spana_Portable.nsi
; ************************************************************************
; * Define constants
; ************************************************************************
!define PortableAppName "Spana Portable" ;for captions and version info.
!define AppName "Spana"
!define NAME "SpanaPortable"       ;name of launcher
!define DEFAULTEXE "Spana.exe"
!define DEFAULTAPPDIR "Eq-Diagr"
!define DEFAULTSETTINGSPATH "settings"
!define PORTABLESETTINGS "SpanaPortableSettings.ini"
!define REGBAKFILE "SpanaPortable.reg"

!define AUTHOR "I.Puigdomenech"
!define ICON "SpanaPortable.ico"

; ************************************************************************
; *  Set basic information
; ************************************************************************
Name "${PortableAppName}"
Icon "${ICON}"
Caption "${PortableAppName} - ${__DATE__}"
OutFile "${NAME}.exe"

;*** Language
LoadLanguageFile "${NSISDIR}\Contrib\Language files\English.nlf"

; ************************************************************************
; *  Set version information
; ************************************************************************
!define /date DATE "%Y.%m.%d.01"
VIProductVersion "${DATE}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName" "${PortableAppName}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "Comments" `Allow "${AppName}" to be run from a removeable drive. Visit also: PortableApps.com`
VIAddVersionKey /LANG=${LANG_ENGLISH} "CompanyName" "by ${AUTHOR}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright" "© ${AUTHOR}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "${PortableAppName}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion" "${__DATE__}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "OriginalFilename" "${NAME}.exe"

; ************************************************************************
; *  Runtime Switches
; ************************************************************************
CRCCheck off
WindowIcon off    ;show no icon of the launcher
SilentInstall silent ;start as launcher, not as installer
AutoCloseWindow true ;automatically close when finished
RequestExecutionLevel "user"

; Best Compression
SetCompress auto
SetCompressor /SOLID lzma
SetCompressorDictSize 32
SetDatablockOptimize on

; ************************************************************************
; *  Includes
; ************************************************************************
!include "FileFunc.nsh" ; add header for file manipulation
!insertmacro "GetParameters" ;function retrieving command line parameters
!insertmacro "GetRoot"
!include "TextFunc.nsh" ; add header for file manipulation
!insertmacro "LineFind"
!include "WordFunc.nsh"
!insertmacro "WordReplace"
!insertmacro "WordFind"
!include "Registry.nsh"

; ************************************************************************
; *  Define variables
; ************************************************************************
Var PROGRAMDIRECTORY
Var PROGRAMEXECUTABLE
Var ADDITIONALPARAMETERS
Var EXECSTRING
Var INIPATH       ; ini-file for the launcher
Var SETTINGSDIRECTORY ;ini-file(s) with the last path, etc
Var LASTDRIVE
Var CURRENTDRIVE
Var SECONDLAUNCH
Var DISABLESPLASHSCREEN
Var SHOWCOMMANDLINE

; ************************************************************************
; *  Main section
; ************************************************************************
Section "Main"

    ;CheckForINI:
    ;=== Find the INI file, if there is one
    StrCpy "$INIPATH" "$EXEDIR"
        IfFileExists "$EXEDIR\${NAME}.ini" "" CheckDataINI
                StrCpy "$INIPATH" "$EXEDIR"
                Goto ReadINI
        CheckDataINI:
        IfFileExists "$EXEDIR\Data\${NAME}.ini" "" CheckDataSettingsINI
                StrCpy "$INIPATH" "$EXEDIR\Data"
                Goto ReadINI
        CheckDataSettingsINI:
        IfFileExists "$EXEDIR\Data\settings\${NAME}.ini" "" CheckOtherINI
                StrCpy "$INIPATH" "$EXEDIR\Data\settings"
                Goto ReadINI
        CheckOtherINI:
        IfFileExists "$EXEDIR\Other\${NAME}.ini" "" NoINI
                StrCpy "$INIPATH" "$EXEDIR\Other"
                ;Goto ReadINI
    ReadINI:
        ;=== Read the parameters from the INI file
        ReadINIStr $0 "$INIPATH\${NAME}.ini" "${NAME}" "${APPNAME}Executable"
        StrCmp "'$0'" "''" +3 0
            StrCpy "$PROGRAMEXECUTABLE" "$0"
            Goto +2
            StrCpy "$PROGRAMEXECUTABLE" "${DEFAULTEXE}"
        ReadINIStr $0 "$INIPATH\${NAME}.ini" "${NAME}" "${APPNAME}Directory"
        StrCmp "'$0'" "''"  +3  0
            StrCpy "$PROGRAMDIRECTORY" "$EXEDIR\$0"
            Goto +5
            IfFileExists "$EXEDIR\$PROGRAMEXECUTABLE" 0 +3
                StrCpy "$PROGRAMDIRECTORY" "$EXEDIR"  ;everything in the same folder
                Goto +2
                StrCpy "$PROGRAMDIRECTORY" "$EXEDIR\App\${DEFAULTAPPDIR}"  ;standard configuration
        ReadINIStr $0 "$INIPATH\${NAME}.ini" "${NAME}" "SettingsDirectory"
        StrCmp "'$0'" "''"  +3  0
            StrCpy "$SETTINGSDIRECTORY" "$EXEDIR\$0"
            Goto +5
            IfFileExists "$EXEDIR\$PROGRAMEXECUTABLE" 0 +3
                StrCpy "$SETTINGSDIRECTORY" "$EXEDIR"  ;everything in the same folder
                Goto +2
                StrCpy "$SETTINGSDIRECTORY" "$EXEDIR\Data\settings"  ;standard configuration
        ReadINIStr $ADDITIONALPARAMETERS "$INIPATH\${NAME}.ini" "${NAME}" "AdditionalParameters"
        ReadINIStr $DISABLESPLASHSCREEN "$INIPATH\${NAME}.ini" "${NAME}" "DisableSplashScreen"
        StrCmp "'$DISABLESPLASHSCREEN'" "'true'"  +2 0
            StrCpy $DISABLESPLASHSCREEN "false"
        ClearErrors
        ReadINIStr $SHOWCOMMANDLINE "$INIPATH\${NAME}.ini" "${NAME}" "ShowCommandLine"
        StrCmp "'$SHOWCOMMANDLINE'" "'true'"  +2 0
            StrCpy $SHOWCOMMANDLINE "false"
        ClearErrors
        Goto CheckProgramExe

    NoINI:
        StrCpy "$ADDITIONALPARAMETERS" ""
        StrCpy $DISABLESPLASHSCREEN "false"
        StrCpy $SHOWCOMMANDLINE "false"
        StrCpy "$PROGRAMEXECUTABLE" "${DEFAULTEXE}"
        IfFileExists "$EXEDIR\$PROGRAMEXECUTABLE" 0 +4
            StrCpy "$PROGRAMDIRECTORY" "$EXEDIR"  ;everything in the same folder
            StrCpy "$SETTINGSDIRECTORY" "$EXEDIR"
            Goto +3
            StrCpy "$PROGRAMDIRECTORY" "$EXEDIR\App\${DEFAULTAPPDIR}"  ;standard configuration
            StrCpy "$SETTINGSDIRECTORY" "$EXEDIR\Data\settings"

    CheckProgramExe:
    ; ---------------------------------------------------------------
    ; Check directory configuration used and set variables accordingly
    ; $EXEDIR - The directory containing the installer executable
    IfFileExists "$PROGRAMDIRECTORY\$PROGRAMEXECUTABLE" 0 +2
        GoTo FoundEXE
    IfFileExists "$EXEDIR\$PROGRAMEXECUTABLE" 0 NoProgramEXE
            StrCpy "$PROGRAMDIRECTORY" "$EXEDIR"  ;everything in the same folder
        GoTo FoundEXE

    NoProgramEXE:
        ;=== Program executable not where expected
        MessageBox MB_OK|MB_ICONSTOP|MB_TOPMOST `Error -- file not found:  "$PROGRAMEXECUTABLE"$\r$\n$\r$\n\
                        It should be in either of:$\r$\n"$PROGRAMDIRECTORY"$\r$\n"$EXEDIR"`
        Abort

    FoundEXE:
    ; Check if launcher is already running
    System::Call 'kernel32::CreateMutexA(i 0, i 0, t "${NAME}2") i .r1 ?e'
    Pop $0
    StrCmp $0 0 +5
        FindProcDLL::FindProc "$PROGRAMEXECUTABLE"   ;the launcher is running, check Exe (case sensitive!)
        StrCmp $R0 "1" +3    ;if Exe not running: Launcher is making registry backup and ending
        ;MessageBox MB_YESNO|MB_ICONINFORMATION|MB_TOPMOST|MB_DEFBUTTON2  'Warning: "$EXEFILE" is already running.$\n$\nIf a Portable Spana window is not visible choose "NO".$\n$\nContinue?' IDNO "TheEnd"
        StrCpy $SECONDLAUNCH "true"

    StrCmp $DISABLESPLASHSCREEN "true" NoSplash
            ;=== Show the splash screen before processing the files
            InitPluginsDir
            File /oname=$PLUGINSDIR\splash.gif "${NAME}.gif"
            newadvsplash::show /NOUNLOAD 500 0 50 0xFFFFFF /L $PLUGINSDIR\splash.gif
            NoSplash:

    ; Check if EXE already running
    StrCmp $SECONDLAUNCH "true" GetPassedParameters
        FindProcDLL::FindProc "$PROGRAMEXECUTABLE"   ;is exe running? (case sensitive!)
        StrCmp $R0 "1" "" GetPassedParameters
        StrCpy $SECONDLAUNCH "true"
        ;MessageBox MB_OK|MB_ICONINFORMATION|MB_TOPMOST `"$PROGRAMEXECUTABLE" is already running`

    GetPassedParameters:
        ClearErrors
        ${GetParameters} $0
        StrCmp $SHOWCOMMANDLINE "true"  0  +2
            StrCpy $0 '-ShowCommandLine $0'
        StrCmp "'$0'" "''" "" LaunchProgramParameters
            ; No parameters
            StrCpy $EXECSTRING '"$PROGRAMDIRECTORY\$PROGRAMEXECUTABLE"'
            Goto AdditionalParameters
        LaunchProgramParameters:
            StrCpy $EXECSTRING '"$PROGRAMDIRECTORY\$PROGRAMEXECUTABLE" $0'
        AdditionalParameters:
            StrCmp $ADDITIONALPARAMETERS "" PassedParametersEnd
            ; Additional Parameters
            StrCpy $EXECSTRING '$EXECSTRING $ADDITIONALPARAMETERS'
        PassedParametersEnd:

        StrCmp $SHOWCOMMANDLINE "true" AlreadyTrue
        ClearErrors
        ${GetOptions} $0 "-dbg" $2
        IfErrors +2 0
            StrCpy $SHOWCOMMANDLINE "true"
        ${GetOptions} $0 "/dbg" $2
        IfErrors +2 0
            StrCpy $SHOWCOMMANDLINE "true"
        AlreadyTrue:

    ;-------------------------------------------------------------
    ;If already running, do not work with the registry
    StrCmp $SECONDLAUNCH "true" LaunchAndExit

    ;get current dirve letter
    ${GetRoot} "$EXEDIR" $CURRENTDRIVE
    ;the SETTINGSDIRECTORY is used to save the current drive leter
    IfFileExists "$SETTINGSDIRECTORY\*.*" CheckDirsEnd
    CreateDirectory "$SETTINGSDIRECTORY"
    CheckDirsEnd:

    ;-------------------------------------------------------------
    ;RegistryBackup:
        ;-Windows Registry:  existing entries are backed up in the Registy itself
        ;-Changes in the registry made by this portable application are
        ;      stored in $SETTINGSDIRECTORY and restored before next launch
        ;-Backup the registry unless there is already a backup
        ${registry::KeyExists} "HKEY_CURRENT_USER\Software\Eq-Diagr_Backup_by_SpanaPortable" $R0
        StrCmp $R0 "0" RegistryBackupEnd
        ;-Backup not found --  Make a backup entry for each file association
        ;# MessageBox MB_YESNO|MB_ICONQUESTION|MB_TOPMOST `No backup found. Going to create backup.$\n$\nContinue Registry Backup?` IDNO RegistryBackupEnd
        ;-Backup file association for PLT-files
        ${registry::KeyExists} "HKEY_CURRENT_USER\Software\Classes\.plt" $R0
        StrCmp $R0 "-1" +4
            ${registry::MoveKey} "HKEY_CURRENT_USER\Software\Classes\.plt" "HKEY_CURRENT_USER\Software\Eq-Diagr_Backup_by_SpanaPortable\.plt"  $R0
            Sleep 50
            DeleteRegKey HKCU "Software\Classes\.plt"
        ${registry::KeyExists} "HKEY_CURRENT_USER\Software\Classes\Eq-Diagr_PLT_File" $R0
        StrCmp $R0 "-1" +4
            ${registry::MoveKey} "HKEY_CURRENT_USER\Software\Classes\Eq-Diagr_PLT_File" "HKEY_CURRENT_USER\Software\Eq-Diagr_Backup_by_SpanaPortable\Eq-Diagr_PLT_File" $R0
            Sleep 50
            DeleteRegKey HKCU "Software\Classes\Eq-Diagr_PLT_File"

        ;-Backup file association for DAT-files
        ${registry::KeyExists} "HKEY_CURRENT_USER\Software\Classes\.dat" $R0
        StrCmp $R0 "-1" +4
            ${registry::MoveKey} "HKEY_CURRENT_USER\Software\Classes\.dat" "HKEY_CURRENT_USER\Software\Eq-Diagr_Backup_by_SpanaPortable\.dat"  $R0
            Sleep 50
            DeleteRegKey HKCU "Software\Classes\.dat"
        ${registry::KeyExists} "HKEY_CURRENT_USER\Software\Classes\Eq-Diagr_DAT_File" $R0
        StrCmp $R0 "-1" +4
            ${registry::MoveKey} "HKEY_CURRENT_USER\Software\Classes\Eq-Diagr_DAT_File" "HKEY_CURRENT_USER\Software\Eq-Diagr_Backup_by_SpanaPortable\Eq-Diagr_DAT_File" $R0
            Sleep 50
            DeleteRegKey HKCU "Software\Classes\Eq-Diagr_DAT_File"

    RegistryBackupEnd:

    ;AdjustPaths:
        ;Change drive letter in ${REGBAKFILE}
        ;(the drive may change from computer to computer)
        ;Get driver letter for last session
        ReadINIStr $LASTDRIVE "$SETTINGSDIRECTORY\${PORTABLESETTINGS}" "${NAME}Settings" "LastDrive"
        IfFileExists "$SETTINGSDIRECTORY\${REGBAKFILE}" 0 AdjustPathsINI
        StrCmp  "'$LASTDRIVE'"  "'$CURRENTDRIVE'"  AdjustPathsINI
        ClearErrors
        StrCpy $R2 "$LASTDRIVE"
        StrCpy $R3 "$CURRENTDRIVE"
        ${LineFind} "$SETTINGSDIRECTORY\${REGBAKFILE}" "" "1:-1" "ReplacePathWithLineFind"
        IfErrors 0 AdjustPathsINI
        MessageBox MB_OK|MB_ICONINFORMATION|MB_TOPMOST `Error while updating drive letters in file:$\r$\n    "$SETTINGSDIRECTORY\${REGBAKFILE}"$\r$\n\
                         when using routine "LineFind" (TextFunc.nsh)`
    AdjustPathsINI:
        ;Change drive letter in "$PROGRAMDIRECTORY\${AppName}.ini"
        ;(the drive may change from computer to computer)
        IfFileExists "$PROGRAMDIRECTORY\${AppName}.ini" 0 AdjustPathsEnd
        StrCmp  "'$LASTDRIVE'"  "'$CURRENTDRIVE'"  AdjustProgExe
        ClearErrors
        StrCpy $R2 "$LASTDRIVE"
        StrCpy $R3 "$CURRENTDRIVE"
        ${LineFind} "$PROGRAMDIRECTORY\${AppName}.ini" "" "1:-1" "ReplacePathWithLineFind"
        IfErrors 0 AdjustProgExe
        MessageBox MB_OK|MB_ICONINFORMATION|MB_TOPMOST `Error while updating drive letters in file:$\r$\n    "$PROGRAMDIRECTORY\${AppName}.ini"$\r$\nwhen using routine "LineFind" (TextFunc.nsh)`
        AdjustProgExe:
        ;Replace Spana.exe to SpanaPortable.exe
        ReadINIStr $0 "$PROGRAMDIRECTORY\${AppName}.ini" "${AppName}" "createDataFileProg"
        StrCmp "'$0'" "''" AdjustPathsEnd 0
            ${WordReplace} "$0" "$PROGRAMDIRECTORY\Database.exe" "$EXEDIR\DatabasePortable.exe" '+' $0
            ${WordReplace} "$0" "$PROGRAMDIRECTORY\Database.jar" "$EXEDIR\DatabasePortable.exe" '+' $0
            WriteINIStr  "$PROGRAMDIRECTORY\${AppName}.ini" "${AppName}" "createDataFileProg"  "$0"
        FlushINI "$PROGRAMDIRECTORY\${AppName}.ini"
        AdjustPathsEnd:

    ;ReadSettingsToRegistry:
        IfFileExists "$SETTINGSDIRECTORY\${REGBAKFILE}" "" RememberPath

        IfFileExists "$WINDIR\system32\reg.exe" "" ReadSettingsToRegistry9x
            nsExec::ExecToStack `"$WINDIR\system32\reg.exe" import "$SETTINGSDIRECTORY\${REGBAKFILE}"`
            Pop $R0
            StrCmp $R0 '0' RememberPath  ;successfully restored key(s)

    ReadSettingsToRegistry9x:
        ${registry::RestoreKey} "$SETTINGSDIRECTORY\${REGBAKFILE}" $R0
        StrCmp $R0 '0' RememberPath ;successfully restored key(s)
        ;StrCpy $FAILEDTORESTOREKEY "true"

    RememberPath:
        ClearErrors
        WriteINIStr "$SETTINGSDIRECTORY\${PORTABLESETTINGS}" "${NAME}Settings" "LastDrive" "$CURRENTDRIVE"
        FlushINI "$SETTINGSDIRECTORY\${PORTABLESETTINGS}"

    ;LaunchNow:
        System::Call 'Shell32::SHChangeNotify(i 0x8000000, i 0, i 0, i 0)'
        ;Sleep 100
        StrCmp $SHOWCOMMANDLINE "true" 0 +2
        MessageBox MB_OK|MB_TOPMOST 'Launch now, EXECSTRING =$\r$\n$EXECSTRING'
        ExecWait '$EXECSTRING'

    CheckRunning:
        Sleep 50
        FindProcDLL::FindProc "$PROGRAMEXECUTABLE"
        StrCmp $R0 "1" CheckRunning
        ;MessageBox MB_OK|MB_TOPMOST `"${AppName}.exe" finished`

    ;RestoreOriginalRegistry:
        ;-Save portable file associations and restore non-portable (local) Spana file associations
        ;-Portable file associations are saved in "$SETTINGSDIRECTORY\${REGBAKFILE}"
        Delete "$SETTINGSDIRECTORY\${REGBAKFILE}"

        ;-Save plot-file association and restore backup
        !define Index "Line${__LINE__}"
        ${registry::KeyExists} "HKEY_CURRENT_USER\Software\Classes\Eq-Diagr_PLT_File" $R0
        ;# MessageBox MB_YESNO|MB_ICONQUESTION|MB_TOPMOST `HKCU\Software\Classes\Eq-Diagr_PLT_File   exists = "$R0"  (0=true)$\n$\nContinue Registry Restore?` IDNO TheEnd
        StrCmp $R0 "-1" "${Index}-Key1Restore"  ;no Key?
            ;-check that we "own" it
            ReadRegStr $1 HKCU "Software\Classes\Eq-Diagr_PLT_File\shell\open\command" ""
            ;# MessageBox MB_YESNO|MB_ICONQUESTION|MB_TOPMOST `HKCU\Software\Classes\Eq-Diagr_PLT_File\shell\open\command = "$1"$\n$\nContinue Registry Restore?` IDNO TheEnd
            ${WordFind} $1 "$PROGRAMDIRECTORY" "E+1{" $R9    ;is it our association?
            IfErrors  +3  0
                  ${registry::SaveKey} "HKEY_CURRENT_USER\Software\Classes\Eq-Diagr_PLT_File" "$SETTINGSDIRECTORY\${REGBAKFILE}" "/A=1" $0
                  Goto "${Index}-Key1Restore"
                  ;-something is wrong! the register has been changed by some other program
                  MessageBox MB_OK|MB_ICONEXCLAMATION|MB_TOPMOST `Something is wrong!$\n$\nThe registry key: \
                      "HKCU\Software\Classes\Eq-Diagr_PLT_File"$\nPoints to "$1"$\nBut it should point to "$PROGRAMDIRECTORY"`
                  Goto "${Index}-Key1End"
        "${Index}-Key1Restore:"
            DeleteRegKey HKCU "Software\Classes\Eq-Diagr_PLT_File"
            ${registry::KeyExists} "HKEY_CURRENT_USER\Software\Eq-Diagr_Backup_by_SpanaPortable\Eq-Diagr_PLT_File" $R0
            StrCmp  $R0  "-1"  "${Index}-Key1End"   ;backup  found?
                ${registry::MoveKey} "HKEY_CURRENT_USER\Software\Eq-Diagr_Backup_by_SpanaPortable\Eq-Diagr_PLT_File" "HKEY_CURRENT_USER\Software\Classes\Eq-Diagr_PLT_File" $R0
        "${Index}-Key1End:"
        ${registry::KeyExists} "HKEY_CURRENT_USER\Software\Classes\.plt" $R0
        StrCmp $R0 "-1" "${Index}-Key2Restore" ;no Key?
            ReadRegStr $1 HKCU "Software\Classes\.plt" ""   ;save file association if needed
            StrCmp $1 "Eq-Diagr_PLT_File" 0 "${Index}-Key2Restore"
                    ${registry::SaveKey} "HKEY_CURRENT_USER\Software\Classes\.plt" "$SETTINGSDIRECTORY\${REGBAKFILE}" "/A=1 /G=0" $0     ;Append, No subkeys
            DeleteRegKey HKCU "Software\Classes\.plt"
        "${Index}-Key2Restore:"
            ${registry::KeyExists} "HKEY_CURRENT_USER\Software\Eq-Diagr_Backup_by_SpanaPortable\.plt" $R0
            StrCmp $R0 "-1"  "${Index}-Key2End"   ;backup found?
                  DeleteRegKey HKCU "Software\Classes\.plt"
                  ${registry::MoveKey} "HKEY_CURRENT_USER\Software\Eq-Diagr_Backup_by_SpanaPortable\.plt" "HKEY_CURRENT_USER\Software\Classes\.plt" $R0
                  Sleep 50
            "${Index}-Key2End:"
        !undef Index

        ;-Save data-file association and restore backup
        !define Index "Line${__LINE__}"
        ${registry::KeyExists} "HKEY_CURRENT_USER\Software\Classes\Eq-Diagr_DAT_File" $R0
        StrCmp $R0 "-1" "${Index}-Key1Restore" ;no Key?
            ;-check that we "own" it
            ReadRegStr $1 HKCU "Software\Classes\Eq-Diagr_DAT_File\shell\open\command" ""
            ;# MessageBox MB_YESNO|MB_ICONQUESTION|MB_TOPMOST `HKCU\Software\Classes\Eq-Diagr_DAT_File\shell\open\command = "$1"$\n$\nContinue Registry Restore?` IDNO TheEnd
            ${WordFind} $1 "$PROGRAMDIRECTORY" "E+1{" $R9    ;is it our association?
            IfErrors  +3  0
                  ${registry::SaveKey} "HKEY_CURRENT_USER\Software\Classes\Eq-Diagr_DAT_File" "$SETTINGSDIRECTORY\${REGBAKFILE}" "/A=1" $0
                  Goto "${Index}-Key1Restore"
                  ;-something is wrong! the register has been changed by some other program
                  MessageBox MB_OK|MB_ICONEXCLAMATION|MB_TOPMOST `Something is wrong!$\n$\nThe registry key: \
                      "HKCU\Software\Classes\Eq-Diagr_DAT_File"$\nPoints to "$1"$\nBut it should point to "$PROGRAMDIRECTORY"`
                  Goto "${Index}-Key1End"
        "${Index}-Key1Restore:"
            DeleteRegKey HKCU "Software\Classes\Eq-Diagr_DAT_File"
            ${registry::KeyExists} "HKEY_CURRENT_USER\Software\Eq-Diagr_Backup_by_SpanaPortable\Eq-Diagr_DAT_File" $R0
            StrCmp  $R0  "-1"  "${Index}-Key1End"   ;backup  found?
                ${registry::MoveKey} "HKEY_CURRENT_USER\Software\Eq-Diagr_Backup_by_SpanaPortable\Eq-Diagr_DAT_File" "HKEY_CURRENT_USER\Software\Classes\Eq-Diagr_DAT_File" $R0
        "${Index}-Key1End:"
        ${registry::KeyExists} "HKEY_CURRENT_USER\Software\Classes\.dat" $R0
        StrCmp $R0 "-1" "${Index}-Key2Restore" ;no Key?
            ReadRegStr $1 HKCU "Software\Classes\.dat" ""   ;save file association if needed
            StrCmp $1 "Eq-Diagr_DAT_File" 0 "${Index}-Key2Restore"
                    ${registry::SaveKey} "HKEY_CURRENT_USER\Software\Classes\.dat" "$SETTINGSDIRECTORY\${REGBAKFILE}" "/A=1" $0    ;Append, No subkeys
            DeleteRegKey HKCU "Software\Classes\.dat"
        "${Index}-Key2Restore:"
            ${registry::KeyExists} "HKEY_CURRENT_USER\Software\Eq-Diagr_Backup_by_SpanaPortable\.dat" $R0
            StrCmp $R0 "-1"  "${Index}-Key2End"   ;backup found?
                  DeleteRegKey HKCU "Software\Classes\.dat"
                  ${registry::MoveKey} "HKEY_CURRENT_USER\Software\Eq-Diagr_Backup_by_SpanaPortable\.dat" "HKEY_CURRENT_USER\Software\Classes\.dat" $R0
                  Sleep 50
            "${Index}-Key2End:"
        !undef Index

        DeleteRegKey HKCU "Software\Eq-Diagr_Backup_by_SpanaPortable"

        ;Clean the registry:  Remove the most common garbage
        StrCpy $9 "$PROGRAMEXECUTABLE"
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.bmp\OpenWithList"
        Call DeleteHKCURegistryValue_Data
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.dat\OpenWithList"
        Call DeleteHKCURegistryValue_Data
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.plt\OpenWithList"
        Call DeleteHKCURegistryValue_Data
        StrCpy $8 "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.wmf\OpenWithList"
        Call DeleteHKCURegistryValue_Data

        DeleteRegValue HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.plt\OpenWithProgids"  "Eq-Diagr_PLT_File"
        DeleteRegKey /ifempty HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.plt\OpenWithProgids"

        DeleteRegValue HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.dat\OpenWithProgids"  "Eq-Diagr_DAT_File"
        DeleteRegKey /ifempty HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.dat\OpenWithProgids"

        DeleteRegValue HKCU "Software\Microsoft\Windows\ShellNoRoam\MUICache"  "$EXEDIR\${NAME}.exe"
        DeleteRegValue HKCU "Software\Microsoft\Windows\ShellNoRoam\MUICache"  "$PROGRAMDIRECTORY\$PROGRAMEXECUTABLE"
        DeleteRegValue HKCU "Software\Microsoft\Windows\ShellNoRoam\MUICache"  "$PROGRAMDIRECTORY\SED.exe"
        DeleteRegValue HKCU "Software\Microsoft\Windows\ShellNoRoam\MUICache"  "$PROGRAMDIRECTORY\PREDOM.exe"
        DeleteRegValue HKCU "Software\Microsoft\Windows\ShellNoRoam\MUICache"  "$PROGRAMDIRECTORY\PREDOM2.exe"
        DeleteRegValue HKCU "Software\Microsoft\Windows\ShellNoRoam\MUICache"  "$PROGRAMDIRECTORY\PLOT-PDF.exe"
        DeleteRegValue HKCU "Software\Microsoft\Windows\ShellNoRoam\MUICache"  $EXEDIR\${NAME}.exe"

        System::Call 'Shell32::SHChangeNotify(i 0x8000000, i 0, i 0, i 0)'
    Goto TheEnd

    LaunchAndExit:
        StrCmp $SHOWCOMMANDLINE "true" 0 +2
            MessageBox MB_OK|MB_TOPMOST 'Launch and exit, EXECSTRING =$\r$\n$EXECSTRING'
        Exec '$EXECSTRING'

    TheEnd:
        StrCmp $SECONDLAUNCH "true" +2
        ${registry::Unload}
        StrCmp $DISABLESPLASHSCREEN "true" +2
        newadvsplash::stop /WAIT
        ;MessageBox MB_OK|MB_TOPMOST `The End`
SectionEnd  ;"Main"

Function ReplacePathWithLineFind
    ;Replace last drive letter with current drive letter
    ${WordReplace} "$R9" "$R2" "$R3" '+' $R9
    Push $0
    ClearErrors
FunctionEnd  ;ReplacePathWithLineFind

Function DeleteHKCURegistryValue_Data
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
            ${WordFind}  "$2"  "$9"  "E+1{"  $3    ;contains the text?
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
FunctionEnd  ;DeleteHKCURegistryValue_Data
