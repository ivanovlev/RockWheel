package lion.rockwheel.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import lion.rockwheel.MessageConstants;

/**
 * Сервис для работы с bluetooth
 */
public class BtService {
    /**
     * Обработчик событий
     */
    private Handler handler;

    /**
     * Подключения
     */
    private List<BtConnection> connections = new ArrayList();

    /**
     * Конструктор с указанием обработчика событий
     * @param handler обработчик
     */
    public BtService(Handler handler){
        this.handler = handler;
    }

    /**
     * Возвращает BT адаптер
     * проверяет его наличие и включает, если выключен
     * @return BT адаптер
     */
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

    /**
     * Возвращает сприсок спаренных с телефоном BT устройств
     * @return список BT устройств
     */
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

    /**
     * Подключиться к BT устройству по MAC адресу
     * @param address MAC адрес устройства
     */
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

    /**
     * Отправляет сообщение получателю через обработчик
     * @param text текст сообщения
     */
    private void showMessage(CharSequence text){
        handler.obtainMessage(MessageConstants.MESSAGE_ERROR, text).sendToTarget();
    }
}
