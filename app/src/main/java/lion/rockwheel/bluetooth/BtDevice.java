package lion.rockwheel.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Created by lion on 6/4/17.
 */

public class BtDevice  {

    private BluetoothDevice device;

    public BtDevice(BluetoothDevice device){
        this.device = device;
    }

    public BluetoothDevice getDevice(){
        return device;
    }

    public String toString(){
        return device.getName();
    }
}
