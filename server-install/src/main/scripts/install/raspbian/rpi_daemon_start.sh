#!/bin/sh
PID_ROOT=/var/run/nethome
PID_FILE=$PID_ROOT/nethome.pid
CONFIGURATION_ROOT=/etc/opt/nethome
LOG_ROOT=/var/log/nethome
SCRIPTFILE=$0
cd lib
PID=`ps -ef | grep ${SCRIPTFILE} | head -n1 |  awk ' {print $2;} '`
echo ${PID} > ${PID_FILE}
chmod a+w ${PID_FILE}
exec java -Djava.library.path=. -jar home.jar -l$LOG_ROOT "$@" $CONFIGURATION_ROOT/config.xml
