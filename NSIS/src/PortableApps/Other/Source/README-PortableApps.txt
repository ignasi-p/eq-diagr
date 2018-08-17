Chemical Equilibrium Diagrams: Portable Launchers
=================================================


ABOUT PORTABLE CHEMICAL EQUILIBRIUM DIAGRAMS
============================================
The Portable Launchers (SpanaPortable.exe and DatabasePortable.exe)
allow you to run the java versions of Spana and Database from
a removable drive whose letter changes as you move it to another
computer.  The program can be entirely self-contained on the
drive and then used on any Windows computer.


INSTALLATION / DIRECTORY STRUCTURE
==================================
By default, the programs expect the following directory structure:

-\  <--- Directory with SpanaPortable.exe and DatabasePortable.exe
    +\App\
          +\Eq-Diagr\     <--- Directory with Spana.exe and Database.exe
          +\AppInfo\
    +\Data\
          +\settings\
    +\Other\
          +\Source\
          +\Help\

Alternatively, the launchers (SpanaPortable.exe and DatabasePortable.exe)
may be located directly in the same folder as Spana.exe.  If so, the settings
files are also kept in the same folder.

Any other directory configuration may be used by including the files
"SpanaPortable.ini" and "DatabasePortable.ini" in the same directory
as the launchers "SpanaPortable.exe" and "DatabasePortable.exe"
and configuring them as detailed in the INI file section below.

The subdirectory "Other\Source" contains this file, and example
INI-files and the source code for the launchers.

The subdirectory "App\Appinfo" contains information used by the
PortableApps.com portable application platform.

The subdirectory "Data\settings" can contain the file "SpanaPortable.reg"
that contains saved Windows Registry data (file associations) from the last
program execution.  The program Database does not make any registry
changes.  When launching the Spana program, any existing Spana-data
in the Windows Registry (from a local copy of the program) is saved
as backup in the Registry itself.  Then any data found in
"SpanaPortable.reg" is inserted into the Windows Registry.

Note that the drive letter can change when the removable drive is inserted
into another computer.  The file "SpanaPortableSettings.ini" in subdirectory
"Data\settings" contains the drive letter from which the Spana/Database
launchers started during the last session.  If the drive letter has changed
since the last session, the launcher will search "SpanaPortable.reg" and
the previous drive letter will be replaced with the current drive letter
before copying the information to the Windows Registry.  The same action
is taken on the files "Spana.ini" and "Database.ini.

When Spana ends the data in the Windows Registry is saved in "Data\settings"
and the backup information in the Registry is then restored, leaving the
Registry in the same state as it was before launching SpanaPortable.
The current drive letter is saved in file "SpanaPortableSettings.ini".

Note that any file name associations with Spana portable only operate while
Spana portable is running.  When you close Spana the file associations will
go back to those defined in the local computer.

Also note that after starting the Spana launcher, the restoring of any
Windows registy data will be made only after all Spana window is closed,
either local or portable.  Unpredictable behaviour could result if a
local copy of Spana is started while the portable Spana is running,
or vice-versa (because of the shuffling of registry entries for file
associations).


INI-FILE CONFIGURATION
======================
The Spana and Database Portable Launchers will look for an ini file called
"SpanaPortable.ini" and "DatabasePortable.ini".  If you are happy with
the default options, it is not necessary to change these files.  These files
are searched in the following folders: (1) in the same directory as
"DatabasePortable.exe" and "SpanaPortable.exe", (2) in the "Data" sub-folder,
(3) in the "Data\settings" sub-folder, and (4) in the "Other" sub-folder.
If the ini-files are are not found, default parameters are used.
The INI files are formatted as follows:

[SpanaPortable]
SpanaExecutable=Spana.exe
SpanaDirectory=App\Eq-Diagr
SettingsDirectory=Data\settings
AdditionalParameters=
DisableSplashScreen=true
ShowCommandLine=false

(The format of the DatabasePortable.ini file is equivalent)

The SpanaExecutable entry allows you to set the Spana Portable Launcher
to use an alternate EXE call to launch Spana.

The SpanaDirectory entry should be set to the *relative* path of the
appropriate directory from the location of the launcher (SpanaPortable.exe).
The default entry for this is described in the installation section above.

The SettingsDirectory entry should be set to the *relative* path of the
appropriate directory from the location of the launcher (SpanaPortable.exe).
The default entry for this is described in the installation section above.

The AdditionalParameters entry allows you to pass additional command line
parameter entries to the executable.  Whatever you enter here will be
appended to the call to the exe.

Set the DisableSplashScreen entry to "true" to suppress the display of the
splash screen when the launcher starts.

ShowCommandLine=true will show the command line in a dialog before
it is executed

PROGRAM HISTORY
===============
This launcher is loosely based on other launchers found at
http://PortableApps.com
