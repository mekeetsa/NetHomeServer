NetHome Server
==============

Welcome to the NetHomeServer project. NetHomeServer is the core of OpenNetHome, 
which is an open home automation project. For more details, see the project home page:
http://opennethome.org/

Release Notes
-------------

Note: The rxtx library 2.2pre1
exhibits high-cpu usage on Raspberry Pi.  
Use `librxtxSerial.so`from here: 
https://github.com/mekeetsa/rxtx-2.2pre2

Changes since the original version 3.0, Sep 29, 2018:

  -  Added configurable /web as static dir to JettyWEB (parameter WebDirectory)
  -  Added reconnect() to IkeaGateway and delayed (re)activate in HomeServer.java
  -  Fixed LoggerComponent to start at next even logInterval minutes
  -  Fixed LoggerComponent update homeItemId in activate()
  -  Added HtmlHeader parameter to HomeGUI.java (to emit custom header)
  -  Exposed reportNodes in IkeaGateway
  -  Removed unnecessary log reports in IkeaGateway
  -  Added html elemnt id's to itemcolumn in PortletPage
  -  Added class=attrvalue for defaultAttributeValue in PortletPage
  -  Fixed isImageFile in MediaPage
  -  Possibility to plot several data series (GraphPage.java and graph.js)
  -  Combined graph link (PortletPage.java, GraphPage.java and graph.js)
  -  Removed depricated async ajax in graph.js
  -  Async update of graphs, one-by-one series (graph.js)
  -  Fixed empty series (in graph.js)
  -  Fixed printDay boundaries in GraphPage.java
  -  Refactored names jquery-ui.custom.min.js jquery.min.js (dropped version nrs)
  -  Status report of the ongoing data series fetch (graph.js)
  -  Added SIGINT and SIGTERM handlers
  -  Upgraded jmdns dependency to version 3.5.6
  -  Added enable/disable to MDNSScanner.java. Commented out jmdns.close()
  -  Fixed an empty jsonurl in GraphPage.java
  -  Updated jquery 1.9.1, jquery-ui 1.9.2, jqplot 1.0.9
  -  Compressed jquery-ui.custom.min.js
  -  Fixed //@ sourceMappingURL in jquery.min.js
  -  Fixed invalid HTMLs
  -  Added an alternative printNavigateBar2 in HomeGUI.java
  -  Added attribute Navbar2 in HomeGUI.java
  -  Added 'itemInfo' html id to `<li>` under the homeiteminfo
  -  Added dropdown menu for 'All Items'
  -  Added dropdown menu for 'Settings'
  -  Merge pull request #1 from NetHome/master
  -  Added customizable menu file (a parameter in HomeGUI.java)
  -  Added customHtmlFooter (HomeGUI.java) and keep navbar on scroll (custom.js)
  -  Fixed navbar scroll.
  -  Added support with .svg with hyperlinks.
  -  Set the size #svgDiv according to the size of #svgObject.
  -  Fixed itemcolumn margins
  -  Applied RWD (responsive web design).
  -  Create/Edit move to as a sumbenu of Settings
  -  Converted custom html header and footer into 'Text' attribute
  -  Added html id's to all the navbar menu items
  -  Responsive navbar for screen widths lt 650px
  -  RoomsPage now uses ajax (similarly as PlanPage)
  -  Fixed bug when generating html onclick (PortletPage.java)
  -  Set up getItemValues every 2s in rooms.js
  -  Unified portlet display in PlanPage, RoomsPage and SeverFloor.
  -  IkeaLamp now reports brightness as a part of its state (e.g. 'On 60%')
  -  Updated PlanPage to show IkeaLamp 'On level%'
  -  Added callItemAction on icons in PortletPage
  -  Fixed pom.xml (maven warnings about version nrs)
  -  Added onclick for Plan, Infrasturcture and Graph icons in PortletPage
  -  Default 'click to view graph' for hasLog items in PlanPlage
  -  View graph in PlanPage only unless isPopupOnClick
  -  IkeaLamp set refreshInterval=0 to mean 'disabled'
  -  Fixed html header in TempWEB
  -  RWD CSS adjustments
  -  Renamed 'Rooms' into 'Home' in RoomsPage
  -  Replaced `<ul>` with `<div>`s
  -  Floating homeitem icon and itemvalue in PortletPage
  -  Use localURL in PortletPage and CSS homeitem height
  -  Set config attach=false for maven-assembly-plugin in pom.xml
  -  Fixed with for .logrows.coders tr td
  -  printCustomMenuFile unless isEditMode (HomeGUI)
  -  Enabled progressive web application (PWA).
  -  Instructions how to generate ssl certificates
  -  Fixed HTMLEncode.encode for several 'Name' and NAME\_ATTRs
  -  Sample ssl config in etc-nginx
  -  CSS navbar wide 100% with adjusted padding
  -  Added RootRedirect attribute and RedirectServlet in JetttyWEB
  -  CSS removed floats for itemcolumns (now set to inline-blocks)
  -  Added robots.txt and root dir handler
  -  Added img alt attributes
  -  Added defer to the `<script>`s printed in HomeGUI
  -  Update of SVG itemvalue class elements with data-value (portlet.js)
  -  Instead of loading as `<object>`, rather include SVG as an html element
  -  `<script defer>` in HomeGUI and GraphPage
  -  SVG retrieved locally from MediaDirectory (PlanPage)
  -  Updated init.d and `rpi_*.sh`
  -  RWD added hrefs to those top menus that have submenus
  -  CSS/JS adjusts after Lighthouse
  -  Added meta viewport to TempWEB and /temp centered screen popup 
  -  Added EmbedSVG attribute in Plan.java (used in PlanPage to control including of SVG as opposed to referal as `<object>`
  -  TempWEB added HTMLEncode of item names + fixed CSS popup sizes
  -  Fixed loading CSS fa fonts for firefox
  -  Fixed constant CPU usage by gnu.io.RXTXPort.eventLoop.

Development Environment
-----------------------

Dependencies: Java SE SDK 1.6, Maven 2.

The project is built using Maven, which makes it quite easy to load it into any
standard development IDE such as Eclipse or IntelliJ. Since the project uses
the rxtx serial library which is platform
dependant, you have to run a platform setup script to configure the build
environment for your development platform before you can build it.
These scripts are located under server-install/src/main/scripts. So to configure the
environment for a 64 bits Windows 7 platform you would have to:

    cd server-install\src\main\scripts
    setup_win64.bat

On Linux and MAC you also have to make the script executable before running it.
For example:

    cd server-install/src/main/scripts
    chmod +x setup_macosx_cocoa64.sh
    ./setup_macosx_cocoa64.sh

This will copy the correct version of the rxtx runtime libraries to the
root of the project so you can run the application from your IDE.

Also, the NetHome Server depends on the projects NetHome/Utils, NetHome/Coders,
Nethome/ZWave and Nethome/cybergarage-upnp.
These projects need to be cloned locally and built and installed in your local 
Maven repository:

    cd <location-of NetHome/Utils>
    mvn install
    cd <location-of NetHome/Coders>
    mvn install
    cd <location-of NetHome/ZWave>
    mvn install
    cd <location-of NetHome/Coders>
    mvn install
    
Overview
--------
The project consists of a number of separate maven modules.

* external - here 3:rd party software which is built from source is located
* home-items - this module contains all NetHome Server Home Items
* server - the NetHomeServer core
* server-install - this module build the installation package

How to Build
------------

To just build the jar file of the project, you issue:

    mvn package

from the root of the project.
To build the entire deployment package you issue:

    mvn install

The installable result will be located under:
server-install/target

Open from IntelliJ
------------------

IntelliJ can read Maven project files, so the project can be opened directly.
All you have to do is to create a run-configuration. Select:

run->edit configurations

and create a new application. Select the main class as:

```nu.nethome.home.start.StaticHomeManagerStarter``` (in server-install modules)

And the configuration should be able to build and run the application.

Open from Eclipse
-----------------

Maven can create an eclipse project by issuing the command:

    mvn eclipse:eclipse

After that you can import the project into your workspace. You will have to
create a variable called M2_REPO which points to your local maven repository,
which on windows is located under your personal folder in a folder called

.m2\repository

For example: ```C:\Users\Stefan\.m2\repository```

