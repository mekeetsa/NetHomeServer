/**
 * Copyright (C) 2005-2016, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.items.net;

import java.sql.SQLException;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.h2.tools.Server;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

/**
 * HomeItem class which starts the H2 database server.
 * 
 * @author Peter Lagerhem 2016-01-03
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Ports")
public class H2DatabaseTCPServer extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"H2DatabaseTCPServer\" Category=\"Ports\" >"
            + "  <Attribute Name=\"DatabasePath\" Type=\"String\" Get=\"getDatabasePath\" Set=\"setDatabasePath\" />"
            + "  <Attribute Name=\"TCP active on startup\" Type=\"Boolean\" Get=\"getTcpActiveOnStartup\" Set=\"setTcpActiveOnStartup\" />"
            + "  <Attribute Name=\"TCP ConnectionString\" Type=\"String\" Get=\"getTCPConnectionString\" />"
            + "  <Attribute Name=\"TCP Port\" Type=\"String\" Get=\"getTcpPort\" Set=\"setTcpPort\" Default=\"9092\" />"
            + "  <Attribute Name=\"TCP Status\" Type=\"String\" Get=\"getTcpActivated\" />"
            + "  <Attribute Name=\"WEB ConnectionString\" Type=\"String\" Get=\"getWEBConnectionString\" />"
            + "  <Attribute Name=\"WEB active on startup\" Type=\"Boolean\" Get=\"getWebActiveOnStartup\" Set=\"setWebActiveOnStartup\" />"
            + "  <Attribute Name=\"WEB Port\" Type=\"String\" Get=\"getWebPort\" Set=\"setWebPort\" Default=\"8082\" />"
            + "  <Attribute Name=\"WEB public\" Type=\"Boolean\" Get=\"getWebIsPublic\" Set=\"setWebIsPublic\" />"
            + "  <Attribute Name=\"WEB Status\" Type=\"String\" Get=\"getWebActivated\" />"
            + "  <Action Name=\"Start TCP Service\"     Method=\"activateTcpServer\" />"
            + "  <Action Name=\"Stop TCP Service\"      Method=\"deactivateTcpServer\" />"
            + "  <Action Name=\"Start WEB Service\"     Method=\"activateWebServer\" />"
            + "  <Action Name=\"Stop WEB Service\"      Method=\"deactivateWebServer\" />" 
            + "</HomeItem> ");

    /*
     * Internal attributes
     */

    // Console
    private String tcpPort = "9092";
    private Server tcpServer = null;
    private String tcpConnectionString = "";
    private boolean all = true;;
    private boolean force = true;
    private boolean tcpActiveOnStartup = true;

    // Web
    private String webPort = "8082";
    private Server webServer = null;
    private String webConnectionString = "";
    private boolean webIsPublic = false;
    private boolean webActiveOnStartup = true;

    // Default is the user's home directory, please see reference:
    // http://www.h2database.com/html/advanced.html?highlight=baseDir&search=basedir#firstFound
    private String databasePath = "~";

    private static Logger logger = Logger.getLogger(H2DatabaseTCPServer.class.getName());

    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    protected boolean isActivated() {
        return isTcpActivated() || isWebActivated();
    }

    @Override
    public void activate(HomeService service) {
        if (tcpActiveOnStartup) {
            activateTcpServer();
        }
        if (webActiveOnStartup) {
            activateWebServer();
        }
        super.activate(service);
    }

    @Override
    public void stop() {
        deactivateTcpServer();
        deactivateWebServer();
        super.stop();
    }

    /*
     * Internal implementation methods
     */

    public void deactivateTcpServer() {
        // Close H2 console server
        try {
            // We don't use password (param #2) for shutting down the server
            Server.shutdownTcpServer(tcpConnectionString, "", force, all);
        } catch (SQLException e) {
            logger.info("Can't shutdown the H2 database server: " + e.getMessage());
        }
    }

    public void deactivateWebServer() {
        // Close H2 web server
        webServer.stop();
    }

    private boolean isTcpActivated() {
        return (tcpServer != null && tcpServer.isRunning(true));
    }

    private boolean isWebActivated() {
        return (webServer != null && webServer.isRunning(true));
    }

    public String getTcpActivated() {
        return isTcpActivated() ? "Running, " + allowOthers(tcpServer) : "Not running - check logs";
    }

    public String getWebActivated() {
        return isWebActivated() ? "Running, " + allowOthers(webServer) : "Not running - check logs";
    }

    private String allowOthers(Server server) {
        if (server.getService().getAllowOthers()) {
            return "others can connect";
        }
        return "only local connections";
    }

    public String getTCPConnectionString() {
        return "jdbc:h2:" + tcpConnectionString + "/[data file name]";
    }

    public String getWEBConnectionString() {
        return webConnectionString;
    }

    public String getWebIsPublic() {
        return String.valueOf(webIsPublic);
    }

    public void setWebIsPublic(String isPublic) {
        webIsPublic = Boolean.parseBoolean(isPublic);
    }

    public String getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(String tcpPort) {
        this.tcpPort = tcpPort;
    }

    public String getWebPort() {
        return webPort;
    }

    public void setWebPort(String webPort) {
        this.webPort = webPort;
    }

    public void activateTcpServer() {
        if (isTcpActivated()) {
            return;
        }

        // Start the H2 database server
        try {
            if (StringUtils.isBlank(tcpPort)) {
                tcpServer = Server.createTcpServer("-tcpAllowOthers", "-baseDir", getDatabasePath());
            } else {
                tcpServer = Server.createTcpServer("-tcpPort", tcpPort, "-tcpAllowOthers", "-baseDir",
                        getDatabasePath());
            }
            tcpServer.start();
            tcpConnectionString = tcpServer.getURL();
            tcpPort = String.valueOf(tcpServer.getPort());
            logger.info("Started the H2 database server at: " + tcpConnectionString);
        } catch (SQLException e) {
            logger.info("Can't start the H2 database server: " + e.getMessage());
        }
    }

    public void activateWebServer() {
        if (isWebActivated()) {
            return;
        }

        // Start the H2 web server
        try {
            if (StringUtils.isBlank(webPort)) {
                webServer = Server.createWebServer("-baseDir", getDatabasePath());
                // , "-webSSL", useWebSSL -webAllowOthers, -webDaemon, -trace,
                // -ifExists, -baseDir, -properties)
            } else {
                webServer = Server.createWebServer("-webPort", webPort, "-ifExists", "-baseDir", getDatabasePath());
            }
            if (webIsPublic) {
                webServer.getService().init("-webAllowOthers");
            }
            webServer.start();
            webConnectionString = webServer.getURL();
            webPort = String.valueOf(webServer.getPort());
            logger.info("Started the H2 web server at: " + tcpConnectionString);
        } catch (SQLException e) {
            logger.info("Can't start the H2 web server: " + e.getMessage());
        } catch (Exception e) {
            logger.info("Can't start the H2 web server: " + e.getMessage());
        }
    }

    public String getDatabasePath() {
        // service.getConfiguration().getLogDirectory()
        return databasePath;
    }

    public void setDatabasePath(String path) {
        // service.getConfiguration().getLogDirectory()
        databasePath = path;
    }

    public String getTcpActiveOnStartup() {
        return String.valueOf(tcpActiveOnStartup);
    }

    public void setTcpActiveOnStartup(String activate) {
        tcpActiveOnStartup = Boolean.parseBoolean(activate);
    }

    public String getWebActiveOnStartup() {
        return String.valueOf(webActiveOnStartup);
    }

    public void setWebActiveOnStartup(String activate) {
        webActiveOnStartup = Boolean.parseBoolean(activate);
    }

}
