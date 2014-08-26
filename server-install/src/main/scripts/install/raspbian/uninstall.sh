#!/bin/sh
#
#Assumes root, make sure to call as 'sudo install_daemon.sh'
#

INSTALLATION_ROOT=/opt/nethome
CONFIGURATION_ROOT=/etc/opt/nethome
LOG_ROOT=/var/log/nethome
PID_ROOT=/var/run/nethome

echo "Stopping Server"
update-rc.d /etc/init.d/nethome	remove
/etc/init.d/nethome stop
echo "Removing directories"
rm /etc/init.d/nethome
rm -Rf $INSTALLATION_ROOT
cp $CONFIGURATION_ROOT/config.xml /home/pi/old_config.xml
rm -Rf $CONFIGURATION_ROOT
rm -Rf $LOG_ROOT
rm -Rf $PID_ROOT
rm -Rf /home/nethome
