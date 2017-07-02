package lion.rockwheel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ObjectInputValidation;

import lion.rockwheel.bluetooth.BtDevice;
import lion.rockwheel.bluetooth.BtService;
import lion.rockwheel.helpers.CfgHelper;
import lion.rockwheel.helpers.DbHelper;

public class SettingsPanel extends BasePanel {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_panel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BtService btService = new BtService(getHandler());

        Button btnDevice = (Button)findViewById(R.id.btnDevice);

        btnDevice.setOnClickListener((view -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.list_dialog);
            ListView lvDevices = (ListView) dialog.findViewById(R.id.lvItems);

            // список доступных устройств
            ArrayAdapter deviceList = new ArrayAdapter(this, android.R.layout.simple_list_item_1, btService.GetDevices());

            // присваиваем адаптер списку
            lvDevices.setAdapter(deviceList);

            // выбор устройства
            lvDevices.setOnItemClickListener((parent, v, p, id) -> {
                BtDevice device = ((BtDevice)parent.getItemAtPosition(p));
                btnDevice.setText(device.getDevice().getAddress());
                dialog.dismiss();
                onBackPressed();
            });

            dialog.setCancelable(true);
            dialog.setTitle("Pared devices:");
            dialog.show();
        }));

        setViewText(R.id.btnDevice, CfgHelper.getLastBtDeviceAddress());
        setViewText(R.id.etLowCellVoltage, CfgHelper.getCellLow());
        setViewText(R.id.etHighCellVoltage, CfgHelper.getCellHigh());
        setViewText(R.id.etBatterySeries, CfgHelper.getBatterySeries());
        setViewText(R.id.etConnectionTimeOut, CfgHelper.getConnectionTimeOut());
        setViewText(R.id.etSpeedCorr, CfgHelper.getSpeedCorr());
        setViewText(R.id.etSpeedLimit, CfgHelper.getSpeedLimit());

        Button btnReset = (Button)findViewById(R.id.btnReset);
        btnReset.setOnClickListener((view) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure?");
            builder.setCancelable(true);

            builder.setPositiveButton(
                    "Continue",
                    (dialog, id) -> {
                        dialog.cancel();
                        DbHelper.clearHistory();
                        onBackPressed();
                    });

            builder.setNegativeButton(
                    "Cancel",
                    (dialog, id) -> dialog.cancel());

            builder.create().show();
        });
    }

    @Override
    public void onBackPressed() {
        CfgHelper.setLastBtDeviceAddress(getViewText(R.id.btnDevice));
        CfgHelper.setCellLow(Float.parseFloat(getViewText(R.id.etLowCellVoltage)));
        CfgHelper.setCellHigh(Float.parseFloat(getViewText(R.id.etHighCellVoltage)));
        CfgHelper.setBatterySeries(Integer.parseInt(getViewText(R.id.etBatterySeries)));
        CfgHelper.setConnectionTimeOut(Integer.parseInt(getViewText(R.id.etConnectionTimeOut)));
        CfgHelper.setSpeedCorr(Float.parseFloat(getViewText(R.id.etSpeedCorr)));
        CfgHelper.setSpeedLimit(Integer.parseInt(getViewText(R.id.etSpeedLimit)));

        finish();
    }

    private Handler getHandler() {
        return new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MessageConstants.MESSAGE_ERROR:
                        showMessage(msg.obj.toString());
                        break;

                    case MessageConstants.REQUEST_ENABLE_BT:
                        startActivityForResult((Intent)msg.obj, MessageConstants.REQUEST_ENABLE_BT);
                        break;
                }
            }
        };
    }
}
