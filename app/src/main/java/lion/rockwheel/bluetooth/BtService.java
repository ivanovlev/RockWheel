package lion.rockwheel.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import lion.rockwheel.MessageConstants;

/**
 * Created by lion on 6/4/17.
 */

public class BtService {
    private Handler handler;
    private List<BtConnection> connections = new ArrayList();

    public BtService(Handler handler){
        this.handler = handler;
    }

    private BluetoothAdapter GetBtAdapter() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(btAdapter == null) {
            showMessage("Bluetooth не поддерживается");
        } else {
            if (!btAdapter.isEnabled()) {
                handler.obtainMessage(MessageConstants.REQUEST_ENABLE_BT, new Intent(btAdapter.ACTION_REQUEST_ENABLE)).sendToTarget();
            }
        }

        return btAdapter;
    }

    public List<BtDevice> GetDevices(){
        BluetoothAdapter btAdapter = GetBtAdapter();

        List<BtDevice> devices = new ArrayList();

        if (btAdapter != null){
            showMessage("...Получаю список устройств...");

            for (BluetoothDevice device: btAdapter.getBondedDevices()) {
                devices.add(new BtDevice(device));
            }
        }

        return devices;
    }

    public void listenDevice(String address){
        BluetoothDevice device = GetBtAdapter().getRemoteDevice(address);
        if (device != null){
            showMessage(String.format("Подключение к %1$s", device.getName()));
            if (connections.size() == 1)
            {
                connections.get(0).cancel();
            }
            if (connections.size() == 2){
                connections.get(1).cancel();
                connections.remove(0);
            }

            connections.add(new BtConnection(device, handler));
        }
    }

    private BtDevice getDevice(String address) {
        for (BtDevice device : GetDevices()) {
            if (device.getDevice().getAddress().equals(address)){
                return device;
            }
        }

        showMessage(String.format("Адрес %1$s не найден", address));
        return null;
    }

    private void showMessage(CharSequence text){
        handler.obtainMessage(MessageConstants.MESSAGE_ERROR, text).sendToTarget();
    }
}
