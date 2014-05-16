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

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.Calendar;
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
@HomeItemType(value = "Gauges", creationEvents = "FineOffset_Message")
public class FooGadgetEnergy extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"FooGadgetEnergy\" Category=\"Gauges\" >"
            + "  <Attribute Name=\"Energy1h\" Type=\"String\" Get=\"getEnergy1h\" Default=\"true\" />"
            + "  <Attribute Name=\"Energy24h\" Type=\"String\" Get=\"getEnergy24h\" />"
            + "  <Attribute Name=\"EnergyThisWeek\" Type=\"String\" Get=\"getEnergyThisWeek\" />"
            + "  <Attribute Name=\"EnergyLastWeek\" Type=\"String\" Get=\"getEnergyLastWeek\" Init=\"setEnergyLastWeek\" />"
            + "  <Attribute Name=\"TotalEnergy\" Type=\"String\" Get=\"getTotalEnergy\" />"
            + "  <Attribute Name=\"TimeSinceUpdate\" 	Type=\"String\" Get=\"getTimeSinceUpdate\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\" />"
            + "  <Attribute Name=\"EnergyK\" Type=\"String\" Get=\"getK\" 	Set=\"setK\" />"
            + "  <Attribute Name=\"TotalEnergyBase\" Type=\"Hidden\" Get=\"getTotalEnergyBase\" Init=\"setTotalEnergyBase\" />"
            + "  <Attribute Name=\"EnergyAtStartOfWeek\" Type=\"Hidden\" Get=\"getEnergyAtStartOfWeek\" Init=\"setEnergyAtStartOfWeek\" />"
            + "  <Attribute Name=\"CurrentWeekNumber\" Type=\"Hidden\" Get=\"getCurrentWeekNumber\" Init=\"setCurrentWeekNumber\" />"
            + "  <Attribute Name=\"MinutesOfLastHour\" Type=\"String\" Get=\"getMinutesOfLastHour\" Init=\"setMinutesOfLastHour\" />"
            + "  <Attribute Name=\"Last24Hours\" Type=\"String\" Get=\"getLast24Hours\" Init=\"setLast24Hours\" />"
            + "</HomeItem> ");

    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_MONTH = 24 * 31;
    public static final int MONTH_BUFFER_SIZE = HOURS_PER_MONTH + 1;
    public static final int HOUR_BUFFER_SIZE = MINUTES_PER_HOUR + 1;
    public static final int HOURS_PER_WEEK = 24 * 7;

    protected Logger logger = Logger.getLogger(FooGadgetEnergy.class.getName());
    private CounterHistory minutesOfLastHour = new CounterHistory(MINUTES_PER_HOUR);
    private CounterHistory last24Hours = new CounterHistory(24);
    private int currentHour = 0;
    private int minuteCounter = MINUTES_PER_HOUR - 1;
    private int hourCounter = 0;
    private int dayCounter = 0;
    private long totalHours = 0;
    private long TotalEnergyAtLastValue = 0;
    private long totalEnergyBase = 0;

    // Public attributes
    private double energyConstantK = 0.1;
    private long energyAtStartOfWeek;
    private int currentWeekNumber = -1;
    private String EnergyLastWeek = "";
    private int lastCounter;
    private boolean hasBeenUpdated;

    public FooGadgetEnergy() {

    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("FineOffset_Message") &&
                (event.getAttribute("FineOffset.Identity").equals(1092) || event.getAttribute("FineOffset.Identity").equals(1091))) {
            return handleEvent(event);
        } else if (hasBeenUpdated && event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(HomeService.MINUTE_EVENT_TYPE)) {
            pushValue();
            return true;
        } else {
            return handleInit(event);
        }
    }

    private void pushValue() {
        minutesOfLastHour.addValue(getTotalEnergyInternal());
        if (++minuteCounter == MINUTES_PER_HOUR) {
            minuteCounter = 0;
            last24Hours.addValue(getTotalEnergyInternal());
        }
    }

    private int calculateCurrentWeekNumber() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    protected boolean handleEvent(Event event) {
        int counter = event.getAttributeInt("FineOffset.Moisture");
        if (counter != lastCounter) {
            lastCounter = counter;
            totalEnergyBase += getEnergyCounter(event);
            hasBeenUpdated = true;
            updateRainOfWeek();
        }
        return true;
    }

    private long getEnergyCounter(Event event) {
        int counter = event.getAttributeInt("FineOffset.Temp");
        if (counter < 0) {
            counter = -counter + 1 << 11;
        }
        return counter;
    }

    private void updateRainOfWeek() {
        if (currentWeekNumber != calculateCurrentWeekNumber()) {
            currentWeekNumber = calculateCurrentWeekNumber();
            EnergyLastWeek = getEnergyThisWeek();
            energyAtStartOfWeek = getTotalEnergyInternal();
        }
    }

    public String getModel() {
        return MODEL;
    }

    public String getEnergy1h() {
        if (!hasBeenUpdated) {
            return "";
        }
        double Energy1h = minutesOfLastHour.differenceSince(getTotalEnergyInternal(), MINUTES_PER_HOUR) * energyConstantK;
        return String.format("%.3f", Energy1h);
    }

    public String getEnergy24h() {
        if (!hasBeenUpdated) {
            return "";
        }
        double Energy1h = last24Hours.differenceSince(getTotalEnergyInternal(), 24) / energyConstantK;
        return String.format("%.3f", Energy1h);
    }

    public String getEnergyThisWeek() {
        if (!hasBeenUpdated) {
            return "";
        }
        double Energy1h = (getTotalEnergyInternal() - energyAtStartOfWeek) / energyConstantK;
        return String.format("%.3f", Energy1h);
    }

    public String getEnergyLastWeek() {
        return EnergyLastWeek;
    }

    public void setEnergyLastWeek(String EnergyLastWeek) {
        this.EnergyLastWeek = EnergyLastWeek;
    }

    public String getValue() {
        if (!hasBeenUpdated) {
            return "";
        }
        long currentTotalEnergy = getTotalEnergyInternal();
        if (TotalEnergyAtLastValue == 0) {
            TotalEnergyAtLastValue = currentTotalEnergy;
        }
        double Energy1h = (getTotalEnergyInternal() - TotalEnergyAtLastValue) / energyConstantK;
        TotalEnergyAtLastValue = currentTotalEnergy;
        return String.format("%.3f", Energy1h);
    }

    public String getTotalEnergy() {
        return hasBeenUpdated ? String.format("%.1f", getTotalEnergyInternal() / energyConstantK) : "";
    }

    public String getEnergyK() {
        return Double.toString(energyConstantK);
    }

    public void setEnergyK(String EnergyK) {
        this.energyConstantK = Double.parseDouble(EnergyK);
    }

    public String getTotalEnergyBase() {
        return String.format("%.3f", totalEnergyBase / energyConstantK);
    }

    public void setTotalEnergyBase(String rainBase) {
        this.totalEnergyBase = (long) (Double.parseDouble(rainBase.replace(",", ".")) / energyConstantK);
    }

    protected long getTotalEnergyInternal() {
        return totalEnergyBase;
    }

    public String getEnergyAtStartOfWeek() {
        return Long.toString(energyAtStartOfWeek);
    }

    public void setEnergyAtStartOfWeek(String energyAtStartOfWeek) {
        this.energyAtStartOfWeek = Long.parseLong(energyAtStartOfWeek);
    }

    public String getCurrentWeekNumber() {
        return Integer.toString(currentWeekNumber);
    }

    public void setCurrentWeekNumber(String currentWeekNumber) {
        this.currentWeekNumber = Integer.parseInt(currentWeekNumber);
    }

    public String getMinutesOfLastHour() {
        return minutesOfLastHour.getHistoryAsString();
    }

    public void setMinutesOfLastHour(String minutesOfLastHour) {
        this.minutesOfLastHour.setHistoryFromString(minutesOfLastHour);
    }

    public String getLast24Hours() {
        return last24Hours.getHistoryAsString();
    }

    public void setLast24Hours(String last24Hours) {
        this.last24Hours.setHistoryFromString(last24Hours);
    }
}
