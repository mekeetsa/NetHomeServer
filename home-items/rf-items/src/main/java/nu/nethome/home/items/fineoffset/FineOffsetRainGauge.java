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
 * Presents and logs rain values received by an FineOffset-rain sensor. The actual
 * values are received as events which may be sent by any kind of receiver module
 * which can receive FineOffset messages from the hardware devices.
 *
 * @author Stefan
 */
@SuppressWarnings("UnusedDeclaration")
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
            + "  <Attribute Name=\"TotalRainBase\" Type=\"String\" Get=\"getTotalRainBase\" Set=\"setTotalRainBase\" />"
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
    private long totalRainBase = 0;
    private long sensorTotalRain = 0;

    // Public attributes
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
        lastHour[currentMinute] = getTotalRainInternal();
        currentMinute = (currentMinute + 1) % HOUR_BUFFER_SIZE;
        totalMinutes++;
        if (++minuteCounter == MINUTES_PER_HOUR) {
            minuteCounter = 0;
            lastMonth[currentHour] = getTotalRainInternal();
            currentHour = (currentHour + 1) % MONTH_BUFFER_SIZE;
            totalHours++;
        }
    }

    @Override
    protected boolean handleEvent(Event event) {
        long oldSensorTotalRain = sensorTotalRain;
        sensorTotalRain = event.getAttributeInt("FineOffset.Rain");
        if (oldSensorTotalRain > sensorTotalRain) {
            totalRainBase += oldSensorTotalRain;
        }
        return super.handleEvent(event);
    }

    public String getModel() {
        return MODEL;
    }

    public String getRain1h() {
        if (!hasBeenUpdated) {
            return "";
        }
        long rainToCompareWith;
        if (totalMinutes == 0) {
            rainToCompareWith = getTotalRainInternal();
        } else if (totalMinutes <= MINUTES_PER_HOUR) {
            rainToCompareWith = lastHour[0];
        } else {
            rainToCompareWith = getValue1HourAgo();
        }
        rainToCompareWith = (rainToCompareWith == 0) ? getTotalRainInternal() : rainToCompareWith;
        double rain1h = (getTotalRainInternal() - rainToCompareWith) * rainConstantK;
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
        rainTocompareWith = (rainTocompareWith == 0) ? getTotalRainInternal() : rainTocompareWith;
        double rain1h = (getTotalRainInternal() - rainTocompareWith) * rainConstantK;
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
            totalRainAtLastValue = getTotalRainInternal();
        }
        double rain1h = (getTotalRainInternal() - totalRainAtLastValue) * rainConstantK;
        return String.format("%.1f", rain1h);
    }

    public String getTotalRain() {
        return hasBeenUpdated ? String.format("%.1f", getTotalRainInternal() * rainConstantK) : "";
    }

    public String getRainK() {
        return Double.toString(rainConstantK);
    }

    public void setRainK(String rainK) {
        this.rainConstantK = Double.parseDouble(rainK);
    }

    public String getTotalRainBase() {
        return Double.toString(totalRainBase);
    }

    public void setTotalRainBase(String rainK) {
        this.rainConstantK = Double.parseDouble(rainK);
    }

    protected long getTotalRainInternal() {
        return totalRainBase + sensorTotalRain;
    }
}
