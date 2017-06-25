package lion.rockwheel.helpers;

import com.jjoe64.graphview.series.DataPoint;
import com.orm.query.Condition;
import com.orm.query.Select;
import java.util.ArrayList;
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
     * Актуальные данные
     */
    private static BtDeviceInfo lastInfo;

    /**
     * Кеш из бд на последние @see DbHelper#meters
     */
    private static List<BtDeviceInfo> cache = init();

    /**
     * Инициализация кеша
     * @return отсортированный массив с данными по возрастанию даты
     */
    private static List<BtDeviceInfo> init() {
        //Получаем актуальные данные по девайсу
        lastInfo = Select.from(BtDeviceInfo.class)
                        .orderBy("date DESC")
                        .first();

        if (lastInfo != null) {
            //Готовим кеш
            return Select.from(BtDeviceInfo.class)
                    .where(Condition.prop("distance").gt(lastInfo.distance - meters))
                    .orderBy("date")
                    .list();
        }

        return new ArrayList();
    }

    /**
     * Сохранить информацию в БД
     * @param newInfo информация о девайсе
     * @return информация о девайсе
     */
    public static BtDeviceInfo save(BtDeviceInfo newInfo){
        if (lastInfo != null){
            //Получаем среднюю скорость и переводим её из км\ч в м\с
            //float speedKph = (lastInfo.speed + newInfo.speed) / 2;
            float speedKph = lastInfo.speed < newInfo.speed ? lastInfo.speed : newInfo.speed;
            float speedMps = speedKph / 3.6f;

            //Получаем время движения с вычесленной средней скоростью
            float periodNanoSec = newInfo.date - lastInfo.date;
            double periodSec = periodNanoSec / 1E9;

            //Сохраняем масималку и пройденную дистанцию
            newInfo.distance = lastInfo.distance + (float)(speedMps * periodSec / 1000);
            newInfo.maxSpeed = newInfo.speed > lastInfo.maxSpeed ? newInfo.speed : lastInfo.maxSpeed;
        }

        //сохраняем при наличии изменений и чистим кеш от устаревших данных
        if (!newInfo.equals(lastInfo)){
            newInfo.save();
            cache.add(newInfo);
            lastInfo = newInfo;

            while (newInfo.distance - cache.get(0).distance > meters){
                cache.remove(0);
            }
        }

        return lastInfo;
    }

    public static float getAverageSpeed(){
        float sp = 0;
        if (cache.size() > 0){
            for (BtDeviceInfo info: cache) {
                sp+= info.speed;
            }

            return sp / cache.size();
        }
        return 0;
    }

    public static double getAverageTime(){
        float start = 0;
        float end = 0;
        if (cache.size() > 0){
            start = cache.get(0).date;
            end = lastInfo.date;
        }

        return (end-start) / 1E9;
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
     * Очищает историю в бд и кеше
     */
    public static void clearHistory(){
        BtDeviceInfo.deleteAll(BtDeviceInfo.class);
        cache.clear();
        lastInfo = null;
    }
}
