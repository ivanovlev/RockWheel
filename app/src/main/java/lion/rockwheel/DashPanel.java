package lion.rockwheel;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import lion.rockwheel.model.DeviceInfo;
import lion.rockwheel.bluetooth.BtService;
import lion.rockwheel.helpers.CfgHelper;
import lion.rockwheel.helpers.DbHelper;

public class DashPanel extends BasePanel {
    int batterySeries = 0;
    float cellLow = 0;
    float cellHigh = 0;
    int speedLimit = 0;
    boolean alert = true;
    int alertCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_panel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BtService btService = new BtService(getHandler());

        View fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            try {
                updateGui();

                String lastAddress = CfgHelper.getLastBtDeviceAddress();

                if (!lastAddress.equals("")){
                    btService.listenDevice(lastAddress, CfgHelper.getConnectionTimeOut());
                }
            }
            catch (Exception e){
                showMessage(e.getMessage());
            }

        });

        fab.callOnClick();
    }

    private void updateGui() {
        batterySeries = CfgHelper.getBatterySeries();
        cellLow = CfgHelper.getCellLow();
        cellHigh = CfgHelper.getCellHigh();
        speedLimit = CfgHelper.getSpeedLimit();
        alert = CfgHelper.getSpeedAlert();

        DecimalFormat format = new DecimalFormat("#0.0");
        setViewInfo(R.id.barVoltageHigh, format.format(batterySeries * cellHigh));
        setViewInfo(R.id.barVoltageLow, format.format(batterySeries * cellLow));
        setViewInfo(R.id.barSpeedHigh, String.valueOf(speedLimit));

        updateHeader("");

        ImageView notify = (ImageView) findViewById(R.id.icoAlert);
        if (CfgHelper.getSpeedAlert()){
            notify.setImageResource(R.mipmap.ic_alert);
        }else {
            notify.setImageResource(R.mipmap.ic_silent);
        }

        GraphView gvTrip = (GraphView)findViewById(R.id.gvTrip);
        gvTrip.removeAllSeries();
        gvTrip.addSeries(new LineGraphSeries());
        Viewport viewport = gvTrip.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setMaxY(45);
        viewport.setMinY(0);

        updateChart();
    }

    private Handler getHandler() {
        TextView tbInfo = (TextView) findViewById(R.id.tbInfo);

        ProgressBar barSpeed= (ProgressBar) findViewById(R.id.barSpeed);
        TextView tbSpeed = (TextView) findViewById(R.id.tbSpeed);
        FrameLayout.LayoutParams tbSpeedPos = (FrameLayout.LayoutParams) findViewById(R.id.tbSpeedPos).getLayoutParams();

        ProgressBar barVoltage = (ProgressBar) findViewById(R.id.barVoltage);
        TextView tbVoltage = (TextView) findViewById(R.id.tbVoltage);
        FrameLayout.LayoutParams tbVoltagePos = (FrameLayout.LayoutParams) findViewById(R.id.tbVoltagePos).getLayoutParams();

        DecimalFormat format = new DecimalFormat("#0.00");
        DecimalFormat intFormat = new DecimalFormat("##");

        return new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MessageConstants.MESSAGE_READ:
                        DeviceInfo info = (DeviceInfo)msg.obj;

                        tbInfo.setText(String.format("%1$s km\\h | %2$s km\\h | %3$s v | %4$s km",
                                                        info.speed,
                                                        info.maxSpeed,
                                                        info.voltage,
                                                        format.format(info.distance)));
                        updateChart();

                        if(alert){
                            playAlert(info.speed);
                        }

                        float s = info.getSpeedPecent(speedLimit);
                        barSpeed.setProgress((int)s);
                        tbSpeed.setText(intFormat.format(info.speed));
                        tbSpeedPos.bottomMargin = (int)convertToPt(s);

                        float v = (info.getCellVoltage(batterySeries) - cellLow)/(cellHigh - cellLow) * 100;
                        barVoltage.setProgress((int)v);
                        tbVoltage.setText(intFormat.format(info.voltage));
                        tbVoltagePos.bottomMargin = (int)convertToPt(v);
                        break;

                    case MessageConstants.MESSAGE_ERROR:
                        tbInfo.setText("- | - | - | -");

                        tbSpeed.setText("-");
                        barSpeed.setProgress(0);
                        tbSpeedPos.bottomMargin = 0;

                        tbVoltage.setText("-");
                        barVoltage.setProgress(0);
                        tbVoltagePos.bottomMargin = 0;

                        showMessage(msg.obj.toString());
                        break;

                    case MessageConstants.CONNECTION_STATE:
                        updateHeader(msg.obj.toString());
                        break;

                    case MessageConstants.REQUEST_ENABLE_BT:
                        startActivityForResult((Intent)msg.obj, MessageConstants.REQUEST_ENABLE_BT);
                        break;
                }
            }
        };
    }

    private void updateHeader(String txt){
        StringBuilder header = new StringBuilder(String.format("RockWheel %1$ss", batterySeries));
        if (txt != ""){
            header.append(String.format(" (%1$s)", txt));
        }

        setTitle(header.toString());
    }

    private void updateChart(){
        GraphView gvTrip = (GraphView)findViewById(R.id.gvTrip);
        try {
            for (Series series : gvTrip.getSeries()) {
                ((LineGraphSeries)series).resetData(DbHelper.getHistory());
                gvTrip.getViewport().setMaxX(series.getHighestValueX());
                gvTrip.getViewport().setMinX(series.getLowestValueX());
            }
        }catch (Exception e){
            showMessage(e.getMessage());
        }
    }

    private float convertToPt(float sp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, sp, getResources().getDisplayMetrics());
    }

    private void playAlert(float speed){
        if (speed > speedLimit * 0.6){
            if(alertCounter == 0){
                int channel = AudioManager.STREAM_MUSIC;
                int volume = ToneGenerator.MAX_VOLUME;
                int template = ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD;
                new ToneGenerator(channel, volume).startTone(template, 200);
            }

            alertCounter++;

            if (speed > speedLimit * 0.8){
                if (alertCounter > 1){
                    alertCounter = 0;
                }
            }
            if (alertCounter > 5){
                alertCounter = 0;
            }
        }else {
            alertCounter = 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dash_panel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsPanel.class);
            startActivityForResult(intent, MessageConstants.CALLBACK);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        List<Integer> reconnect = Arrays.asList(
                MessageConstants.CALLBACK,
                MessageConstants.REQUEST_ENABLE_BT);

        if (reconnect.contains(requestCode)){
            findViewById(R.id.fab).callOnClick();
        }
    }
}
