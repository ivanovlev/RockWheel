package lion.rockwheel.helpers;

import com.jjoe64.graphview.series.DataPoint;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lion.rockwheel.bluetooth.BtDeviceInfo;

/**
 * Помощник работы с БД
 */
public class DbHelper {
    /**
     * Ограничение обрабатываемого пробега. Ускоряет производительность в разы
     */
    private static float meters = 0.2f;

    /**
     * Кеш из бд на последние @see DbHelper#meters
     */
    private static List<BtDeviceInfo> cache = init();

    /**
     * Актуальные данные
     */
    private static BtDeviceInfo lastInfo = getLastInfo();

    /**
     * Инициализация кеша
     * @return отсортированный массив с данными по возрастанию даты
     */
    private static List<BtDeviceInfo> init() {
        BtDeviceInfo max = Select.from(BtDeviceInfo.class)
                .orderBy("date DESC")
                .first();

        if (max != null) {
            return Select.from(BtDeviceInfo.class)
                    .where(Condition.prop("distance").gt(max.distance - meters))
                    .orderBy("date")
                    .list();
        }

        return new ArrayList();
    }

    /**
     * Сохранить информацию в БД
     * @param info информация о девайсе
     * @return информация о девайсе
     */
    public static BtDeviceInfo save(BtDeviceInfo info){
        if (!info.equals(lastInfo)){
            info.save();
            cache.add(info);
            lastInfo = info;

            while (info.distance - cache.get(0).distance > meters){
                cache.remove(0);
            }
        }

        return lastInfo;
    }

    /**
     * Возвращает историю девайса за последние @see DbHelper#meters
     * @return история девайса
     */
    public static DataPoint[] getHistory(){
        DataPoint[] points = new DataPoint[cache.size()];
        for (BtDeviceInfo info: cache) {
            points[cache.indexOf(info)] = new DataPoint(info.distance, info.speed);
        }

        return points;
    }

    /**
     * Обновляет актуальную информацию о девайсе
     * @return текущая информация о девайсе
     */
    public static BtDeviceInfo getLastInfo(){
        if (cache.size() > 0){
            return Collections.max(cache, (current, last) -> current.date.compareTo(last.date));
        }

        return null;
    }

    /**
     * Очищает историю в бд и кеше
     */
    public static void clearHistory(){
        BtDeviceInfo.deleteAll(BtDeviceInfo.class);
        cache.clear();
    }
}
