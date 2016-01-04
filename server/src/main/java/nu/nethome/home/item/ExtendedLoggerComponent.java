package nu.nethome.home.item;

import nu.nethome.home.system.HomeService;

/**
 *
 */
public class ExtendedLoggerComponent extends LoggerComponent {

	public ExtendedLoggerComponent(Object o) {
		super((ValueItem) o);
		homeItemId = Long.toString( ((HomeItem)o).getItemId() );
	}

	public void activate(HomeService server) {
		String logPath = "";
		if (server != null) {
			logPath = server.getConfiguration().getLogDirectory();
		}
		super.activate(logPath);
		service = server;
		config = service.getConfiguration();
	}
}
