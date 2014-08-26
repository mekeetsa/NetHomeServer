#!/bin/sh
#
#Assumes root, make sure to call as 'sudo install.sh'
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
cp -r $SRCROOT $INSTALLATION_ROOT
chown -R $NH_USER $INSTALLATION_ROOT
cp $SRCPATH/rpi_deamon_start.sh $INSTALLATION_ROOT/rpi_deamon_start.sh
chmod +x $INSTALLATION_ROOT/rpi_deamon_start.sh

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

echo "Copying configurations..."
cp $SRCPATH/nethome /etc/init.d
chmod +x /etc/init.d/nethome
update-rc.d nethome	defaults
/etc/init.d/nethome start
