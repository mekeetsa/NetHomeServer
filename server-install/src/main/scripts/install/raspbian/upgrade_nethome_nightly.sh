#!/bin/sh

#Assumes root, make sure to call as 'sudo ./install.sh'
if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root. Call as 'sudo $0'" 1>&2
   exit 1
fi

NOWTIME=$(date +"%Y_%m_%d_%H_%M")
FILENAME=build$NOWTIME.zip

if [ ! -d "nethomebackup" ]; then
  mkdir nethomebackup
fi

cd nethomebackup
echo "Downloading latest OpenNetHome nightly build" 1>&2
wget http://wiki.nethome.nu/lib/exe/fetch.php/nethomeservernightly.zip
echo "Unpacking release" 1>&2
unzip nethomeservernightly.zip >/dev/null
chmod +x nethomeservernightly/install/raspbian/*.sh
if [ -x "/etc/init.d/nethome"  ]; then
  echo "Upgrading existing installation" 1>&2
  nethomeservernightly/install/raspbian/upgrade.sh
else
  echo "Making new installation" 1>&2
  nethomeservernightly/install/raspbian/install.sh
fi
rm -r nethomeservernightly
mv nethomeservernightly.zip $FILENAME
chmod a+w $FILENAME
cd ..
