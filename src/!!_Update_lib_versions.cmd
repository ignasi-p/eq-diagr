@echo off
rem ---- Windows script to create new files "Version.java" in
rem      the libraries "LibChemDiagr" and "LibDataBase"
rem      describing the last modification date for each library

REM no prompt
prompt  $S$H
echo.-----------------------
REM base (root) folder
set _r_=..
REM the name of the version-file
set _vf_=%_r_%\LibChemDiagr\src\lib\Version.java
REM pointer to "touch"
set _touch_=\bin\GnuWin32\touch.exe
if NOT exist "%_touch_%" (
	echo ****  ERROR - file not found: "%_touch_%"
	goto xit
)

echo.
echo.Most recent java file in "LibChemDiagr"
del /q "%_vf_%" >nul
dir /-c /s /a:-d /o:d "%_r_%\LibChemDiagr\src\*.java" | sort /r | findstr /n "^[2]" | findstr /b /c:"1:" >deleteme.txt
type deleteme.txt
for /f "tokens=2,3,4 delims=: " %%a in (deleteme.txt) do (
	set FileDate=%%a
	set FileTime=%%b:%%c
)
echo FileDate="%FileDate%", FileTime="%FileTime%"
echo.package lib; >"%_vf_%"
echo.>>"%_vf_%"
echo./** Copyright (C) 2014-2020 I.Puigdomenech. >>"%_vf_%"
echo. *  >>"%_vf_%"
echo. * This program is free software: you can redistribute it and/or modify >>"%_vf_%"
echo. * it under the terms of the GNU General Public License as published by >>"%_vf_%"
echo. * the Free Software Foundation, either version 3 of the License, or >>"%_vf_%"
echo. * any later version. >>"%_vf_%"
echo. * >>"%_vf_%"
echo. * This program is distributed in the hope that it will be useful, >>"%_vf_%"
echo. * but WITHOUT ANY WARRANTY; without even the implied warranty of >>"%_vf_%"
echo. * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the >>"%_vf_%"
echo. * GNU General Public License for more details. >>"%_vf_%"
echo. * >>"%_vf_%"
echo. * You should have received a copy of the GNU General Public License >>"%_vf_%"
echo. * along with this program.  If not, see http://www.gnu.org/licenses/ >>"%_vf_%"
echo. * >>"%_vf_%"
echo. * @author Ignasi Puigdomenech */ >>"%_vf_%"
echo.public class Version { >>"%_vf_%"
echo.    static final String VERSION = "%FileDate%"; >>"%_vf_%"
echo.    public static String version() {return VERSION;} >>"%_vf_%"
echo.} >>"%_vf_%"
del /q deleteme.txt
echo.touch --time=modify --date="%FileDate% %FileTime%" "%_vf_%"
"%_touch_%" --time=modify --date="%FileDate% %FileTime%" "%_vf_%"
set FileDate=
set FileTime=
echo.-----------------------
set _vf_=%_r_%\LibDataBase\src\lib\database\Version.java
echo.
echo.Most recent java file in "LibDataBase"
del /q "%_vf_%" >nul
dir /-c /s /a:-d /o:d "%_r_%\LibDataBase\src\*.java" | sort /r | findstr /n "^[2]" | findstr /b /c:"1:" >deleteme.txt
type deleteme.txt
for /f "tokens=2,3,4 delims=: " %%a in ('type deleteme.txt') do (
	set FileDate=%%a
	set FileTime=%%b:%%c
)
echo.FileDate="%FileDate%", FileTime="%FileTime%"
echo.package lib.database; >"%_vf_%"
echo.>>"%_vf_%"
echo./** Copyright (C) 2014-2020 I.Puigdomenech. >>"%_vf_%"
echo. *  >>"%_vf_%"
echo. * This program is free software: you can redistribute it and/or modify >>"%_vf_%"
echo. * it under the terms of the GNU General Public License as published by >>"%_vf_%"
echo. * the Free Software Foundation, either version 3 of the License, or >>"%_vf_%"
echo. * any later version. >>"%_vf_%"
echo. * >>"%_vf_%"
echo. * This program is distributed in the hope that it will be useful, >>"%_vf_%"
echo. * but WITHOUT ANY WARRANTY; without even the implied warranty of >>"%_vf_%"
echo. * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the >>"%_vf_%"
echo. * GNU General Public License for more details. >>"%_vf_%"
echo. * >>"%_vf_%"
echo. * You should have received a copy of the GNU General Public License >>"%_vf_%"
echo. * along with this program.  If not, see http://www.gnu.org/licenses/ >>"%_vf_%"
echo. * >>"%_vf_%"
echo. * @author Ignasi Puigdomenech */ >>"%_vf_%"
echo.public class Version { >>"%_vf_%"
echo.    static final String VERSION = "%FileDate%"; >>"%_vf_%"
echo.    public static String version() {return VERSION;} >>"%_vf_%"
echo.} >>"%_vf_%"
del /q deleteme.txt >nul
echo.touch --time=modify --date="%FileDate% %FileTime%" "%_vf_%"
"%_touch_%" --time=modify --date="%FileDate% %FileTime%" "%_vf_%"
echo.
echo.-----------------------
echo.All done.
:xit
prompt  $p$g
set FileDate=
set FileTime=
set _vf_=
set _r_=
set _touch_=
pause
