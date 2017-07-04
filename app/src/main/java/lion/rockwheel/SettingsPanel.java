package lion.rockwheel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

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

        setViewInfo(R.id.btnDevice, CfgHelper.getLastBtDeviceAddress());
        setViewInfo(R.id.etLowCellVoltage, CfgHelper.getCellLow());
        setViewInfo(R.id.etHighCellVoltage, CfgHelper.getCellHigh());
        setViewInfo(R.id.etBatterySeries, CfgHelper.getBatterySeries());
        setViewInfo(R.id.etConnectionTimeOut, CfgHelper.getConnectionTimeOut());
        setViewInfo(R.id.etSpeedCorr, CfgHelper.getSpeedCorr());
        setViewInfo(R.id.etSpeedLimit, CfgHelper.getSpeedLimit());
        setViewInfo(R.id.cbSpeedAlert, CfgHelper.getSpeedAlert());

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
        CfgHelper.setLastBtDeviceAddress(getViewInfo(R.id.btnDevice));
        CfgHelper.setCellLow(Float.parseFloat(getViewInfo(R.id.etLowCellVoltage)));
        CfgHelper.setCellHigh(Float.parseFloat(getViewInfo(R.id.etHighCellVoltage)));
        CfgHelper.setBatterySeries(Integer.parseInt(getViewInfo(R.id.etBatterySeries)));
        CfgHelper.setConnectionTimeOut(Integer.parseInt(getViewInfo(R.id.etConnectionTimeOut)));
        CfgHelper.setSpeedCorr(Float.parseFloat(getViewInfo(R.id.etSpeedCorr)));
        CfgHelper.setSpeedLimit(Integer.parseInt(getViewInfo(R.id.etSpeedLimit)));
        CfgHelper.setSpeedAlert(Boolean.parseBoolean(getViewInfo(R.id.cbSpeedAlert)));

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
