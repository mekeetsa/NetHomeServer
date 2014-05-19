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

package nu.nethome.home.items.fineoffset;

import nu.nethome.home.item.*;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Presents and logs rain values received by an FineOffset-rain sensor. The actual
 * values are received as events which may be sent by any kind of receiver module
 * which can receive FineOffset messages from the hardware devices.
 *
 * @author Stefan
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Gauges", creationEvents = "FooGadgetEnergy_Message")
public class FooGadgetEnergy extends HomeItemAdapter implements HomeItem, ValueItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"FooGadgetEnergy\" Category=\"Gauges\" >"
            + "  <Attribute Name=\"Power\" Type=\"String\" Get=\"getPower\" Default=\"true\" />"
            + "  <Attribute Name=\"TotalEnergy\" Type=\"String\" Get=\"getTotalEnergy\" Init=\"setTotalEnergy\" />"
            + "  <Attribute Name=\"TimeSinceUpdate\" 	Type=\"String\" Get=\"getTimeSinceUpdate\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"PulsesPerKwh\" Type=\"String\" Get=\"getEnergyK\" 	Set=\"setEnergyK\" />"
            + "  <Attribute Name=\"LostSamples\" Type=\"String\" Get=\"getLostSamples\" Init=\"setLostSamples\" />"
            + "</HomeItem> ");

    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_MONTH = 24 * 31;
    public static final int MONTH_BUFFER_SIZE = HOURS_PER_MONTH + 1;
    public static final int HOUR_BUFFER_SIZE = MINUTES_PER_HOUR + 1;
    public static final int HOURS_PER_WEEK = 24 * 7;
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss yyyy.MM.dd ");

    public static final String ENERGY_FORMAT = "%.3f";
    protected Logger logger = Logger.getLogger(FooGadgetEnergy.class.getName());
    private LoggerComponent tempLoggerComponent = new LoggerComponent(this);
    private Date latestUpdateOrCreation = new Date();
    private Date latestValueSampleTime = new Date();
    private long latestValueSampleEnergy;

    private long TotalEnergyAtLastValue = 0;
    private long totalEnergy = 0;
    private long currentEnergy = 0;

    // Public attributes
    private double pulsesPerKWh = 1000;
    private int lastCounter;
    private boolean hasBeenUpdated;
    private long lostSamples;

    public FooGadgetEnergy() {
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (isFooGadgetEnergyEvent(event)) {
            return handleEvent(event);
        } else {
            return handleInit(event);
        }
    }

    private boolean isFooGadgetEnergyEvent(Event event) {
        return event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("FooGadgetEnergy_Message");
    }

    protected boolean handleEvent(Event event) {
        latestUpdateOrCreation = new Date();
        int counter = getSampleCounter(event);
        if (counter != lastCounter) {
            addNewSample(event, counter);
        }
        if (!hasBeenUpdated) {
            latestValueSampleEnergy = totalEnergy;
            latestValueSampleTime = new Date();
        }
        hasBeenUpdated = true;
        return true;
    }

    private void addNewSample(Event event, int counter) {
        if (hasBeenUpdated) {
            int sampleCounterDiff = counter - lastCounter;
            sampleCounterDiff += (sampleCounterDiff < 0) ? 100 : 0;
            lostSamples += sampleCounterDiff - 1;
        }
        lastCounter = counter;
        currentEnergy = getEnergySample(event);
        totalEnergy += currentEnergy;
    }

    private int getSampleCounter(Event event) {
        return event.getAttributeInt("FooGadgetEnergy.Counter");
    }

    private long getEnergySample(Event event) {
        return event.getAttributeInt("FooGadgetEnergy.Energy");
    }

    public String getModel() {
        return MODEL;
    }

    public String getPower() {
        if (!hasBeenUpdated) {
            return "";
        }
        return String.format(ENERGY_FORMAT, (currentEnergy / pulsesPerKWh) * MINUTES_PER_HOUR);
    }

    public String getEnergyK() {
        return Double.toString(pulsesPerKWh);
    }

    public void setEnergyK(String EnergyK) {
        this.pulsesPerKWh = Double.parseDouble(EnergyK);
    }

    public String getTotalEnergy() {
        return String.format(ENERGY_FORMAT, totalEnergy / pulsesPerKWh);
    }

    public void setTotalEnergy(String rainBase) {
        this.totalEnergy = (long) (Double.parseDouble(rainBase.replace(",", ".")) * pulsesPerKWh);
    }

    protected long getTotalEnergyInternal() {
        return totalEnergy;
    }

    public String getLastUpdate() {
        return hasBeenUpdated ? dateFormatter.format(latestUpdateOrCreation) : "";
    }

    public String getLogFile() {
        return tempLoggerComponent.getFileName();
    }

    public void setLogFile(String logfile) {
        tempLoggerComponent.setFileName(logfile);
    }

    public String getTimeSinceUpdate() {
        return Long.toString((new Date().getTime() - latestUpdateOrCreation.getTime()) / 1000);
    }

    @Override
    public String getValue() {
        if (!hasBeenUpdated) {
            return "";
        }
        Date sampleTime = new Date();
        long currentEnergy = getTotalEnergyInternal();
        double timeSinceLastSampleHours = (sampleTime.getTime() - latestValueSampleTime.getTime()) / (1000 * 60 * 60);
        double energySinceLastSampleKWh = (currentEnergy - latestValueSampleEnergy) / pulsesPerKWh;
        latestValueSampleTime = sampleTime;
        latestValueSampleEnergy = currentEnergy;
        return String.format(ENERGY_FORMAT, energySinceLastSampleKWh / timeSinceLastSampleHours);
    }

    public String getLostSamples() {
        return Long.toString(lostSamples);
    }

    public void setLostSamples(String lostSamples) {
        this.lostSamples = Long.parseLong(lostSamples);
    }
}
