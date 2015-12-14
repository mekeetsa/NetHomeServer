package nu.nethome.home.items.timer.SunTimer;

import nu.nethome.home.item.HomeItemAdapter;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static nu.nethome.home.items.timer.SunTimer.TimeExpressionParser.SwitchTime;
import static nu.nethome.home.items.timer.SunTimer.TimeExpressionParser.TIME_EXPRESSION_SEPARATOR;
import static nu.nethome.home.items.timer.SunTimer.TimeExpressionParser.TIME_PERIOD_SEPARATOR;

/**
 *
 */
@SuppressWarnings("UnusedDeclaration")
public class SunTimer extends HomeItemAdapter {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"SunTimer\" Category=\"Timers\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Init=\"setState\" Default=\"true\" />"
            + "  <Attribute Name=\"Location: Lat,Long\" Type=\"String\" Get=\"getLatLong\" 	Set=\"setLatLong\" />"
            + "  <Attribute Name=\"Timer Today\" Type=\"String\" Get=\"getTodayStartEnd\" />"
            + "  <Attribute Name=\"Sunrise (R)\" Type=\"String\" Get=\"getSunriseToday\" />"
            + "  <Attribute Name=\"Sunset (S)\" Type=\"String\" Get=\"getSunsetToday\" />"
            + "  <Attribute Name=\"Mondays\" Type=\"String\" Get=\"getMondays\" 	Set=\"setMondays\" />"
            + "  <Attribute Name=\"Tuesdays\" Type=\"String\" Get=\"getTuesdays\" 	Set=\"setTuesdays\" />"
            + "  <Attribute Name=\"Wednesdays\" Type=\"String\" Get=\"getWednesdays\" 	Set=\"setWednesdays\" />"
            + "  <Attribute Name=\"Thursdays\" Type=\"String\" Get=\"getThursdays\" 	Set=\"setThursdays\" />"
            + "  <Attribute Name=\"Fridays\" Type=\"String\" Get=\"getFridays\" 	Set=\"setFridays\" />"
            + "  <Attribute Name=\"Saturdays\" Type=\"String\" Get=\"getSaturdays\" 	Set=\"setSaturdays\" />"
            + "  <Attribute Name=\"Sundays\" Type=\"String\" Get=\"getSundays\" 	Set=\"setSundays\" />"
            + "  <Attribute Name=\"OnCommand\" Type=\"Command\" Get=\"getOnCommand\" 	Set=\"setOnCommand\" />"
            + "  <Attribute Name=\"OffCommand\" Type=\"Command\" Get=\"getOffCommand\" 	Set=\"setOffCommand\" />"
            + "  <Action Name=\"Enable timer\" 	Method=\"enableTimer\" />"
            + "  <Action Name=\"Disable timer\" 	Method=\"disableTimer\" />"
            + "</HomeItem> ");

    private String[] weekDays = new String[7];
    private List<SwitchTime> switchTimesToday = Collections.emptyList();

    public SunTimer() {
        for (int i = 0; i < weekDays.length; i++) {
            weekDays[i] = "";
        }
    }

    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public void activate() {
        calculateSwitchTimesForToday();
    }

    void calculateSwitchTimesForToday() {
        try {
            switchTimesToday = TimeExpressionParser.parseExpression(getTodaysTimeExpression());
        } catch (TimeExpressionParser.TimeExpressionException e) {
            switchTimesToday = Collections.emptyList();
        }
    }

    private String getTodaysTimeExpression() {
        return weekDays[getToday() - 1];
    }

    public String getTodayStartEnd() {
        String result = "";
        boolean isFirst = true;
        boolean lastTimeIsOn = false;
        for (SwitchTime time : switchTimesToday) {
            if (!(lastTimeIsOn && !time.isOn()) && !isFirst) {
                result += TIME_EXPRESSION_SEPARATOR;
            }
            if (!time.isOn() && !lastTimeIsOn) {
                result += TIME_PERIOD_SEPARATOR;
            }
            result += time.valueAsTimeString();
            if (time.isOn()) {
                result += TIME_PERIOD_SEPARATOR;
            }
            lastTimeIsOn = time.isOn();
            isFirst = false;
        }
        return result;
    }

    int getToday() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }

    public String getMondays() {
        return weekDays[1];
    }

    public void setMondays(String times) {
        setTimeExpressionForDay(1, times);
    }

    private void setTimeExpressionForDay(int day, String times) {
        String oldValue = weekDays[day];
        weekDays[day] = times;
        if (isActivated() && !oldValue.equals(times)) {
            calculateSwitchTimesForToday();
        }
    }

    public String getTuesdays() {
        return weekDays[2];
    }

    public void setTuesdays(String times) {
        setTimeExpressionForDay(2, times);
    }

    public String getWednesdays() {
        return weekDays[3];
    }

    public void setWednesdays(String times) {
        setTimeExpressionForDay(3, times);
    }

    public String getThursdays() {
        return weekDays[4];
    }

    public void setThursdays(String times) {
        setTimeExpressionForDay(4, times);
    }

    public String getFridays() {
        return weekDays[5];
    }

    public void setFridays(String times) {
        setTimeExpressionForDay(5, times);
    }

    public String getSaturdays() {
        return weekDays[6];
    }

    public void setSaturdays(String times) {
        setTimeExpressionForDay(6, times);
    }

    public String getSundays() {
        return weekDays[0];
    }

    public void setSundays(String times) {
        setTimeExpressionForDay(0, times);
    }
}
