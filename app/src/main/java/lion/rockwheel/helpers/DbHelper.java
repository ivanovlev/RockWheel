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
    private static List<BtDeviceInfo> allHistory = Select.from(BtDeviceInfo.class)
            .orderBy("date")
            .list();

    private static List<BtDeviceInfo> shortHistory = Select.from(BtDeviceInfo.class)
            .orderBy("date")
            .list();

    public static BtDeviceInfo save(BtDeviceInfo info){
        if (info.speed > 0){
            info.save();
            allHistory.add(info);
        }

        return info;
    }

    public static DataPoint[] getHistory(BtDeviceInfo max, float meters){
        if (allHistory.size() > 1){
            BtDeviceInfo min = Collections.min(allHistory, (current, last) -> max.distance - current.distance >= meters ? 1 : -1);
            List<BtDeviceInfo> history = allHistory.subList(allHistory.indexOf(min), allHistory.size());

            DataPoint[] points = new DataPoint[history.size()];
            for (BtDeviceInfo info: history) {
                points[history.indexOf(info)] = new DataPoint(info.distance, info.speed);
            }

            return points;
        }

        return new DataPoint[0];
    }

    public static BtDeviceInfo getLastInfo(){
        return Collections.max(allHistory, (current, last) -> current.date.getTime() > last.date.getTime() ? 1 : -1);
    }

    public static void clearHistory(){
        BtDeviceInfo.deleteAll(BtDeviceInfo.class);
        allHistory.clear();
    }
}
