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
@Plugin
@HomeItemType("Database")
public class H2DatabaseTCPServer extends HomeItemAdapter implements HomeItem {

	private final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"H2DatabaseTCPServer\" Category=\"Database\" >"
			+ "  <Attribute Name=\"ConnectionString\" Type=\"String\" Get=\"getConnectionString\" />"
			+ "  <Attribute Name=\"TCPPort\" Type=\"String\" Get=\"getTcpPort\" Set=\"setTcpPort\" Default=\"9123\" />"
			+ "</HomeItem> ");

	/*
	 * Internal attributes
	 */
	private String tcpPort = null;
	private Server server = null;
	private String password = "";
	private boolean all = true;;
	private boolean force = false;
	private String connectionString = "";

	private static Logger logger = Logger.getLogger(H2DatabaseTCPServer.class.getName());

	@Override
	public String getModel() {
		return m_Model;
	}

	@Override
	protected boolean isActivated() {
		return (server != null && server.isRunning(true));
	}

	/*
	 * Internal implementation methods
	 */

	public String getConnectionString() {
		return connectionString;
	}

	public String getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(String tcpPort) {
		this.tcpPort = tcpPort;
	}

	public void activate(HomeService service) {
		// Start the H2 database server
		try {
			if (StringUtils.isBlank(tcpPort)) {
				server = Server.createTcpServer("-tcpAllowOthers", "-baseDir", service.getConfiguration().getLogDirectory());
			} else {				
				server = Server.createTcpServer("-tcpPort", tcpPort, "-tcpAllowOthers", "-baseDir", service.getConfiguration().getLogDirectory());
			}
			server.start();
			connectionString = server.getURL();
			tcpPort = String.valueOf(server.getPort());
			logger.info("Started the H2 database server at: " + connectionString);
		} catch (SQLException e) {
			logger.info("Can't start the H2 database server: " + e.getMessage());
		}
		super.activate(service);
	}

	public void stop() {
		// Close H2 database server
		try {
			Server.shutdownTcpServer(connectionString, password, force, all);
		} catch (SQLException e) {
			logger.info("Can't shutdown the H2 database server: " + e.getMessage());
		}
		super.stop();
	}

}
