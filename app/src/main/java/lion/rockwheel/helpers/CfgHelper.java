package lion.rockwheel.helpers;

import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lion.rockwheel.model.ConfigInfo;

/**
 * Created by lion on 6/5/17.
 */

public class CfgHelper {
    private static final String lastBtDevice = "lastBtDevice";
    private static final String batterySeries = "batterySeries";
    private static final String cellLow = "cellLow";
    private static final String cellHigh = "cellHigh";
    private static final String speedCorr = "speedCorr";
    private static final String speedLimit = "speedLimit";
    private static final String speedAlert = "speedAlert";
    private static final String connTimeOut = "connTimeOut";
    private static final String totalOdo = "totalOdo";

    private static Map<String, ConfigInfo> settingsMap = init();

    private static Map<String, ConfigInfo> init(){
        List<ConfigInfo> settings = Select.from(ConfigInfo.class).list();

        Map<String,ConfigInfo> map = new HashMap();
        for (ConfigInfo i : settings) {
            map.put(i.getOption(),i);
        }

        return map;
    }

    //region Getters

    public static String getLastBtDeviceAddress(){ return getOption(lastBtDevice, ""); }

    public static Integer getBatterySeries(){ return getOption(batterySeries, 14); }

    public static Float getCellLow(){ return getOption(cellLow, 3.3f); }

    public static Float getCellHigh(){ return getOption(cellHigh, 4.2f); }

    public static Float getSpeedCorr(){ return getOption(speedCorr, 1.19f); }

    public static Integer getSpeedLimit(){ return getOption(speedLimit, 30); }

    public static Boolean getSpeedAlert(){ return getOption(speedAlert, true); }

    public static Integer getConnectionTimeOut(){ return getOption(connTimeOut, 5); }

    public static Float getTotalOdo(){ return getOption(totalOdo, 0f); }

    //endregion

    //region Setters

    public static void setBatterySeries(Integer series){ saveOption(batterySeries, series); }

    public static void setCellLow(Float lowLevel){
        saveOption(cellLow, lowLevel);
    }

    public static void setCellHigh(Float highLevel){
        saveOption(cellHigh, highLevel);
    }

    public static void setLastBtDeviceAddress(String address){
        saveOption(lastBtDevice, address);
    }

    public static void setSpeedCorr(Float corr){ saveOption(speedCorr, corr); }

    public static void setSpeedLimit(Integer limit){ saveOption(speedLimit, limit); }

    public static void setSpeedAlert(Boolean alert){ saveOption(speedAlert, alert); }

    public static void setConnectionTimeOut(Integer timeOut){ saveOption(connTimeOut, timeOut); }

    public static void setTotalOdo(Float odometer){ saveOption(totalOdo, odometer); }

    //endregion

    private static void saveOption(String option, Object value){
        if(settingsMap.containsKey(option)){
            ConfigInfo info = settingsMap.get(option);
            info.setValue(value);
            info.save();
        }else {
            ConfigInfo info = new ConfigInfo(option, value);
            settingsMap.put(option, info);
            info.save();
        }
    }

    private static <T> T getOption(String option, T defaultValue){
        if(settingsMap.containsKey(option)){
            return (T)settingsMap.get(option).getValue();
        }

        return defaultValue;
    }
}
