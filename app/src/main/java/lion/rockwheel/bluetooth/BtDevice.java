package lion.rockwheel.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * BT устройство
 * используется для корректного отображения пунктов с ними в интерфейсе
 */
public class BtDevice  {
    /**
     * Непосредственно bluetooth устройство
     */
    private BluetoothDevice device;

    /**
     * Конструктор с передачей bluetooth устройства
     * @param device bluetooth устройство
     */
    public BtDevice(BluetoothDevice device){
        this.device = device;
    }

    /**
     * Возвращает bluetooth устройство
     * @return bluetooth устройство
     */
    public BluetoothDevice getDevice(){
        return device;
    }

    /**
     * Возвращает имя bluetooth устройства
     * @return имя bluetooth устройства
     */
    public String toString(){
        return device.getName();
    }
}
