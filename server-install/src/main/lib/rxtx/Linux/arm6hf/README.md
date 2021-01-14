## Constant CPU usage by gnu.io.RXTXPort.eventLoop

*Bug:* The gnu.io.RXTXPort.eventLoop is constantly using the cpu (even if there is no activity on the serial port).
On Raspberry Pi 3B, the cpu usage is around 5-6%.

*Fix:* lines 4141-4142 in `src/SerialImp.c`, increase tv_usec from 1000 to 100000:

        // Fixed Constant CPU usage by gnu.io.RXTXPort.eventLoop.
        // Increased tv_usec from 1000 to 100000.
        // See: https://github.com/NeuronRobotics/nrjavaserial/issues/36
        eis->tv_sleep.tv_sec = 0;
        eis->tv_sleep.tv_usec = 100000;

See https://github.com/NeuronRobotics/nrjavaserial/issues/36

The procedure to compile rxtx follows.
At the end, from rxtx we only need the native library `librxtxSerial.so`. 

        wget http://gentoo.osuosl.org/distfiles/rxtx-2.2pre2.zip
        unzip rxtx-2.2pre2.zip
        cd rxtx-2.2pre2

        # change src/SerialImp.c
        # change pre1 -> pre2 in configure.*

        ./configure
        make

Note: The build will fail because of the missing symbol `UTS_RELASE` when building librxtxI2C.
However, `librxtxSerial.so` will be built already.
(If you want to proceede and build other libraries, add `#define UTS_RELEASE "rpi"` to `config.h`.)

Now, the file we want is `armv7l-unknown-linux-gnu/.libs/librxtxSerial-2.2pre2.so`.
It should be copied here as librxtsSerial.so then striped:

        cp -a ~/nhs-prerequisites/rxtx-2.2pre2/armv7l-unknown-linux-gnu/.libs/librxtxSerial-2.2pre2.so librxtxSerial.so
        strip librxtxSerial.so
        readelf -d librxtxSerial.so

At the end, `librxtxSerial.so` can be copied to the nethome runtime (`/opt/nethome/lib`):

        cp -a librxtxSerial.so /opt/nethome/lib/librxtxSerial.so

