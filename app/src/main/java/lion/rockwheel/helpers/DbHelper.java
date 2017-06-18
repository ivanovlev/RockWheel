package lion.rockwheel.helpers;

import com.jjoe64.graphview.series.DataPoint;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lion.rockwheel.bluetooth.BtDevice;
import lion.rockwheel.bluetooth.BtDeviceInfo;

/**
 * Created by lion on 6/15/17.
 */

public class DbHelper {
    public static List<BtDeviceInfo> getHistory(Date date){
        long startDate = clearDate(date).getTime();
        long endDate = getNextDay(startDate);

       return Select.from(BtDeviceInfo.class)
                .where(Condition.prop("date").gt(startDate)).and(Condition.prop("date").lt(endDate))
                .orderBy("date")
                .list();
    }

    public static DataPoint[] getHistory(BtDeviceInfo current, float meters){
        List<BtDeviceInfo> history = Select.from(BtDeviceInfo.class)
                .where(Condition.prop("distance").gt(current.distance - meters))
                .orderBy("date")
                .list();

        DataPoint[] points = new DataPoint[history.size()];
        for (BtDeviceInfo info: history) {
            points[history.indexOf(info)] = new DataPoint(info.distance, info.speed);
        }

        return points;
    }

    public static BtDeviceInfo getLastInfo(){
        List<BtDeviceInfo> history = BtDeviceInfo.find(BtDeviceInfo.class, null, null, null, "date DESC", "1");

        if (history.size() > 0){
            return history.get(0);
        }

        return null;
    }

    public static void clearHistory(){
        BtDeviceInfo.deleteAll(BtDeviceInfo.class);
    }

    private static Date clearDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static Long getNextDay(long date){
        return date + 1 * 24 * 60 * 60 * 1000;
    }
}
