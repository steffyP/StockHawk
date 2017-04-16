package com.udacity.stockhawk.ui;

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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class StockDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int DETAIL_LOADER = 1;
    private Uri mUri;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.chart)
    LineChart mLineChart;

    @BindView(R.id.change)
    TextView mChangeTextView;

    @BindView(R.id.price)
    TextView mPriceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.title_details));
        mUri = getIntent().getData();

        getSupportLoaderManager().restartLoader(DETAIL_LOADER, null, this);

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
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    StockDetailActivity.this,
                    mUri,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    null,
                    null,
                    null
            );
        } else {
            //TODO
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null && data.moveToFirst()){
            String stock = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
            getSupportActionBar().setTitle(getString(R.string.title_details, stock));
            final DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            final DecimalFormat decimalFormat =  new DecimalFormat("##.00");
            DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");

            mPriceTextView.setText(dollarFormat.format(data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PRICE))));

            double priceChange = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));

            if(priceChange > 0 ){
                mChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_green);
            } else {
                mChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_red);
            }
            mChangeTextView.setText(dollarFormatWithPlus.format(priceChange));

            Timber.d(data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY)));
            String historyString = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY));

            String[] historyArray = historyString.split("\n");

            List<Entry> entries = new ArrayList<Entry>();
            String dateString = "";
            String amountString = "";
            DateFormat formatter = new SimpleDateFormat("dd/MM/yy");
            int indexToSplit = 0;
            float xAxisPlaceholder = 1f;
            final List<String> dateLabelList = new ArrayList<>();
            String history = "";
            for (int i = historyArray.length-1; i >= 0; i--) {
                history = historyArray[i];
                // turn your data into Entry objects
                indexToSplit = history.indexOf(',');
                dateString = formatter.format(Long.parseLong(history.substring(0, indexToSplit)));
                dateLabelList.add(dateString);

                amountString = history.substring(indexToSplit+1, history.length());

                entries.add(new Entry(xAxisPlaceholder, Float.parseFloat(amountString)));
                xAxisPlaceholder++;
            }

            LineDataSet lineDataSet = new LineDataSet(entries, stock);
            //lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);


            // the labels that should be drawn on the XAxis

            IAxisValueFormatter xAxisFormatter = new IAxisValueFormatter() {

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    Timber.d(value+"");
                    if(value <= dateLabelList.size()) {
                        return dateLabelList.get((int) value - 1);
                    }
                    return "";
                }

            };


            IAxisValueFormatter axisFormatter = new IAxisValueFormatter() {

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return dollarFormat.format(value) ;
                }

            };


            IValueFormatter yAxisFormatter = new IValueFormatter() {

                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    return dollarFormat.format(value) ;
                }

            };

            IValueFormatter graphicFormatter = new IValueFormatter() {

                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    return decimalFormat.format(value) ;
                }

            };


            lineDataSet.setValueFormatter(graphicFormatter);

            lineDataSet.setCircleColor(getResources().getColor(android.R.color.transparent));
            lineDataSet.setColor(getResources().getColor(R.color.colorPrimary));
            lineDataSet.setCircleColorHole(getResources().getColor(android.R.color.white));
            lineDataSet.setValueTextColor(getResources().getColor(android.R.color.darker_gray));
        //    lineDataSet.setValueTextColor(R.color.colorAccent);


            XAxis xAxis = mLineChart.getXAxis();
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            xAxis.setValueFormatter(xAxisFormatter);
            xAxis.setTextColor(getResources().getColor(android.R.color.white));

            mLineChart.getAxisLeft().setTextColor(getResources().getColor(android.R.color.white));
            mLineChart.getAxisLeft().setValueFormatter(axisFormatter);

            mLineChart.getAxisRight().setDrawLabels(false);

            LineData lineData = new LineData(lineDataSet);
            mLineChart.getLegend().setEnabled(false);
            mLineChart.setData(lineData);
            mLineChart.setDescription(null);
            mLineChart.setContentDescription(getString(R.string.a11y_description_chart));
            mLineChart.setScaleYEnabled(false);
            mLineChart.setKeepPositionOnRotation(true);
            mLineChart.setAutoScaleMinMaxEnabled(true);
            mLineChart.invalidate();

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
