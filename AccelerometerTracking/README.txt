Based on the Sun SPOT Telemetry Demo - Version 1.0

Author: Ron Goldman
June 9, 2006

This is a demo application that provides the basic framework for
collecting data on a remote SPOT and passing it over the radio to 
a host application that can record and display it. It was written
with the hope that it could be easily modified by people developing
new SPOT applications.

The framework shows how the remote SPOT and the host application can
locate each other by the use of broadcast packets. A point-to-point
radio connection is then established and used to pass commands and
replies between the host application and the remote SPOT.

The SPOT application consists of two Java classes: one to handle the
radio connection to the host application, and the second to read the
accelerometer and send the data to the host application.

Two auxiliary classes are also used: one to provide the basic
application framework and the other to listen for commands over the
USB connection (so you do not have to push the reset button when
deploying a new application).

The SPOT uses its LEDs to display its status as follows:

LED 1:
    * Red = running, but not connected to host
    * Green = connected to host display server 

LED 2:
    * Yellow = looking for host display server
    * Blue = calibrating accelerometer
    * Red blink = responding to a ping request
    * Green = sending accelerometer values using 2G scale
    * Blue-green = sending accelerometer values using 6G scale 


The host application consists of three main Java classes: one to
handle the radio connection to the remote SPOT, one to display the
data collected, and the third to manage the GUI.

The host display server lets you collect up to one minute of
accelerometer readings from the SPOT. It will ignore initial zero
values, so you won't see anything until you move the SPOT. In
addition to displaying the x, y and z forces, you can also display 
the magnitude of their vector sum (|a|). You can view the raw data 
or smooth it using either a boxcar or triangle filter. You can also 
change the width of the filter window, that is how many samples are 
used to filter each point.

The host server display also allows the collected data to be saved 
to a file, or a previously collected file to be viewed. The data 
can also be printed out. A sample file of collected accelerometer
readings is included: accel-sample.data for your viewing pleasure.

Javadoc can be found in Telemetry-onSpot/doc/index.html and 
Telemetry-onDesktop/doc/index.html


Here is how the 3 axis of the accelerometer map to the Sun SPOT:

                                  Z  X
      ________________            | /
     / / *           /|           |/
    /_/_*___________/ |       ----. --- Y 
    |_|  Sun SPOT   | /          /|
      |_____________|/          / |


-------------------------
Installation instructions
-------------------------

To build and run the demo first setup a SPOT to report on its
accelerometer:

cd Telemetry-onSpot
ant deploy run

Then startup a base station and run the desktop application.  
Note: Please edit the build.properties file to specify the port
where the base station can be found.

cd ../Telemetry-onDesktop
ant selectbasestation run
^C
ant host-run


---------------------------------
To setup for use in NetBeans IDE:
---------------------------------

Open each project in NetBeans.

Edit the "Properties" for each project. Change the "Java Source 
Classpath" to:

for Telemetry-onSpot:

<SDK_INSTALL_DIRECTORY>/lib/transducerlib_rt.jar
<SDK_INSTALL_DIRECTORY>/lib/spotlib_rt.jar
<SDK_INSTALL_DIRECTORY>/lib/spotlib_common.jar
<SDK_INSTALL_DIRECTORY>/lib/squawk_rt.jar

for Telemetry-onDesktop:

<SDK_INSTALL_DIRECTORY>/lib/spotlib_host.jar
<SDK_INSTALL_DIRECTORY>/lib/spotlib_common.jar
<SDK_INSTALL_DIRECTORY>/lib/squawk_classes.jar


