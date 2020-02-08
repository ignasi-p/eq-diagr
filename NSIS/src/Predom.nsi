;-----------------------------------------------
;  Predom.nsi
;-----------------------------------------------
; Java Launcher
;-----------------------------------------------

Name "Predom Java Launcher"
Caption "Predom Java Launcher"
Icon "images/Predom.ico"
OutFile "Predom.exe"
Unicode true

;--------------------------------
;Include Version Information
LoadLanguageFile "${NSISDIR}\Contrib\Language files\English.nlf"
!define /date DATE "%Y.%m.%d.01"
  VIProductVersion "${DATE}"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName" "Predom Java Launcher"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "Comments" "launches Predom.jar"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright" "I.Puigdomenech"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "Chemical Equilibrium Diagrams"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion" "${__DATE__}"
;--------------------------------
XPStyle on
RequestExecutionLevel user
SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow

!define CLASSPATH "Predom.jar"
!define JAR "Predom.jar"
!define PRODUCT_NAME "Predom"

; use javaw.exe to avoid dos box.
; use java.exe to keep stdout/stderr
!define JAVAEXE "javaw.exe"
!define JRE_VERSION "7.0"

!include "FileFunc.nsh"
!insertmacro GetFileVersion
!insertmacro GetParameters
!insertmacro GetOptions
!include "WordFunc.nsh"
!insertmacro VersionCompare

Var DBG ; debug

!macro CallFindFiles DIR FILE CBFUNC
Push "${DIR}"
Push "${FILE}"
Push $0
GetFunctionAddress $0 "${CBFUNC}"
Exch $0
Call FindFiles
!macroend

Section ""
  ; check that the jar-file exists
  StrCpy $0 "$EXEDIR\${JAR}"
  IfFileExists "$0" +3 +1
  MessageBox MB_OK|MB_ICONSTOP|MB_TOPMOST 'File not found: "$0"'
  Abort

  ; Debug mode if $DBG is "true"
  StrCpy $DBG "false"
  ; get command-line parameters
  ${GetParameters} $1
  ; do the parameters start with "-ShowCommandLine"?
  StrCpy $2 $1 16       ;get the first 16 letters
  StrCmp $2 "-ShowCommandLine" 0 +3
  StrCpy $DBG "true"
  StrCpy $1 $1 "" 17       ;remove the first 17 letters
  ; do the parameters contain either -dbg or /dbg?
  ClearErrors
  ${GetOptions} $1 "-dbg" $2
  IfErrors +2 0
  StrCpy $DBG "true"
  ${GetOptions} $1 "/dbg" $2
  IfErrors +2 0
  StrCpy $DBG "true"

  ; check if Java is in PATH by only using ${JAVAEXE} (without path)
  StrCpy $0 ${JAVAEXE}
  ; at this point $0 is the the java exe-file, and $1 contains the command-line parameters
  ; StrCpy $0 '"$0" -classpath "$EXEDIR\${CLASSPATH}" -jar "$EXEDIR\${JAR}" $1'  ; change for your purpose
  StrCpy $2 '"$0" -jar "$EXEDIR\${JAR}" $1'
  StrCmp $DBG "true" 0 +2
  MessageBox MB_OK|MB_TOPMOST "ExecWait =$\n $2"
  ClearErrors
  ExecWait $2 $3
  IfErrors javaNOTinPATH
  Abort

  javaNOTinPATH:   ; Java is NOT in PATH, try to find ${JAVAEXE}
  StrCmp $DBG "true" 0 +2
  MessageBox MB_OK|MB_TOPMOST "Java not in path..."
  ; get the Java Runtime Environment
  Call GetJRE
  Pop $0
  StrCmp $0 ${JAVAEXE} 0 JRE_OK ; success?

  StrCmp $DBG "true" 0 +2
  MessageBox MB_OK|MB_ICONSTOP|MB_TOPMOST 'Could NOT find file "${JAVAEXE}" version ${JRE_VERSION} or higher$\n\
  in the following:$\n  1-  .\jre and .\java directories\
		   $\n  2-  ..\jre and ..\java\
	       $\n  3-  JAVA_HOME environment variable\
	       $\n  4-  \PortableApps\CommonFiles\Java\
		   $\n  5-  the Windows registry\
		   $\nWill now search all "$PROGRAMFILES" subdirectories ... '

  ; search the directory tree $PROGRAMFILES for file ${JAVAEXE}
  ; "FindJRE" is a callback function
  !insertmacro CallFindFiles $PROGRAMFILES ${JAVAEXE} FindJRE
  StrCmp $0 "" 0 JRE_OK ; success?
  MessageBox MB_OK|MB_TOPMOST `Java not found in "$PROGRAMFILES" ...`

  ; not found anywhere...
  ; use only ${JAVAEXE} (without path) and display error messages
  StrCpy $0 ${JAVAEXE}
  Goto Go_ahead

  JRE_OK:
  ;StrCmp $DBG "true" 0 +2
  ;MessageBox MB_OK|MB_ICONINFORMATION|MB_TOPMOST 'Found java at:$\n  "$0"'

  Go_ahead:
  ; at this point $0 is the the java exe-file, and $1 contains the command-line parameters
  ; StrCpy $0 '"$0" -classpath "$EXEDIR\${CLASSPATH}" -jar "$EXEDIR\${JAR}" $1'  ; change for your purpose
  StrCpy $2 '"$0" -jar "$EXEDIR\${JAR}" $1'

  StrCmp $DBG "true" 0 +2
  MessageBox MB_OK|MB_TOPMOST "ExecWait (last attempt)=$\n $2"

  ; wait for return code
  ExecWait "$2" $3
        IfErrors +1 +2
        MessageBox MB_OK|MB_ICONSTOP|MB_TOPMOST `Error while running$\r$\n"$2"`
        StrCmp "'$3'" "'0'" +3 +1
        StrCmp "'$3'" "''" +2 +1
        MessageBox MB_OK|MB_ICONSTOP|MB_TOPMOST `Return code "$3" while running$\r$\n"$2"`
SectionEnd

Function CheckJREVersion
; Pass the "javaw.exe" path by $0
; it sets the error flag if the javaw.exe has an old version.
    Push $5
    Push $6
    ; Get the file version of javaw.exe
    ${GetFileVersion} $0 $5
	;${VersionCompare} "[Version1]" "[Version2]" $var
	;"[Version1]"        First version
	;"[Version2]"        Second version
	;$var              Result:
    ;                    $var=0  Versions are equal
    ;                    $var=1  Version1 is newer
    ;                    $var=2  Version2 is newer
    ${VersionCompare} ${JRE_VERSION} $5 $6
    ClearErrors
    StrCmp $6 "1" 0 CheckDone
    SetErrors
    StrCmp $DBG "true" 0 +2
    MessageBox MB_OK|MB_ICONINFORMATION|MB_TOPMOST 'Note --- file:$\n  "$0"$\nhas version $5.$\n$\n\
	                                               This software requires Java ${JRE_VERSION} or higher.'
    Pop $5
    Pop $6
    Return
  CheckDone:
    StrCmp $DBG "true" 0 +2
    MessageBox MB_OK|MB_ICONINFORMATION|MB_TOPMOST 'Note --- found java at:$\n  "$0"$\nVersion $5.'
    Pop $5
    Pop $6
FunctionEnd

Function GetJRE
;  Function GetJRE -- returns the full path of a valid java.exe in variable $0
;  looks in:
;  1 - .\jre and .\java directory (JRE Installed with application)
;  2 - JAVA_HOME environment variable
;  3 - \PortableApps\CommonFiles\Java
;      ..\jre
;      ..\java
;  4 - the registry
;  5 - hopes it is in current dir or PATH
    Push $0
    Push $1
    Push $2

  ; 1) Check local JRE
  ;CheckLocal:
    ClearErrors
    StrCpy $0 "$EXEDIR\jre\bin\${JAVAEXE}"
    IfFileExists $0 0 +3
    Call CheckJREVersion
    IfErrors 0 JreFound
    StrCpy $0 "$EXEDIR\java\bin\${JAVAEXE}"
    IfFileExists $0 0 CheckJavaHome
    Call CheckJREVersion
    IfErrors 0 JreFound

  ; 2) Check for JAVA_HOME
  CheckJavaHome:
    ClearErrors
    ReadEnvStr $0 "JAVA_HOME"
	; if there is an error reading the string the error flag is set
	IfErrors PortableJRE
    StrCpy $0 "$0\bin\${JAVAEXE}"
    IfFileExists $0 0 PortableJRE
    Call CheckJREVersion
    IfErrors 0 JreFound

  PortableJRE:
  ; 3) Check for Portable Java
    ClearErrors
    ; check in \PortableApps\CommonFiles\Java
    ${GetRoot} "$EXEDIR" $1
    StrCpy $0 "$1\PortableApps\CommonFiles\Java\bin\${JAVAEXE}"
    IfErrors PortableJRE2
    IfFileExists $0  0  PortableJRE2
    Call CheckJREVersion
    IfErrors  0  JreFound
  PortableJRE2:
    ; check "..\jre"
    ${GetParent} "$EXEDIR" $1
    StrCpy $0 "$1\jre\bin\${JAVAEXE}"
    IfErrors PortableJRE3
    IfFileExists $0  0  PortableJRE3
    Call CheckJREVersion
    IfErrors  0  JreFound
  PortableJRE3:
    ; check ..\java
    StrCpy $0 "$1\java\bin\${JAVAEXE}"
    IfErrors CheckRegistry
    IfFileExists $0  0  CheckRegistry
    Call CheckJREVersion
    IfErrors  0 JreFound

  ; 4) Check for registry
  CheckRegistry:
    ClearErrors
    ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
    StrCpy $0 "$0\bin\${JAVAEXE}"
    IfErrors GoodLuck
    IfFileExists $0 0 GoodLuck
    Call CheckJREVersion
    IfErrors  0 JreFound

  ; 5) wishing you good luck
  GoodLuck:
    StrCpy $0 "${JAVAEXE}"
    ;Abort

  JreFound:
    ;MessageBox MB_OK|MB_ICONINFORMATION|MB_TOPMOST "JreFound"
    Pop $2
    Pop $1
    Exch $0
FunctionEnd

Function FindJRE
; Callback Function Syntax
; Do not use $R0-$R6 in the function unless you are saving their old content
; Return a "stop" value if you want to stop searching, anything else to continue
; Always return a value through the stack (Push) to prevent stack corruption
; Do not push values on the stack without poping them later unless it's the return value
	Pop $0
	Call CheckJREVersion
	IfErrors 0 FindJRE_done
	StrCpy $0 ""
	Push "go"
	Return
	FindJRE_done:
	Push "stop"	
FunctionEnd

Function FindFiles
; from http://nsis.sourceforge.net/Search_For_a_File
; This function searches a given directory and all of its subdirectories for a certain file (or directory).
; Whenever it finds a match it calls a given callback function. If the callback function returns "stop"
; the function will stop searching.
;
; The function gets a directory to search, a file/directory name and an address to a callback function
; (use GetFunctionAddress) through the stack.
;
; Warning: do not push a directory with a trailing backslash. To remove the trailing backslash automatically
; use (assuming the input is in $0):
;	Push $0       # directory to remove trailing backslash from.         Stack: $0(with backslash)
;	Exch $EXEDIR  # exchange with a built-in dir var - exedir will do.
;              	  # NSIS automatically removes the trailing backslash.   Stack: $EXEDIR(original)
;	Exch $EXEDIR  # restore original dir var.                            Stack: $0(without backslash)
;	Pop $0        # and pop the directory without the backslash.         Stack: <clean>
;
; A simple macro is included to simplify the process of getting your callback function's address.
;	!macro CallFindFiles DIR FILE CBFUNC
;	Push "${DIR}"
;	Push "${FILE}"
;	Push $0
;	GetFunctionAddress $0 "${CBFUNC}"
;	Exch $0
;	Call FindFiles
;	!macroend
  Exch $R5 # callback function
  Exch 
  Exch $R4 # file name
  Exch 2
  Exch $R0 # directory
  Push $R1
  Push $R2
  Push $R3
  Push $R6
 
  Push $R0 # first dir to search
 
  StrCpy $R3 1
 
  nextDir:
    Pop $R0
    IntOp $R3 $R3 - 1
    ClearErrors
    FindFirst $R1 $R2 "$R0\*.*"
    nextFile:
      StrCmp $R2 "." gotoNextFile
      StrCmp $R2 ".." gotoNextFile
 
      StrCmp $R2 $R4 0 isDir
        Push "$R0\$R2"
        Call $R5
        Pop $R6
        StrCmp $R6 "stop" 0 isDir
          loop:
            StrCmp $R3 0 done
            Pop $R0
            IntOp $R3 $R3 - 1
            Goto loop
 
      isDir:
        IfFileExists "$R0\$R2\*.*" 0 gotoNextFile
          IntOp $R3 $R3 + 1
          Push "$R0\$R2"
 
  gotoNextFile:
    FindNext $R1 $R2
    IfErrors 0 nextFile
 
  done:
    FindClose $R1
    StrCmp $R3 0 0 nextDir
 
  Pop $R6
  Pop $R3
  Pop $R2
  Pop $R1
  Pop $R0
  Pop $R5
  Pop $R4
FunctionEnd