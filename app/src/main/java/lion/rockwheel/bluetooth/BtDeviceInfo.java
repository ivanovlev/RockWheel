package lion.rockwheel.bluetooth;

import com.orm.SugarRecord;

import java.util.Date;

/**
 * Класс информации о девайсе в момент времени
 * */
public class BtDeviceInfo extends SugarRecord<BtDeviceInfo> {
    private float speed = 0;

    private float maxSpeed = 0;

    private float voltage = 0;

    private float distance = 0;

    private Date date = new Date();

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
            if (last.getMaxSpeed() > maxSpeed){
                maxSpeed = last.getMaxSpeed();
            }

            distance += last.getDistance();
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

    public float getSpeed(){
        return speed;
    }

    public float getMaxSpeed(){
        return maxSpeed;
    }

    public float getVoltage(){
        return voltage;
    }

    public float getCellVoltage(int s){
        return voltage / s;
    }

    public float getDistance(){ return distance; }

    public Date getDate() {return date; }

}
