package lion.rockwheel.helpers;

import com.jjoe64.graphview.series.DataPoint;
import com.orm.query.Condition;
import com.orm.query.Select;
import java.util.ArrayList;
import java.util.List;
import lion.rockwheel.model.DeviceInfo;

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
    private static DeviceInfo lastInfo;

    /**
     * Кеш из бд на последние @see DbHelper#meters
     */
    private static List<DeviceInfo> cache = init();

    /**
     * Инициализация кеша
     * @return отсортированный массив с данными по возрастанию даты
     */
    private static List<DeviceInfo> init() {
        //Получаем актуальные данные по девайсу
        lastInfo = Select.from(DeviceInfo.class)
                        .orderBy("date DESC")
                        .first();

        if (lastInfo != null) {
            //Готовим кеш
            return Select.from(DeviceInfo.class)
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
    public static DeviceInfo save(DeviceInfo newInfo){
        if (lastInfo != null){
            //Получаем время движения
            float periodNanoSec = newInfo.date - lastInfo.date;
            double periodSec = periodNanoSec / 1E9;

            //Проверяем актуальность данных
            if(periodSec < 1){
                //Получаем среднюю скорость и переводим её из км\ч в м\с
                float speedKph = (lastInfo.speed + newInfo.speed) / 2;
                float speedMps = speedKph / 3.6f;

                //Рассчитываем пробег
                float move = (float)(speedMps * periodSec / 1000);

                //Сохраняем масималку, пройденную дистанцию и время в пути
                newInfo.distance = lastInfo.distance + move;
                newInfo.maxSpeed = newInfo.speed > lastInfo.maxSpeed ? newInfo.speed : lastInfo.maxSpeed;
                newInfo.elapsed = lastInfo.elapsed + periodNanoSec;

                //Апаем одометр
                CfgHelper.setTotalOdo(CfgHelper.getTotalOdo() + move);
            }else {
                newInfo.distance = lastInfo.distance;
                newInfo.maxSpeed = lastInfo.maxSpeed;
                newInfo.elapsed = lastInfo.elapsed;
            }
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

    public static DeviceInfo getLastInfo(){
        return lastInfo;
    }

    /**
     * Возвращает историю девайса за последние @see DbHelper#meters
     * @return история девайса
     */
    public static DataPoint[] getHistory(){
        DataPoint[] points = new DataPoint[cache.size()];
        for (DeviceInfo info: cache) {
            points[cache.indexOf(info)] = new DataPoint(info.distance, info.speed);
        }

        return points;
    }

    /**
     * Очищает историю в бд и кеше
     */
    public static void clearHistory(){
        DeviceInfo.deleteAll(DeviceInfo.class);
        cache.clear();
        lastInfo = null;
    }
}
