package com.udacity.stockhawk.ui.stockdetails;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.util.FormatterHelperUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class StockDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER = 1;
    private Uri mUri;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.chart)
    LineChart mLineChart;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.stock)
    TextView mStockTextView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.change_percent)
    TextView mChangePercentTextView;

    @BindView(R.id.change)
    TextView mChangeTextView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.price)
    TextView mPriceTextView;

    private ArrayList<String> dateLabelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.title_details));
        mUri = getIntent().getData();

        getSupportLoaderManager().initLoader(DETAIL_LOADER, null, this);


        formatAxisOfChart();

        mLineChart.getLegend().setEnabled(false);
        mLineChart.setDescription(null);
        mLineChart.setContentDescription(getString(R.string.a11y_description_chart));
        mLineChart.setScaleYEnabled(false);
        mLineChart.setKeepPositionOnRotation(true);
        mLineChart.setAutoScaleMinMaxEnabled(true);

        mLineChart.setHighlightPerTapEnabled(true);

        // deprecated, but still easiest way and also recommended in the official documentation
        //noinspection deprecation
        mLineChart.setDrawMarkerViews(true);
        //noinspection deprecation
        mLineChart.setMarkerView(new CustomMarkerView(this));

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                // it could be that this view has been opened from the widget and MainActivity is not on the stack
                // then we will have to recreate the stack
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                            // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    this,
                    mUri,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    null,
                    null,
                    null
            );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            String stock = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
            mStockTextView.setText(stock);


            mPriceTextView.setText(FormatterHelperUtil.getInstance().formatDollarValue(data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PRICE))));

            double priceChange = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));

            if (priceChange > 0) {
                mChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_green);

            } else {
                mChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_red);

            }
            mChangeTextView.setText(FormatterHelperUtil.getInstance().formatDollarValueWithPlus((float) priceChange));
            mChangePercentTextView.setText(FormatterHelperUtil.getInstance().formatPercentageValue(data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE)) / 100));

            String historyString = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            Timber.d("history data: " + historyString);

            // if the string is empty, something went wrong with the request
            // probably the limit of requests has been reached
            if (historyString.isEmpty()) {
                return;
            }
            List<Entry> entries = createEntriesFromHistory(historyString);
            LineData lineData = new LineData(createFormattedLineDataSet(entries, stock));
            mLineChart.setData(lineData);

            mLineChart.invalidate();

        }
    }

    private void formatAxisOfChart() {
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Timber.d(value + "");
                if (value <= dateLabelList.size()) {
                    return dateLabelList.get((int) value - 1);
                }
                return "";
            }

        });
        xAxis.setTextColor(getResources().getColor(android.R.color.white));

        mLineChart.getAxisLeft().setTextColor(getResources().getColor(android.R.color.white));
        mLineChart.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return FormatterHelperUtil.getInstance().formatDollarValue(value) ;
            }

        });

        mLineChart.getAxisRight().setDrawLabels(false);

    }

    @SuppressWarnings("deprecation")
    private LineDataSet createFormattedLineDataSet(List<Entry> entries, String stock) {
        LineDataSet lineDataSet = new LineDataSet(entries, stock);


        lineDataSet.setValueFormatter(new IValueFormatter() {

            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return FormatterHelperUtil.getInstance().formatDecimalValue(value);
            }
        });

        lineDataSet.setCircleColor(getResources().getColor(android.R.color.transparent));
        lineDataSet.setColor(getResources().getColor(R.color.colorPrimary));
        lineDataSet.setCircleColorHole(getResources().getColor(android.R.color.white));
        lineDataSet.setValueTextColor(getResources().getColor(android.R.color.darker_gray));

        return lineDataSet;
    }

    private List<Entry> createEntriesFromHistory(String historyString) {
        String[] historyArray = historyString.split("\n");

        List<Entry> entries = new ArrayList<Entry>();
        String dateString;
        String amountString;
        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        int indexToSplit;
        float xAxisPlaceholder = 1f;
        dateLabelList = new ArrayList<>();
        String history;
        for (int i = historyArray.length - 1; i >= 0; i--) {
            history = historyArray[i];

            // turn your data into Entry objects
            indexToSplit = history.indexOf(',');
            dateString = formatter.format(Long.parseLong(history.substring(0, indexToSplit)));
            dateLabelList.add(dateString);

            amountString = history.substring(indexToSplit + 1, history.length());

            entries.add(new Entry(xAxisPlaceholder, Float.parseFloat(amountString)));
            xAxisPlaceholder++;
        }
        return entries;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
