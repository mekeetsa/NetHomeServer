#!/bin/sh
#
echo "*Installing OpenNetHomeServer*" 1>&2
#Assumes root, make sure to call as 'sudo ./install.sh'
if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root. Call as 'sudo ./install.sh'" 1>&2
   exit 1
fi

#Check for previous daemon installation
if [ -e /etc/init.d/nhs-daemon ]; then
  echo "Previous version of daemon start installed. Please see readme_upgrade_old.txt for instructions on how to uninstall."
  exit 1
fi

#
SRCPATH=$(dirname $(readlink -f $0))
SRCROOT=$SRCPATH/../..
INSTALLATION_ROOT=/opt/nethome
CONFIGURATION_ROOT=/etc/opt/nethome
LOG_ROOT=/var/log/nethome
PID_ROOT=/var/run/nethome
PID_FILE=$PID_ROOT/nethome.pid
NH_GROUP=nethome
NH_USER=nethome

#Check so we don't overwrite an existing installation
if [ -d "$INSTALLATION_ROOT" -o -d "$CONFIGURATION_ROOT" -o -d "/home/nethome" ]; then
  echo "Server already installed. Please uninstall the old installation first."
  exit 1
fi

# Create user and group
getent group $NH_GROUP >/dev/null 2>&1
if [ $? -ne 0 ]; then
   echo Creating group $NH_GROUP
   groupadd -r $NH_GROUP || return 1
   fi
id -u $NH_USER >/dev/null 2>&1
if [ $? -ne 0 ]; then
   echo Creating user $NH_USER
   useradd -r -c "user for nethome service" -g $NH_GROUP -G users  $NH_USER
   fi
# Need a home directory for java prefs
mkdir /home/nethome
chown $NH_USER /home/nethome
chgrp $NH_USER /home/nethome
# Group membership to access serial ports
usermod -a -G dialout nethome
usermod -a -G tty nethome

# Main installation
echo "Copying files" 1>&2
cp -r $SRCROOT $INSTALLATION_ROOT
chown -R $NH_USER $INSTALLATION_ROOT
rm -f $INSTALLATION_ROOT/lib/librxtxSerial.so
cp $INSTALLATION_ROOT/os/librxtxSerial_raspian.so $INSTALLATION_ROOT/lib/librxtxSerial.so
cp $SRCPATH/rpi_daemon_start.sh $INSTALLATION_ROOT/rpi_daemon_start.sh
chmod +x $INSTALLATION_ROOT/rpi_daemon_start.sh
chmod -w $INSTALLATION_ROOT/lib

# Configuration
mkdir $CONFIGURATION_ROOT
mv $INSTALLATION_ROOT/lib/demo_rpi.xml $CONFIGURATION_ROOT/config.xml
mv $INSTALLATION_ROOT/media $CONFIGURATION_ROOT/
chown -R $NH_USER $CONFIGURATION_ROOT

# Logging
mkdir $LOG_ROOT
chown -R $NH_USER $LOG_ROOT

# pid-file
mkdir $PID_ROOT
chown -R $NH_USER $PID_ROOT

cp $SRCPATH/nethome /etc/init.d
chmod +x /etc/init.d/nethome
update-rc.d nethome	defaults
/etc/init.d/nethome start

echo "Installation complete."
echo "Browse to http://localhost:8020/home to configure the server"
