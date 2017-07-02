package lion.rockwheel.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.InputStream;
import java.util.UUID;

import lion.rockwheel.MessageConstants;
import lion.rockwheel.helpers.DbHelper;

/**
 * Работа с bluetooth устройством
 */
public class BtConnection extends Thread {

    // Id COM порта телефона
    public final static UUID SppId = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Bluetooth устройство
    BluetoothDevice device;

    // Таймаут реконнекта
    Double timeOut;

    // Флаг прослушки COM порта
    Boolean listen;

    // Обработчик событий
    Handler handler;

    /**
     * Конструктор с передачей bluetooth устройства и обработчика событий
     * @param device bluetooth устройство
     * @param timeOut таймаут реконнекта
     * @param handler обработчик событий
     */
    public BtConnection(BluetoothDevice device, Integer timeOut, Handler handler) {
        this.handler = handler;
        this.device = device;
        this.timeOut = timeOut * 60 * 1E9;

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
        Long lastRespondTime = null; // время последнего ответа
        byte[] mmBuffer = new byte[64];
        int numBytes = 0; // bytes returned from read()
        String rawInfo = "";
        String result = "";
        InputStream inStream;
        BluetoothSocket socket;

        // после превышения таймаута поездка считается оконченной
        while (listen == null || (listen && System.nanoTime() - lastRespondTime < timeOut)) {

            if (lastRespondTime != null){
                double expire =  (timeOut - (System.nanoTime() - lastRespondTime)) / (1E9 * 60);
                int min = (int)expire;
                int sec = (int)((expire - min) * 60);
                sendState(String.format("Reconnect... %1$s:%2$02d left", min, sec));
            }else {
                listen = false;
            }

            // слушаем поток пока живо соединиение
            try {
                socket = connect(3);
                if (socket != null && socket.isConnected()) {
                    inStream = socket.getInputStream();
                    listen = true;

                    while (listen) {
                        // фиксируем активность
                        lastRespondTime = System.nanoTime();

                        // читаем поток в буфер
                        numBytes = inStream.read(mmBuffer);

                        rawInfo = rawInfo + getBufferText(mmBuffer, numBytes);

                        int start = rawInfo.indexOf("<");
                        int end = rawInfo.indexOf(">");

                        if (start >= 0 && end >= 0) {
                            result = rawInfo.substring(start + 1, end);
                            rawInfo = rawInfo.substring(end + 1);

                            // отправляем результат в GUI
                            sendInfo(DbHelper.save(new BtDeviceInfo(result)));
                        }
                    }

                    inStream.close();
                    socket.close();
                }
            } catch (Exception e) {
                sendMessage(e);
            }
        }

        if (lastRespondTime == null){
            sendMessage("Подключение не удалось");
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

    //region Senders

    /**
     * Передача информации о девайсе получателю через обработчик
     * @param info информация о девайсе
     */
    private void sendInfo(BtDeviceInfo info){
        sendState("Connected");
        handler.obtainMessage(MessageConstants.MESSAGE_READ, info).sendToTarget();
    }

    /**
     * Передача ошибок получателю через обработчик
     * @param e ошибка
     */
    private void sendMessage(Exception e){
        sendMessage(e.getMessage());
    }

    /**
     * Передача ошибок получателю через обработчик
     * @param msg ошибка
     */
    private void sendMessage(String msg){
        sendState("");
        handler.obtainMessage(MessageConstants.MESSAGE_ERROR, msg).sendToTarget();
    }


    /**
     * Передача сообщений получателю через обработчик
     * @param msg статус
     */
    private void sendState(String msg){
        handler.obtainMessage(MessageConstants.CONNECTION_STATE, msg).sendToTarget();
    }

    //endregion
}
