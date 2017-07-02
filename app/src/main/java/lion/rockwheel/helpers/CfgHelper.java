package lion.rockwheel.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import static android.content.SharedPreferences.Editor;

/**
 * Created by lion on 6/5/17.
 */

public class CfgHelper {
    private SharedPreferences sharedPref = null;

    private static final String lastBtDevice = "lastBtDevice";
    private static final String batterySeries = "batterySeries";
    private static final String cellLow = "cellLow";
    private static final String cellHigh = "cellHigh";
    private static final String speedCorr = "speedCorr";
    private static final String connTimeOut = "connTimeOut";

    public CfgHelper(Activity activity){
        sharedPref = activity.getSharedPreferences("AppData", Context.MODE_PRIVATE);
    }

    //region Getters

    public String getLastBtDeviceAddress(){
        return sharedPref.getString(lastBtDevice, null);
    }

    public Integer getBatterySeries(){
        return sharedPref.getInt(batterySeries, 14);
    }

    public Float getCellLow(){
        return sharedPref.getFloat(cellLow, 3.3f);
    }

    public Float getCellHigh(){
        return sharedPref.getFloat(cellHigh, 4.2f);
    }

    public Float getSpeedCorr(){
        return sharedPref.getFloat(speedCorr, 1.33f);
    }

    public Integer getConnectionTimeOut(){ return sharedPref.getInt(connTimeOut, 5); }

    //endregion

    //region Setters

    public void setBatterySeries(Integer series){
        saveOption(batterySeries, series);
    }

    public void setCellLow(Float lowLevel){
        saveOption(cellLow, lowLevel);
    }

    public void setCellHigh(Float highLevel){
        saveOption(cellHigh, highLevel);
    }

    public void setLastBtDeviceAddress(String address){
        saveOption(lastBtDevice, address);
    }

    public void setSpeedCorr(Float corr){ saveOption(speedCorr, corr); }

    public void setConnectionTimeOut(Integer timeOut){ saveOption(connTimeOut, timeOut); }

    //endregion

    private void saveOption(String option, Object value)
    {
        Editor editor = sharedPref.edit();

        if (value instanceof Integer){
            editor.putInt(option, (int)value);
        }

        if (value instanceof Float){
            editor.putFloat(option, (Float)value);
        }

        if (value instanceof String){
            editor.putString(option, (String)value);
        }

        editor.commit();
    }
}
