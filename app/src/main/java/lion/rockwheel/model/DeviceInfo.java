package lion.rockwheel.model;

import com.orm.SugarRecord;

/**
 * Класс информации о девайсе в момент времени
 * */
public class DeviceInfo extends SugarRecord {
    /**
     * Текущая скорость
     */
    public float speed = 0;

    /**
     * Максимальная скорость
     */
    public float maxSpeed = 0;

    /**
     * Напряжение батареи
     */
    public float voltage = 0;

    /**
     * Пробег
     */
    public float distance = 0;

    /**
     * Время поездки
     */
    public float elapsed = 0;

    /**
     * Дата фиксации данных
     */
    public float date = System.nanoTime();

    /**
     * Конструктор по умолчанию (для sql lite)
     */
    public DeviceInfo(){

    }

    /**
     * Конструктор из строки данных устройства
     * @param rawInfo строка данных устройства
     */
    public DeviceInfo(String rawInfo, float speedCorr){
        String[] split = rawInfo.split(",");

        speed = Float.parseFloat(split[0])/10;
        voltage = Float.parseFloat(split[2])/10;

        speed = Math.round(speed * speedCorr * 10.0f) / 10.0f;
    }

    /**
     * Возвращает скорость в % от максимальной
     * @param limit максимальная скорость
     * @return скорость в %
     */
    public float getSpeedPecent(int limit){
        if (speed > limit){
            return 100;
        }

        return speed / limit * 100;
    }

    /**
     * Возвращает напряжение на одной ячейке
     * @param s количество серий ячеек (S) в батарее
     * @return напряжение одной ячейки
     */
    public float getCellVoltage(int s){
        return voltage / s;
    }

    /**
     * Сравнение двух объектов
     * @param info друга информация
     * @return совпадает или нет
     */
    public boolean equals(DeviceInfo info){
        if (info != null && info.distance == distance && info.speed == speed && info.voltage == voltage){
            return true;
        }

        return false;
    }
}
