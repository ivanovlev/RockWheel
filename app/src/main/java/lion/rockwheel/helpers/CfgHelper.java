package lion.rockwheel.helpers;

import com.orm.query.Condition;
import com.orm.query.Select;

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

    //region Getters

    public static String getLastBtDeviceAddress(){ return getOption(lastBtDevice, ""); }

    public static Integer getBatterySeries(){ return getOption(batterySeries, 14); }

    public static Float getCellLow(){ return getOption(cellLow, 3.3f); }

    public static Float getCellHigh(){ return getOption(cellHigh, 4.2f); }

    public static Float getSpeedCorr(){ return getOption(speedCorr, 1.19f); }

    public static Integer getSpeedLimit(){ return getOption(speedLimit, 30); }

    public static Boolean getSpeedAlert(){ return getOption(speedAlert, true); }

    public static Integer getConnectionTimeOut(){ return getOption(connTimeOut, 5); }

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

    //endregion

    private static void saveOption(String option, Object value){
        ConfigInfo info = Select.from(ConfigInfo.class)
                .where(Condition.prop("option").eq(option))
                .first();

        if (info != null){
            info.setValue(value);
        }else {
            info = new ConfigInfo(option, value);
        }

        info.save();
    }

    private static <T> T getOption(String option, T defaultValue){
        ConfigInfo info = Select.from(ConfigInfo.class)
                .where(Condition.prop("option").eq(option))
                .first();

        if (info != null){
            return (T)info.getValue();
        }

        return defaultValue;
    }
}
