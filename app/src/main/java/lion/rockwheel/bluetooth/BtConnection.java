package lion.rockwheel.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.InputStream;
import java.util.UUID;

import lion.rockwheel.helpers.DbHelper;
import lion.rockwheel.MessageConstants;

/**
 * Created by lion on 6/5/17.
 */

public class BtConnection extends Thread {
    public final static UUID SppId = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    BluetoothDevice device;
    Boolean listen;
    Handler handler;

    public BtConnection(BluetoothDevice device, Handler handler) {
        this.handler = handler;
        this.device = device;

        start();
    }

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

    public void run() {
        byte[] mmBuffer = new byte[64];
        int numBytes = 0; // bytes returned from read()
        String rawInfo = "";
        String result = "";
        InputStream inStream;
        BluetoothSocket socket;
        BtDeviceInfo lastInfo = DbHelper.getLastInfo();

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
                        handler.obtainMessage(MessageConstants.MESSAGE_READ, new BtDeviceInfo(result).update(lastInfo).toDb()).sendToTarget();
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

    public void cancel() {
        listen = false;
    }

    private String getBufferText(byte[] _bytes, int size)
    {
        String file_string = "";

        for(int i = 0; i < size; i++)
        {
            file_string += (char)_bytes[i];
        }

        return file_string;
    }

    private void showMessage(Exception e){
        handler.obtainMessage(MessageConstants.MESSAGE_ERROR, e.getMessage()).sendToTarget();
    }
}
