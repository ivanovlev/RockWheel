package lion.rockwheel.helpers;

import com.jjoe64.graphview.series.DataPoint;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

    public static DataPoint[] getHistory(float round){
        List<BtDeviceInfo> history = Select.from(BtDeviceInfo.class)
                .orderBy("date")
                //.limit("120")
                .list();

        if (history.size() > 120){
            history = history.subList(history.size() - 120, history.size() - 1);
        }

        List<DataPoint> points = new ArrayList();
        int records = 0;
        float speedSum = 0;
        float distance = 0;
        for (BtDeviceInfo info: history) {
            speedSum += info.speed;
            records++;

            if (info.distance - distance > round) {
                points.add(new DataPoint(info.distance, speedSum / records));

                distance = info.distance;
                records = 0;
                speedSum = 0;
            }
        }

        if (records > 0){
            points.add(new DataPoint(distance + round, speedSum / records));
        }

        return points.toArray(new DataPoint[points.size()]);
    }

    public static BtDeviceInfo getLastInfo(){
        List<BtDeviceInfo> history = Select.from(BtDeviceInfo.class)
                .orderBy("date")
                .list();

        if (history.size() > 0){
            return history.get(history.size() - 1);
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
