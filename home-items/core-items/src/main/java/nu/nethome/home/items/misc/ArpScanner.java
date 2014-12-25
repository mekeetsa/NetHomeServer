/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
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

package nu.nethome.home.items.misc;

import nu.nethome.home.item.*;
import nu.nethome.util.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Stefan
 */
@Plugin
@HomeItemType("Ports")
public class ArpScanner extends HomeItemAdapter implements HomeItem {

    private final String m_Model = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ArpScanner\"  Category=\"Ports\" >"
            + "  <Attribute Name=\"Temperature\" 	Type=\"String\" Get=\"getValue\" Default=\"true\"  Unit=\"°C\" />"
            + "  <Attribute Name=\"ArpScan\"	Type=\"String\" Get=\"getExecName\" 	Set=\"setExecName\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Action Name=\"Scan\" 	Method=\"scan\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(ArpScanner.class.getName());
    protected String execName = "/usr/bin/arp-scan -r 3 -b 2 -q --interface=eth0 --localnet";

    // Public attributes
    protected double m_Value = 0.0;
    protected String m_SensorName = "";

    public ArpScanner() {
    }

    /* (non-Javadoc)
     * @see ssg.home.HomeItem#getModel()
     */
    public String getModel() {
        return m_Model;
    }

    /**
     * @return Returns the m_Value.
     */
    public List<String> scan() {
        try {
            Runtime r = Runtime.getRuntime();
            Process proc = r.exec(execName);
            ResponseParser response = new ResponseParser(proc.getInputStream());
            response.start();
            int result = proc.waitFor();
            response.join(1000);
            return response.responseLines;

        } catch (IOException|InterruptedException e) {
            logger.warning("Failed to execute arp-scan: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * @return Returns the m_SensorName.
     */
    public String getSensorName() {
        return m_SensorName;
    }

    /**
     * @param SensorName The m_SensorName to set.
     */
    public void setSensorName(String SensorName) {
        m_SensorName = SensorName;
    }

    /**
     * @param execName the m_ExecName to set
     */
    public void setExecName(String execName) {
        this.execName = execName;
    }

    public String getExecName() {
        return execName;
    }

    static class ResponseParser extends Thread {
        private InputStream is;
        public List<String> responseLines = new ArrayList<>();

        ResponseParser(InputStream is) {
            super("arp-scan parser");
            this.is = is;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    Scanner lineScanner = new Scanner(line);
                    String value = lineScanner.findInLine("[0-9,a-f][0-9,a-f]:[0-9,a-f][0-9,a-f]:[0-9,a-f][0-9,a-f]:[0-9,a-f][0-9,a-f]:[0-9,a-f][0-9,a-f]:[0-9,a-f][0-9,a-f]");
                    if ((value != null) && (value.length() != 0)) {
                        responseLines.add(value);
                    }
                }
            } catch (IOException e) {
                logger.warning("Failed to execute arp-scan command: " + e.getMessage());
            }
        }
    }

}
