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
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

/**
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
            + "  <Attribute Name=\"EnergyToday\" Type=\"String\" Get=\"getEnergyToday\" />"
            + "  <Attribute Name=\"TotalEnergy\" Type=\"String\" Get=\"getTotalEnergy\" />"
            + "  <Attribute Name=\"TimeSinceUpdate\" 	Type=\"String\" Get=\"getTimeSinceUpdate\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"PulsesPerKwh\" Type=\"String\" Get=\"getEnergyK\" 	Set=\"setEnergyK\" />"
            + "  <Attribute Name=\"LostSamples\" Type=\"String\" Get=\"getLostSamples\" Init=\"setLostSamples\" />"
            + "  <Attribute Name=\"TotalSavedPulses\" Type=\"String\" Get=\"getTotalSavedPulses\" Set=\"setTotalSavedPulses\" />"
            + "</HomeItem> ");

    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_MONTH = 24 * 31;
    public static final int MONTH_BUFFER_SIZE = HOURS_PER_MONTH + 1;
    public static final int HOUR_BUFFER_SIZE = MINUTES_PER_HOUR + 1;
    public static final int HOURS_PER_WEEK = 24 * 7;
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss yyyy.MM.dd ");

    public static final String ENERGY_FORMAT = "%.3f";
    protected Logger logger = Logger.getLogger(FooGadgetEnergy.class.getName());
    private LoggerComponent energyLoggerComponent = new LoggerComponent(this);
    private Date latestUpdateOrCreation = getCurrentTime();
    private Date latestValueSampleTime = getCurrentTime();
    private long latestValueSamplePulses;

    private long totalSavedPulses = 0;
    private long latestPulseSample = 0;

    // Public attributes
    private double pulsesPerKWh = 1000.0;
    private int latestSampleCounter;
    private boolean hasBeenUpdated;
    private long lostSamples;
    private long totalPulsesAtStartOfDay;
    private int currentDay;

    public FooGadgetEnergy() {
    }

    @Override
    public void activate() {
        energyLoggerComponent.activate();
    }

    @Override
    public void stop() {
        energyLoggerComponent.stop();
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (isFooGadgetEnergyEvent(event)) {
            return handleFooGadgetEvent(event);
        } else if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(HomeService.MINUTE_EVENT_TYPE)) {
            return handleMinuteEvent();
        }
        return false;
    }

    private boolean isFooGadgetEnergyEvent(Event event) {
        return event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("FooGadgetEnergy_Message");
    }

    protected boolean handleFooGadgetEvent(Event event) {
        latestUpdateOrCreation = getCurrentTime();
        int counter = getSampleCounter(event);
        if (counter != latestSampleCounter) {
            addNewSample(event, counter);
        }
        if (!hasBeenUpdated) {
            InitializeAtFirstUpdate();
        }
        hasBeenUpdated = true;
        return true;
    }

    private boolean handleMinuteEvent() {
        int dayNow = getPresentDay();
        if (dayNow != currentDay) {
            totalPulsesAtStartOfDay = getTotalPulses();
            currentDay = dayNow;
        }
        return true;
    }

    private int getPresentDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getCurrentTime());
        return calendar.get(Calendar.DAY_OF_YEAR);
    }

    private void InitializeAtFirstUpdate() {
        latestValueSamplePulses = getTotalPulses();
        latestValueSampleTime = getCurrentTime();
        if (totalPulsesAtStartOfDay == 0) {
            totalPulsesAtStartOfDay = getTotalPulses();
        }
        if (currentDay == 0) {
            currentDay = getPresentDay();
        }
    }

    Date getCurrentTime() {
        return new Date();
    }

    private void addNewSample(Event event, int counter) {
        if (hasBeenUpdated) {
            int sampleCounterDiff = counter - latestSampleCounter;
            sampleCounterDiff += (sampleCounterDiff < 0) ? 100 : 0;
            lostSamples += sampleCounterDiff - 1;
        }
        latestSampleCounter = counter;
        latestPulseSample = getPulseSample(event);
        totalSavedPulses += latestPulseSample;
    }

    private int getSampleCounter(Event event) {
        return event.getAttributeInt("FooGadgetEnergy.Counter");
    }

    private long getPulseSample(Event event) {
        return event.getAttributeInt("FooGadgetEnergy.Energy");
    }

    public String getModel() {
        return MODEL;
    }

    public String getPower() {
        if (!hasBeenUpdated) {
            return "";
        }
        return String.format(ENERGY_FORMAT, (latestPulseSample / pulsesPerKWh) * MINUTES_PER_HOUR);
    }

    public String getEnergyK() {
        return Double.toString(pulsesPerKWh);
    }

    public void setEnergyK(String EnergyK) {
        this.pulsesPerKWh = Double.parseDouble(EnergyK);
    }

    public String getTotalEnergy() {
        return String.format(ENERGY_FORMAT, totalSavedPulses / pulsesPerKWh);
    }

    protected long getTotalPulses() {
        return totalSavedPulses;
    }

    public String getTotalSavedPulses() {
        return Long.toString(totalSavedPulses);
    }

    public void setTotalSavedPulses(String totalSavedPulses) {
        this.totalSavedPulses = Long.parseLong(totalSavedPulses);
    }

    public String getLastUpdate() {
        return hasBeenUpdated ? dateFormatter.format(latestUpdateOrCreation) : "";
    }

    public String getLogFile() {
        return energyLoggerComponent.getFileName();
    }

    public void setLogFile(String logfile) {
        energyLoggerComponent.setFileName(logfile);
    }

    public String getTimeSinceUpdate() {
        return Long.toString((getCurrentTime().getTime() - latestUpdateOrCreation.getTime()) / 1000);
    }

    @Override
    public String getValue() {
        if (!hasBeenUpdated) {
            return "";
        }
        Date sampleTime = getCurrentTime();
        long currentEnergy = getTotalPulses();
        double timeSinceLastSampleHours = (sampleTime.getTime() - latestValueSampleTime.getTime()) / (1000.0 * 60.0 * 60.0);
        double energySinceLastSampleKWh = (currentEnergy - latestValueSamplePulses) / pulsesPerKWh;
        latestValueSampleTime = sampleTime;
        latestValueSamplePulses = currentEnergy;
        return String.format(ENERGY_FORMAT, energySinceLastSampleKWh / timeSinceLastSampleHours);
    }

    public String getLostSamples() {
        return Long.toString(lostSamples);
    }

    public void setLostSamples(String lostSamples) {
        this.lostSamples = Long.parseLong(lostSamples);
    }

    public String getEnergyToday() {
        if (!hasBeenUpdated) {
            return "";
        }
        long pulsesToday = getTotalPulses() - totalPulsesAtStartOfDay;
        return String.format(ENERGY_FORMAT, pulsesToday / pulsesPerKWh);
    }
}
