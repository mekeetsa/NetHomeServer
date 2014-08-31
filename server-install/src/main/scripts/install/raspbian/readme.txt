NetHomeServer Linux installer

The install.sh script in this directory will install NetHomeServer as a daemon
that will start automatically when the computer is booted.
The script must be run as root:

chmod +x install.sh
sudo ./install.sh

This will copy the NetHomeServer files to appropriate locations in the file system
and install it as a daemon running as the user nethome.
To stop the server you type:

/etc/init.d/nethome stop

And to start it again you type:

/etc/init.d/nethome start

The installation files are moved to the following locations:

Executable files -> /opt/nethome/
Configuration file -> /etc/opt/nethome
Media files -> /etc/opt/nethome/media
Log files -> /var/log/nethome
Start script -> /etc/init.d/nethome