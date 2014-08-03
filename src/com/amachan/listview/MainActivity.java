
package com.amachan.listview;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amachan.list.SwipeableListView;
import com.amachan.pinnedWidget.PinnedHeaderListAdapter;

public class MainActivity extends Activity {

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createListView();
    }

    private void createListView() {
        final TestPinnedHeaderListAdapter adapter = new TestPinnedHeaderListAdapter(this);
        String[] names = {
                "Asia", "Africa", "Europe", "North America"
        };
        adapter.setHeaderTopEnabled(true);
        adapter.setHeaderTopKeepOne(false);
        adapter.setHeaderBottomKeepOne(true);
        adapter.setPinnedPartitionHeadersEnabled(true);
        adapter.setHeaders(names);
        for (int i = 0; i < names.length; i++) {
            adapter.addPartition(true, names[i] != null);
            adapter.mPinnedHeaderCount = names.length;
        }

        SwipeableListView pn = (SwipeableListView) findViewById(R.id.main_listView);
        pn.setAdapter(adapter);

        ArrayList<String> asianCountries = new ArrayList<String>();
        asianCountries.add("India");
        asianCountries.add("Singapore");
        asianCountries.add("China");
        asianCountries.add("Japan");
        asianCountries.add("Korea");
        asianCountries.add("Malaysia");
        ArrayList<String> africanCountries = new ArrayList<String>();
        africanCountries.add("South Africa");
        africanCountries.add("Nigeria");
        africanCountries.add("Kenya");
        ArrayList<String> europeanCountries = new ArrayList<String>();
        europeanCountries.add("England");
        europeanCountries.add("France");
        europeanCountries.add("Germany");
        europeanCountries.add("Spain");
        europeanCountries.add("Italy");
        europeanCountries.add("Russia");
        ArrayList<String> americanCountries = new ArrayList<String>();
        americanCountries.add("US");
        americanCountries.add("Canada");
        americanCountries.add("Mexico");
        final Cursor cursor = makeCursor(asianCountries);
        mHandler.postDelayed(new Runnable() {

            public void run() {
                adapter.changeCursor(0, cursor);

            }
        }, 500);

        final Cursor cursor2 = makeCursor(africanCountries);
        mHandler.postDelayed(new Runnable() {

            public void run() {
                adapter.changeCursor(1, cursor2);
            }
        }, 500);

        final Cursor cursor3 = makeCursor(europeanCountries);
        mHandler.postDelayed(new Runnable() {

            public void run() {
                adapter.changeCursor(2, cursor3);
            }
        }, 500);

        final Cursor cursor4 = makeCursor(americanCountries);
        mHandler.postDelayed(new Runnable() {

            public void run() {
                adapter.changeCursor(3, cursor4);
            }
        }, 500);
    }

    private Cursor makeCursor(ArrayList<String> countries) {
        MatrixCursor cursor = new MatrixCursor(new String[] {
                "_id", "name", "unread"
        });

        for (int i = 0; i < countries.size(); i++) {
            cursor.addRow(new Object[] {
                    i, countries.get(i), 0
            });
        }
        return cursor;
    }

    public class TestPinnedHeaderListAdapter extends PinnedHeaderListAdapter {

        public TestPinnedHeaderListAdapter(Context context) {
            super(context);
            setPinnedPartitionHeadersEnabled(true);
        }

        private String[] mHeaders;
        private int mPinnedHeaderCount;

        public void setHeaders(String[] headers) {
            this.mHeaders = headers;
        }

        @Override
        protected View newHeaderView(Context context, int partition, Cursor cursor,
                ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(R.layout.list_section, null);
        }

        @Override
        protected void bindHeaderView(View view, int parition, Cursor cursor) {
            TextView headerText = (TextView) view.findViewById(R.id.header_text);
            headerText.setText(mHeaders[parition]);
        }

        @Override
        protected View newView(Context context, int partition, Cursor cursor, int position,
                ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(R.layout.list_item, null);
        }

        @Override
        protected void bindView(View v, int partition, Cursor cursor, int position) {
            TextView name = (TextView) v.findViewById(R.id.name);
            String country = cursor.getString(1);
            name.setText(country);
        }

        @Override
        public View getPinnedHeaderView(int viewIndex, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.list_section, parent, false);
            view.setFocusable(false);
            view.setEnabled(false);
            bindHeaderView(view, viewIndex, null);
            return view;
        }

        @Override
        public int getPinnedHeaderCount() {
            return mPinnedHeaderCount;
        }

    }

}
