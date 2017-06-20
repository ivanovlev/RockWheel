package lion.rockwheel.bluetooth;

import com.orm.SugarRecord;

import java.util.Date;

/**
 * Класс информации о девайсе в момент времени
 * */
public class BtDeviceInfo extends SugarRecord<BtDeviceInfo> {
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
     * Сессия устройства
     */
    public float session = 0;

    /**
     * Дата фиксации данных
     */
    public Date date = new Date();

    /**
     * Конструктор по умолчанию (для sql lite)
     */
    public BtDeviceInfo(){

    }

    /**
     * Конструктор из строки данных устройства
     * @param rawInfo строка данных устройства
     */
    public BtDeviceInfo(String rawInfo){
        String[] split = rawInfo.split(",");

        speed = Float.parseFloat(split[0])/10;
        maxSpeed = Float.parseFloat(split[1])/10;
        voltage = Float.parseFloat(split[2])/10;
        distance = Float.parseFloat(split[3])/10000;
        session = Float.parseFloat(split[4]);
    }

    /**
     * Корректирует текущие данные по указанному состоянию
     * Для восстановления по предыдущей сессии после отключения питания BT устройства
     * @param last
     * @return
     */
    public BtDeviceInfo update(BtDeviceInfo last){
        if (last != null && last.session != session){
            if (last.maxSpeed > maxSpeed){
                maxSpeed = last.maxSpeed;
            }

            distance += last.distance;
        }

        return this;
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
    public boolean equals(BtDeviceInfo info){
        if (info != null && info.distance == distance && info.speed == speed && info.voltage == voltage){
            return true;
        }

        return false;
    }
}
