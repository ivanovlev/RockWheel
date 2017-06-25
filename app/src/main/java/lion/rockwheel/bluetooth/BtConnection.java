package lion.rockwheel.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.InputStream;
import java.util.UUID;

import lion.rockwheel.helpers.DbHelper;
import lion.rockwheel.MessageConstants;

/**
 * Работа с bluetooth устройством
 */
public class BtConnection extends Thread {
    /**
     * Id COM порта телефона
     */
    public final static UUID SppId = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    /**
     * bluetooth устройство
     */
    BluetoothDevice device;

    /**
     * Флаг прослушки COM порта
     */
    Boolean listen;

    /**
     * Обработчик событий
     */
    Handler handler;

    /**
     * Конструктор с передачей bluetooth устройства и обработчика событий
     * @param device bluetooth устройство
     * @param handler обработчик событий
     */
    public BtConnection(BluetoothDevice device, Handler handler) {
        this.handler = handler;
        this.device = device;

        start();
    }

    /**
     * Подключение у bluetooth устройству
     * @param retry количество попыток
     * @return открытый bluetooth сокет
     * @throws Exception
     */
    private BluetoothSocket connect(Integer retry) throws Exception {
        BluetoothSocket socket;
        Integer count = 0;

        while (count < retry) {
            Thread.sleep(100);

            try {
                socket = device.createRfcommSocketToServiceRecord(SppId);
                socket.connect();

                if (socket.isConnected()) {
                    return socket;
                }
            } catch (Exception e) {
                if (count == retry) {
                    throw e;
                }
            }

            count++;
        }

        return null;
    }

    /**
     * Прослушка COM порта к bluetooth устройству
     */
    public void run() {
        byte[] mmBuffer = new byte[64];
        int numBytes = 0; // bytes returned from read()
        String rawInfo = "";
        String result = "";
        InputStream inStream;
        BluetoothSocket socket;

        // слушаем поток пока живо соединиение
        try {
            socket = connect(3);
            if (socket != null && socket.isConnected()) {
                inStream = socket.getInputStream();
                listen = true;

                while (listen) {
                    // читаем поток в буфер
                    numBytes = inStream.read(mmBuffer);

                    rawInfo = rawInfo + getBufferText(mmBuffer, numBytes);

                    int start = rawInfo.indexOf("<");
                    int end = rawInfo.indexOf(">");

                    if (start >= 0 && end >= 0) {
                        result = rawInfo.substring(start + 1, end);
                        rawInfo = rawInfo.substring(end + 1);

                        // отправляем результат в GUI
                        if (listen){
                            handler.obtainMessage(MessageConstants.MESSAGE_READ, DbHelper.save(new BtDeviceInfo(result))).sendToTarget();
                        }
                    }
                }

                inStream.close();
                socket.close();
            }
        }
        catch (Exception e) {
            showMessage(e);
        }

    }

    /**
     * Остановка прослушки COM порта
     */
    public void cancel() {
        listen = false;
    }

    /**
     * Преобразование буфера из байтов в текст
     * @param _bytes буфер
     * @param size реальная длинна содержимого буфера
     * @return текст
     */
    private String getBufferText(byte[] _bytes, int size)
    {
        String file_string = "";

        for(int i = 0; i < size; i++)
        {
            file_string += (char)_bytes[i];
        }

        return file_string;
    }

    /**
     * Передача ошибок получателю через обработчик
     * @param e ошибка
     */
    private void showMessage(Exception e){
        handler.obtainMessage(MessageConstants.MESSAGE_ERROR, e.getMessage()).sendToTarget();
    }
}
