/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.nethome.home.item;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This factory class is responsible for creating and caching exactly one
 * instance of a particular logger component.
 * 
 * @author Peter Lagerhem
 */
public class LoggerComponentFactory {

	private static Logger logger = Logger.getLogger(LoggerComponent.class.getName());
	private static HashMap<String, LoggerComponentType> loggerComponentCache = new HashMap<String, LoggerComponentType>();

	public static synchronized LoggerComponentType createLoggerComponentType(String loggerComponentDescriptor) {

		String clean = loggerComponentDescriptor.trim().toLowerCase();

		if (clean.trim().startsWith(LoggerComponentH2Database.UNIQUE_IDENTIFIER)) {
			if (!loggerComponentCache.containsKey(LoggerComponentH2Database.UNIQUE_IDENTIFIER)) {
				loggerComponentCache.put(LoggerComponentH2Database.UNIQUE_IDENTIFIER, new LoggerComponentH2Database());
				logger.log(Level.INFO, "Enabled global logging of type: LoggerComponentH2Database with descriptor: "
						+ loggerComponentDescriptor);
			}
			return loggerComponentCache.get(LoggerComponentH2Database.UNIQUE_IDENTIFIER);
		}

		// Can only be file based left
		if (!loggerComponentCache.containsKey(LoggerComponentFileBased.UNIQUE_IDENTIFIER)) {
			loggerComponentCache.put(LoggerComponentFileBased.UNIQUE_IDENTIFIER, new LoggerComponentFileBased());
			logger.log(Level.INFO, "Enabled global logging of type: LoggerComponentFileBased with descriptor: "
					+ loggerComponentDescriptor);
		}
		return loggerComponentCache.get(LoggerComponentFileBased.UNIQUE_IDENTIFIER);
	}

}
