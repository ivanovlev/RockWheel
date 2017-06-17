package lion.rockwheel.bluetooth;

import com.orm.SugarRecord;

import java.util.Date;

/**
 * Класс информации о девайсе в момент времени
 * */
public class BtDeviceInfo extends SugarRecord<BtDeviceInfo> {
    public float speed = 0;

    public float maxSpeed = 0;

    public float voltage = 0;

    public float distance = 0;

    public Date date = new Date();

    public BtDeviceInfo(){

    }

    public BtDeviceInfo(String rawInfo){
        String[] split = rawInfo.split(",");

        speed = Float.parseFloat(split[0])/10;
        maxSpeed = Float.parseFloat(split[1])/10;
        voltage = Float.parseFloat(split[2])/10;
        distance = Float.parseFloat(split[3])/10000;
    }

    public BtDeviceInfo update(BtDeviceInfo last){
        if (last != null){
            if (last.maxSpeed > maxSpeed){
                maxSpeed = last.maxSpeed;
            }

            distance += last.distance;
        }

        return this;
    }

    public BtDeviceInfo toDb()
    {
        if (speed > 0){
            save();
        }

        return this;
    }

    public float getSpeedPecent(int limit){
        if (speed > limit){
            return 100;
        }

        return speed / limit * 100;
    }

    public float getCellVoltage(int s){
        return voltage / s;
    }
}
