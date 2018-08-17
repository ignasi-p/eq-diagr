Chemical Equilibrium Diagrams
-----------------------------

This software has been developed using Netbeans (v.8.0.2 portable).

The help system is found in folder "Chem_Diagr_Help", and it uses
JavaHelp version 2.  The java source-code is in sub-folder
"src\chemicalDiagramsHelp", while the help contents is located
in sub-folder "src\html".  The JavaHelp system requires the also
following files in sub-folder "src\javahelp":
 - helpset.hs
 - index.xml
 - map.jhm
 - toc.xml

In order to add "search" functionality, it is necessary to create
search indexes with "jhindexer".  This may be performed with the
script "!_index.cmd".  The log-file "indexer_output.log" is created.
If the log-file is newer than the help-contents in "src\html", then
the re-indexing is not needed.  The search indexes are stored in
sub-folder "src\javahelp\JavaHelpSearch".

For Chem_Diagr_Help it is convenient to include the help contents
in the jar-file, so that the help system is a single file.
To package all dependent lib/jars and other files (the help
contents etc) into a single jar-file, the file "build.xml" has been
modified by adding a target named "Package-for-Store".  To build
this target, select the "Files" tab (next to the "Projects" tab),
then select the file "build.xml", right-click, and on the menu
that appears select "Run Target" and "Package-for-Store".
Netbeans will then generate the sub-folder "store" containing
"Chem_Diagr_Help.jar" which includes the java program and all
dependent libraries, the help contents, the search indexes, etc.

Changes in the help contents
============================
If you change an image or add or change a htm-file, then you
will also need to change:
 - The map list ("map.jhm") which contains all the links used in the
   help contents.
 - The table of contents (file "toc.xml") that appears on the left
   panel of the help window at start.
 - The index that appears on the left panel when the user selects
   the index tab (file "index.xml").
 - Update the search indexes with the script "!_index.cmd" mentioned
   above.
 - Update the "store" jar-file with the added or changed files.
   This may be performed with script "!_update_store.cmd".
