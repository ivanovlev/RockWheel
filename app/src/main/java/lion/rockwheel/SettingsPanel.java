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
import android.widget.Toast;

import lion.rockwheel.bluetooth.BtDevice;
import lion.rockwheel.bluetooth.BtService;
import lion.rockwheel.helpers.CfgHelper;
import lion.rockwheel.helpers.DbHelper;

public class SettingsPanel extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_panel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CfgHelper cfgService = new CfgHelper(this);
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

        btnDevice.setText(cfgService.getLastBtDeviceAddress());

        EditText etLowCellVoltage = (EditText)findViewById(R.id.etLowCellVoltage);
        etLowCellVoltage.setText(String.valueOf(cfgService.getCellLow()));

        EditText etHighCellVoltage = (EditText)findViewById(R.id.etHighCellVoltage);
        etHighCellVoltage.setText(String.valueOf(cfgService.getCellHigh()));

        EditText etCellsCount = (EditText)findViewById(R.id.etBatterySeries);
        etCellsCount.setText(String.valueOf(cfgService.getBatterySeries()));

        Button btnReset = (Button)findViewById(R.id.btnReset);
        btnReset.setOnClickListener((view) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Shutdown wheel before continue!");
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
        CfgHelper cfgService = new CfgHelper(this);

        Button btnDevices = (Button) findViewById(R.id.btnDevice);
        String deviceAddress = btnDevices.getText().toString();
        cfgService.setLastBtDeviceAddress(deviceAddress);

        EditText etLowCellVoltage = (EditText)findViewById(R.id.etLowCellVoltage);
        String cellLow = etLowCellVoltage.getText().toString();
        cfgService.setCellLow(Float.parseFloat(cellLow));

        EditText etHighCellVoltage = (EditText)findViewById(R.id.etHighCellVoltage);
        String cellHigh = etHighCellVoltage.getText().toString();
        cfgService.setCellHigh(Float.parseFloat(cellHigh));

        EditText etBatterySeries = (EditText)findViewById(R.id.etBatterySeries);
        String batterySeries = etBatterySeries.getText().toString();
        cfgService.setBatterySeries(Integer.parseInt(batterySeries));

        finish();
    }

    private Handler getHandler() {
        return new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MessageConstants.MESSAGE_ERROR:
                        ShowMessage(msg.obj.toString());
                        break;

                    case MessageConstants.REQUEST_ENABLE_BT:
                        startActivityForResult((Intent)msg.obj, MessageConstants.REQUEST_ENABLE_BT);
                        break;
                }
            }
        };
    }

    private void ShowMessage(CharSequence text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
