package com.udacity.stockhawk.ui;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.udacity.stockhawk.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by stefanie on 18.04.17.
 */

public class CustomMarkerView extends MarkerView {

    private final DecimalFormat dollarFormat;
    private TextView mValueTextView;
    private MPPointF mOffset;


    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        mValueTextView = (TextView) findViewById(R.id.tv_value);
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
// content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        mValueTextView.setText(dollarFormat.format(e.getY())); // set the entry-value as the display text
        super.refreshContent(e, highlight);
    }


    @Override
    public MPPointF getOffset() {

        if(mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }

        return mOffset;
    }

}
