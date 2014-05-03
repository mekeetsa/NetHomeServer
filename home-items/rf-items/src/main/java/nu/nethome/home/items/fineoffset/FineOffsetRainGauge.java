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
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Presents and logs temperature values received by an FineOffset-temperature sensor. The actual
 * values are received as events which may be sent by any kind of receiver module
 * which can receive FineOffset messages from the hardware devices.
 *
 * @author Stefan
 */
@Plugin
@HomeItemType(value = "Gauges", creationEvents = "FineOffset_Message")
public class FineOffsetRainGauge extends FineOffsetThermometer implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"FineOffsetRainGauge\" Category=\"Gauges\" >"
            + "  <Attribute Name=\"Rain1h\" Type=\"String\" Get=\"getRain1h\" Default=\"true\" />"
            + "  <Attribute Name=\"Rain24h\" Type=\"String\" Get=\"getRain24h\" />"
            + "  <Attribute Name=\"RainWeek\" Type=\"String\" Get=\"getRainWeek\" />"
            + "  <Attribute Name=\"RainMonth\" Type=\"String\" Get=\"getRainMonth\" />"
            + "  <Attribute Name=\"TotalRain\" Type=\"String\" Get=\"getTotalRain\" />"
            + "  <Attribute Name=\"Temperature\" 	Type=\"String\" Get=\"getTemp\" />"
            + "  <Attribute Name=\"TimeSinceUpdate\" 	Type=\"String\" Get=\"getTimeSinceUpdate\" />"
            + "  <Attribute Name=\"DeviceId\" Type=\"String\" Get=\"getDeviceId\" 	Set=\"setDeviceId\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\" />"
            + "  <Attribute Name=\"RainK\" Type=\"String\" Get=\"getK\" 	Set=\"setK\" />"
            + "  <Attribute Name=\"TempK\" Type=\"String\" Get=\"getK\" 	Set=\"setK\" />"
            + "  <Attribute Name=\"TempM\" Type=\"String\" Get=\"getM\" 	Set=\"setM\" />"
            + "</HomeItem> ");
    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_MONTH = 24 * 31;
    public static final int MONTH_BUFFER_SIZE = HOURS_PER_MONTH + 1;
    public static final int HOUR_BUFFER_SIZE = MINUTES_PER_HOUR + 1;
    public static final int HOURS_PER_WEEK = 24 * 7;

    private long lastHour[] = new long[HOUR_BUFFER_SIZE];
    private int currentMinute = 0;
    private long totalMinutes = 0;
    private long lastMonth[] = new long[MONTH_BUFFER_SIZE];
    private int currentHour = 0;
    private int minuteCounter = 0;
    private long totalHours = 0;
    private long totalRainAtLastValue = 0;

    // Public attributes
    private long totalRain = 0;
    private double rainConstantK = 0.1;

    public FineOffsetRainGauge() {
        constantK = 0.1;
        logger = Logger.getLogger(FineOffsetRainGauge.class.getName());
    }

    @Override
    public boolean receiveEvent(Event event) {
        boolean result = super.receiveEvent(event);
        if (hasBeenUpdated && event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(HomeService.MINUTE_EVENT_TYPE)) {
            pushValue();
        }
        return result;
    }

    private void pushValue() {
        lastHour[currentMinute] = totalRain;
        currentMinute = (currentMinute + 1) % HOUR_BUFFER_SIZE;
        totalMinutes++;
        if (++minuteCounter == MINUTES_PER_HOUR) {
            minuteCounter = 0;
            lastMonth[currentHour] = totalRain;
            currentHour = (currentHour + 1) % MONTH_BUFFER_SIZE;
            totalHours++;
        }
    }

    @Override
    protected boolean handleEvent(Event event) {
        long oldTotalRain = totalRain;
        totalRain = event.getAttributeInt("FineOffset.Rain");
        if (oldTotalRain > totalRain) {
            resetStatistics();
        }
        return super.handleEvent(event);
    }

    private void resetStatistics() {
        currentMinute = 0;
        totalMinutes = 0;
        currentHour = 0;
        minuteCounter = 0;
        totalHours = 0;
    }

    public String getModel() {
        return MODEL;
    }

    public String getRain1h() {
        if (!hasBeenUpdated) {
            return "";
        }
        long rainTocompareWith;
        if (totalMinutes == 0) {
            rainTocompareWith = totalRain;
        } else if (totalMinutes <= MINUTES_PER_HOUR) {
            rainTocompareWith = lastHour[0];
        } else {
            rainTocompareWith = getValue1HourAgo();
        }
        rainTocompareWith = (rainTocompareWith == 0) ? totalRain : rainTocompareWith;
        double rain1h = (totalRain - rainTocompareWith) * rainConstantK;
        return String.format("%.1f", rain1h);
    }

    public String getRain24h() {
        return getRainLastHours(24);
    }

    public String getRainWeek() {
        return getRainLastHours(HOURS_PER_WEEK);
    }

    public String getRainMonth() {
        return getRainLastHours(HOURS_PER_MONTH);
    }

    private String getRainLastHours(int hours) {
        if (!hasBeenUpdated) {
            return "";
        }
        long rainTocompareWith;
        if (totalHours == 0) {
            return getRain1h();
        } else if (totalHours <= hours) {
            rainTocompareWith = lastMonth[0];
        } else {
            rainTocompareWith = getValueHoursAgo(hours);
        }
        rainTocompareWith = (rainTocompareWith == 0) ? totalRain : rainTocompareWith;
        double rain1h = (totalRain - rainTocompareWith) * rainConstantK;
        return String.format("%.1f", rain1h);
    }

    private long getValue1HourAgo() {
        return lastHour[(currentMinute + HOUR_BUFFER_SIZE - MINUTES_PER_HOUR) % HOUR_BUFFER_SIZE];
    }

    private long getValueHoursAgo(int hours) {
        return lastMonth[(currentHour + MONTH_BUFFER_SIZE - hours) % HOUR_BUFFER_SIZE];
    }

    public String getValue() {
        if (!hasBeenUpdated) {
            return "";
        }
        if (totalRainAtLastValue == 0) {
            totalRainAtLastValue = totalRain;
        }
        double rain1h = (totalRain - totalRainAtLastValue) * rainConstantK;
        return String.format("%.1f", rain1h);
    }

    public String getTotalRain() {
        return hasBeenUpdated ? String.format("%.1f", totalRain * rainConstantK) : "";
    }

    public String getRainK() {
        return Double.toString(rainConstantK);
    }

    public void setRainK(String rainK) {
        this.rainConstantK = Double.parseDouble(rainK);
    }
}
