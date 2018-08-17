#!/bin/bash
# echo "The present working directory is `pwd`"
# echo "\$0: $0"
# echo "basename: `basename $0`"
# echo "dirname: `dirname $0`"
# echo "dirname/readlink: $(dirname $(readlink -f $0))"
echo "= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = ="
echo " Makes *.sh and jar-files executable in file: Eq-Diagr_Java.zip"
echo "= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = ="
echo " press Enter to continue ..."
read
echo " "
OLD_PATH="$PWD"

_S_="/media/$USER/EQ-DIAGR/Eq-Calc_Java"
# Loop forever (until break is issued)
while true; do

   if [ ! -e "$_S_/bundles/Eq-Diagr_Java.zip" ]
   then
     echo "Something is wrong."
     echo "Error -  file NOT FOUND:"
     echo "   $_S_/bundles/Eq-Diagr_Java.zip"
     break
   fi

   if [ ! -e "$_S_/ubuntu-files" ]
   then
     echo "Something is wrong."
     echo "Error -  folder NOT FOUND:"
     echo "   $_S_/ubuntu-files"
     break
   fi


   echo "The present working directory is: `pwd`"
   mkdir -p ./deleteMe/Eq-Diagr/ubuntu-files
   echo "->  cp  \"$_S_/ubuntu-files/installDesktop.sh\""
   echo "           ./deleteMe/Eq-Diagr/ubuntu-files"
   cp "$_S_/ubuntu-files/installDesktop.sh" ./deleteMe/Eq-Diagr/ubuntu-files
   echo "->  chmod  a+x  ubuntu-files/installDesktop.sh"
   chmod a+x ./deleteMe/Eq-Diagr/ubuntu-files/installDesktop.sh
   echo "->  cp  \"$_S_/ubuntu-files/installCmdLine.sh\""
   echo "           ./deleteMe/Eq-Diagr/ubuntu-files"
   cp "$_S_/ubuntu-files/installCmdLine.sh" ./deleteMe/Eq-Diagr/ubuntu-files
   echo "->  chmod  a+x  ubuntu-files/installCmdLine.sh"
   chmod a+x ./deleteMe/Eq-Diagr/ubuntu-files/installCmdLine.sh
   echo "->  touch -r (installDesktop.sh)"
   touch -r "$_S_/ubuntu-files/installDesktop.sh" ./deleteMe/Eq-Diagr/ubuntu-files/installDesktop.sh
   echo "->  touch -r (installCmdLine.sh)"
   touch -r "$_S_/ubuntu-files/installCmdLine.sh" ./deleteMe/Eq-Diagr/ubuntu-files/installCmdLine.sh
   echo "->  cp  \"$_S_/dist/*.jar\""
   echo "           ./deleteMe/Eq-Diagr"
   cp "$_S_"/dist/*.jar ./deleteMe/Eq-Diagr/
   echo "->  chmod  a+x  *.jar"
   chmod a+x ./deleteMe/Eq-Diagr/*.jar
   echo "->  touch -r (*.jar)"
   touch -r "$_S_"/dist/AddShowReferences.jar ./deleteMe/Eq-Diagr/AddShowReferences.jar
   touch -r "$_S_"/dist/Chem_Diagr_Help.jar ./deleteMe/Eq-Diagr/Chem_Diagr_Help.jar
   touch -r "$_S_"/dist/DataBase.jar ./deleteMe/Eq-Diagr/DataBase.jar
   touch -r "$_S_"/dist/DataMaintenance.jar ./deleteMe/Eq-Diagr/DataMaintenance.jar
   touch -r "$_S_"/dist/PlotPDF.jar ./deleteMe/Eq-Diagr/PlotPDF.jar
   touch -r "$_S_"/dist/PlotPS.jar ./deleteMe/Eq-Diagr/PlotPS.jar
   touch -r "$_S_"/dist/Predom.jar ./deleteMe/Eq-Diagr/Predom.jar
   touch -r "$_S_"/dist/SED.jar ./deleteMe/Eq-Diagr/SED.jar
   touch -r "$_S_"/dist/Spana.jar ./deleteMe/Eq-Diagr/Spana.jar

   echo "->  cd ./deleteMe"
   cd ./deleteMe
   echo "->  zip -o $_S_/bundles/Eq-Diagr_Java.zip"
   echo "              ./Eq-Diagr/ubuntu-files/*.sh"
   zip -o "$_S_"/bundles/Eq-Diagr_Java.zip Eq-Diagr/ubuntu-files/installDesktop.sh
   echo "->  zip -o $_S_/bundles/Eq-Diagr_Java.zip"
   echo "              ./Eq-Diagr/ubuntu-files/*.sh"
   zip -o "$_S_"/bundles/Eq-Diagr_Java.zip Eq-Diagr/ubuntu-files/installCmdLine.sh
   echo "->  zip -o $_S_/bundles/Eq-Diagr_Java.zip"
   echo "              ./Eq-Diagr/ubuntu-files/*.jar"
   zip -o "$_S_"/bundles/Eq-Diagr_Java.zip Eq-Diagr/*.jar
   unzip -T "$_S_"/bundles/Eq-Diagr_Java.zip
   touch -r "$_S_"/bundles/Eq-Diagr_Java.zip -d "+2 min" "$_S_"/bundles/Eq-Diagr_Java.zip
   cd ..
   rm -r ./deleteMe

   break
done

echo " "
cd "$OLD_PATH"
OLD_PATH=
_S_=
echo "All done."
echo "Press Enter to continue ..."
read
