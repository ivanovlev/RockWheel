package lion.rockwheel;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import lion.rockwheel.bluetooth.BtDeviceInfo;
import lion.rockwheel.bluetooth.BtService;
import lion.rockwheel.helpers.CfgHelper;
import lion.rockwheel.helpers.DbHelper;

public class DashPanel extends AppCompatActivity {
    int batterySeries = 0;
    float cellLow = 0;
    float cellHigh = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_panel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CfgHelper cfg = new CfgHelper(this);
        BtService btService = new BtService(getHandler());

        View fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            try {
                updateGui(cfg);

                String lastAddress = cfg.getLastBtDeviceAddress();
                if (lastAddress != null){
                    btService.listenDevice(lastAddress);
                }
            }
            catch (Exception e){
                showMessage(e.getMessage());
            }

        });

        fab.callOnClick();
    }

    private void updateGui(CfgHelper cfg) {
        batterySeries = cfg.getBatterySeries();
        cellLow = cfg.getCellLow();
        cellHigh = cfg.getCellHigh();
        TextView barVoltageHigh = (TextView) findViewById(R.id.barVoltageHigh);
        TextView barVoltageLow = (TextView) findViewById(R.id.barVoltageLow);
        DecimalFormat format = new DecimalFormat("#0.0");
        barVoltageHigh.setText(format.format(batterySeries * cellHigh));
        barVoltageLow.setText(format.format(batterySeries * cellLow));

        setTitle(String.format("RockWheel %1$ss", batterySeries));

        GraphView gvTrip = (GraphView)findViewById(R.id.gvTrip);
        gvTrip.removeAllSeries();
        gvTrip.addSeries(new LineGraphSeries());
        Viewport viewport = gvTrip.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setMaxY(40);
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
                        BtDeviceInfo info = (BtDeviceInfo)msg.obj;

                        tbInfo.setText(String.format("%1$s km\\h | %2$s km\\h | %3$s v | %4$s km",
                                                        info.speed,
                                                        info.maxSpeed,
                                                        info.voltage,
                                                        format.format(info.distance)));
                        updateChart();

                        float s = info.getSpeedPecent(30);
                        barSpeed.setProgress((int)s);
                        tbSpeed.setText(intFormat.format(info.speed));
                        tbSpeedPos.bottomMargin = (int)convertToPt(s);

                        float v = (info.getCellVoltage(batterySeries) - cellLow)/(cellHigh - cellLow) * 100;
                        barVoltage.setProgress((int)v);
                        tbVoltage.setText(intFormat.format(info.voltage));
                        tbVoltagePos.bottomMargin = (int)convertToPt(v);

                        setTitle(String.format("RockWheel %1$ss (Connected)", batterySeries));
                        break;

                    case MessageConstants.MESSAGE_ERROR:
                        tbInfo.setText("- | - | - | -");

                        tbSpeed.setText("-");
                        barSpeed.setProgress(0);
                        tbSpeedPos.bottomMargin = 0;

                        tbVoltage.setText("-");
                        barVoltage.setProgress(0);
                        tbVoltagePos.bottomMargin = 0;

                        setTitle(String.format("RockWheel %1$ss", batterySeries));

                        showMessage(msg.obj.toString());
                        break;

                    case MessageConstants.REQUEST_ENABLE_BT:
                        startActivityForResult((Intent)msg.obj, MessageConstants.REQUEST_ENABLE_BT);
                        break;
                }
            }
        };
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

    private void showMessage(CharSequence text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
