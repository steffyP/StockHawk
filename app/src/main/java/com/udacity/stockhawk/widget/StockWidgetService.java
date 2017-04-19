package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.util.FormatterHelperUtil;
import com.udacity.stockhawk.util.PrefUtils;

/**
 * Created by stefanie on 18.04.17.
 */

public class StockWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }


    class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private Cursor mCursor;
        private Context mContext;
        @SuppressWarnings("unused")
        private int mAppWidgetId;


        public ListRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);


        }

        private void loadData() {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }

            // required to load data without exporting the content provider
            final long token = Binder.clearCallingIdentity();
            try {

                mCursor = mContext.getContentResolver().query(Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL);
            } finally {
                Binder.restoreCallingIdentity(token);
            }


        }

        @Override
        public void onCreate() {
            loadData();
        }

        @Override
        public void onDataSetChanged() {
            loadData();
        }

        @Override
        public void onDestroy() {
            if (mCursor == null) {
                mCursor.close();
                mCursor = null;
            }
        }

        @Override
        public int getCount() {
            if (mCursor == null) return 0;
            return mCursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION ||
                    mCursor == null || !mCursor.moveToPosition(position)) {
                return null;
            }

            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.item_widget);

            String stock = mCursor.getString(Contract.Quote.POSITION_SYMBOL);
            String price = FormatterHelperUtil.getInstance().formatDollarValue(mCursor.getFloat(Contract.Quote.POSITION_PRICE));

            float changeFloat = mCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);


            if (changeFloat > 0) {
                rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            } else {
                rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);

            }
            if (PrefUtils.getDisplayMode(mContext)
                    .equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
                String change = FormatterHelperUtil.getInstance().formatDollarValueWithPlus(changeFloat);
                rv.setTextViewText(R.id.change, change);
                rv.setContentDescription(R.id.change, mContext.getString(R.string.a11y_change_absolute, change));

            } else {
                float percentFloat = mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
                String changeInPercent = FormatterHelperUtil.getInstance().formatPercentageValue(percentFloat / 100);
                rv.setTextViewText(R.id.change, changeInPercent);
                rv.setContentDescription(R.id.change, mContext.getString(R.string.a11y_change_relative, changeInPercent));

            }
            rv.setTextViewText(R.id.symbol, stock);
            rv.setTextViewText(R.id.price, price);

            final Intent fillInIntent = new Intent();
            Uri stockUri = Contract.Quote.makeUriForStock(stock);
            fillInIntent.setData(stockUri);
            rv.setOnClickFillInIntent(R.id.widget_row, fillInIntent);
            // Return the remote views object.
            return rv;
        }


        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

//... include adapter-like methods here. See the StackView Widget sample.

    }


}
