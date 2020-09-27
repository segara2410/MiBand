package nodomain.freeyourgadget.gadgetbridge.activities.custom_charts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.HeartRateUtils;
import nodomain.freeyourgadget.gadgetbridge.activities.charts.AbstractChartFragment;
import nodomain.freeyourgadget.gadgetbridge.activities.charts.TimestampValueFormatter;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceService;

public class HeartRateActivity extends AppCompatActivity {
    private TextView mMaxHeartRateView;
    private TimestampTranslation tsTranslation;
    private LineDataSet mHeartRateSet;

    private int mHeartRate;
    private int mMaxHeartRate = 0;

    protected int BACKGROUND_COLOR;
    protected int DESCRIPTION_COLOR;
    protected int CHART_TEXT_COLOR;
    protected int HEARTRATE_COLOR;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case DeviceService.ACTION_REALTIME_SAMPLES: {
                    ActivitySample sample = (ActivitySample) intent.getSerializableExtra(DeviceService.EXTRA_REALTIME_SAMPLE);
                    addSample(sample);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filterLocal = new IntentFilter();
        filterLocal.addAction(DeviceService.ACTION_REALTIME_SAMPLES);
        tsTranslation = new TimestampTranslation();
        
        setContentView(R.layout.activity_heart_rate);
        
        mMaxHeartRateView = findViewById(R.id.max_heart_rate);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filterLocal);
    }

    private void addSample(ActivitySample sample) {
        int heartRate = sample.getHeartRate();
        int timestamp = tsTranslation.shorten(sample.getTimestamp());
        if (HeartRateUtils.getInstance().isValidHeartRateValue(heartRate)) {
            setCurrentHeartRate(heartRate, timestamp);
        }
    }

    private void enableRealtimeTracking(boolean enable) {

    }

    private void setCurrentHeartRate(int heartRate, int timestamp) {
        mHeartRate = heartRate;
        if (mMaxHeartRate < mHeartRate) {
            mMaxHeartRate = mHeartRate;
        }
        mMaxHeartRateView.setText(getString(R.string.live_activity_max_heart_rate, heartRate, mMaxHeartRate));
    }

    private int getCurrentHeartRate() {
        int result = mHeartRate;
        mHeartRate = -1;
        return result;
    }

    private void addEntries(int timestamp) {
        int hr = getCurrentHeartRate();
        if (hr > HeartRateUtils.getInstance().getMinHeartRate()) {
            mHeartRateSet.addEntry(new Entry(timestamp, hr));
        }
    }

    protected LineDataSet createHeartrateSet(List<Entry> values, String label) {
        LineDataSet set1 = new LineDataSet(values, label);
        set1.setLineWidth(2.2f);
        set1.setColor(HEARTRATE_COLOR);
//        set1.setDrawCubic(true);
        set1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set1.setCubicIntensity(0.1f);
        set1.setDrawCircles(false);
//        set1.setCircleRadius(2f);
//        set1.setDrawFilled(true);
//        set1.setColor(getResources().getColor(android.R.color.background_light));
//        set1.setCircleColor(HEARTRATE_COLOR);
//        set1.setFillColor(ColorTemplate.getHoloBlue());
//        set1.setHighLightColor(Color.rgb(128, 0, 255));
//        set1.setColor(Color.rgb(89, 178, 44));
        set1.setDrawValues(true);
        set1.setValueTextColor(CHART_TEXT_COLOR);
        set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
        return set1;
    }

    protected void setupLegend(Chart chart) {
        // no legend
    }

    protected void configureChartDefaults(Chart<?> chart) {
        chart.getXAxis().setValueFormatter(new TimestampValueFormatter());
        chart.getDescription().setText("");

        // if enabled, the chart will always start at zero on the y-axis
        chart.setNoDataText(getString(R.string.chart_no_data_synchronize));

        // disable value highlighting
        chart.setHighlightPerTapEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

// commented out: this has weird bugs/side-effects at least on WeekStepsCharts
// where only the first Day-label is drawn, because AxisRenderer.computeAxisValues(float,float)
// appears to have an overflow when calculating 'n' (number of entries)
//        chart.getXAxis().setGranularity(60*5);

        setupLegend(chart);
    }

    protected void configureBarLineChartDefaults(BarLineChartBase<?> chart) {
        configureChartDefaults(chart);
        if (chart instanceof BarChart) {
            ((BarChart) chart).setFitBars(true);
        }

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
//        chart.setPinchZoom(true);

        chart.setDrawGridBackground(false);
    }

    private void setupHeartChart(BarLineChartBase chart) {
        configureBarLineChartDefaults(chart);

        chart.setTouchEnabled(false); // no zooming or anything, because it's updated all the time
        chart.setBackgroundColor(BACKGROUND_COLOR);
        chart.getDescription().setTextColor(DESCRIPTION_COLOR);
        chart.getDescription().setText(getString(R.string.live_activity_steps_per_minute_history));
        chart.setNoDataText(getString(R.string.live_activity_start_your_activity));
        chart.getLegend().setEnabled(false);
        Paint infoPaint = chart.getPaint(Chart.PAINT_INFO);
        infoPaint.setTextSize(Utils.convertDpToPixel(20f));
        infoPaint.setFakeBoldText(true);
        chart.setPaint(infoPaint, Chart.PAINT_INFO);

        XAxis x = chart.getXAxis();
        x.setDrawLabels(true);
        x.setDrawGridLines(false);
        x.setEnabled(true);
        x.setTextColor(CHART_TEXT_COLOR);
        x.setValueFormatter(new SampleXLabelFormatter(tsTranslation));
        x.setDrawLimitLinesBehindData(true);

        YAxis y = chart.getAxisLeft();
        y.setDrawGridLines(false);
        y.setDrawTopYLabelEntry(false);
        y.setTextColor(CHART_TEXT_COLOR);
        y.setEnabled(true);
        y.setAxisMinimum(0);

        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setDrawGridLines(false);
        yAxisRight.setEnabled(true);
        yAxisRight.setDrawLabels(true);
        yAxisRight.setDrawTopYLabelEntry(false);
        yAxisRight.setTextColor(CHART_TEXT_COLOR);

        mHeartRateSet = createHeartrateSet(new ArrayList<Entry>(), getString(R.string.live_activity_heart_rate));
        mHeartRateSet.setDrawValues(false);
    }

    protected static class SampleXLabelFormatter extends ValueFormatter {
        private final TimestampTranslation tsTranslation;
        SimpleDateFormat annotationDateFormat = new SimpleDateFormat("HH:mm");
        //        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Calendar cal = GregorianCalendar.getInstance();

        public SampleXLabelFormatter(TimestampTranslation tsTranslation) {
            this.tsTranslation = tsTranslation;

        }
        // TODO: this does not work. Cannot use precomputed labels
        @Override
        public String getFormattedValue(float value) {
            cal.clear();
            int ts = (int) value;
            cal.setTimeInMillis(tsTranslation.toOriginalValue(ts) * 1000L);
            Date date = cal.getTime();
            String dateString = annotationDateFormat.format(date);
            return dateString;
        }
    }

    protected static class TimestampTranslation {
        private int tsOffset = -1;

        public int shorten(int timestamp) {
            if (tsOffset == -1) {
                tsOffset = timestamp;
                return 0;
            }
            return timestamp - tsOffset;
        }

        public int toOriginalValue(int timestamp) {
            if (tsOffset == -1) {
                return timestamp;
            }
            return timestamp + tsOffset;
        }
    }
}