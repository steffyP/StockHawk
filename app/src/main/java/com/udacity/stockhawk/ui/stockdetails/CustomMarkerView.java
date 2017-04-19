package com.udacity.stockhawk.ui.stockdetails;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.util.FormatterHelperUtil;

/**
 * Created by stefanie on 18.04.17.
 */

@SuppressLint("ViewConstructor")
public class CustomMarkerView extends MarkerView {

    private TextView mValueTextView;
    private MPPointF mOffset;


    public CustomMarkerView(Context context) {
        super(context, R.layout.view_custom_marker);
        mValueTextView = (TextView) findViewById(R.id.tv_value);

    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
// content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        mValueTextView.setText(FormatterHelperUtil.getInstance().formatDollarValue(e.getY())); // set the entry-value as the display text
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
