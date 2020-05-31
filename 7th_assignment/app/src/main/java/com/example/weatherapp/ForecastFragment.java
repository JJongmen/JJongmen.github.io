package com.example.weatherapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;

import android.os.IBinder;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FORECAST_LOADER = 0;
//    private ArrayAdapter<String> adapter;
    private WeatherForecastAdapter adapter;
    private ArrayAdapter<String> detail_adapter;
    private IFetchWeatherService mService;
    private String[] detail_weather = new String[7];

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherColumns._ID,
            WeatherContract.WeatherColumns.COLUMN_DATE,
            WeatherContract.WeatherColumns.COLUMN_SHORT_DESC,
            WeatherContract.WeatherColumns.COLUMN_MAX_TEMP,
            WeatherContract.WeatherColumns.COLUMN_MIN_TEMP,
            WeatherContract.WeatherColumns.COLUMN_LONG_DESC,
            WeatherContract.WeatherColumns.COLUMN_MORNING_TEMP,
            WeatherContract.WeatherColumns.COLUMN_EVENING_TEMP,
            WeatherContract.WeatherColumns.COLUMN_NIGHT_TEMP
    };


    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_LONG_DESC = 5;
    static final int COL_WEATHER_MORNING_TEMP = 6;
    static final int COL_WEATHER_EVENING_TEMP = 7;
    static final int COL_WEATHER_NIGHT_TEMP = 8;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(FetchWeatherService.ACTION_RETRIEVE_WEATHER_DATA)) {
//                String[] data = intent.getStringArrayExtra(FetchWeatherService.EXTRA_WEATHER_DATA);
//                String[] data1 = intent.getStringArrayExtra(FetchWeatherService.EXTRA_DESCRIPTION_DATA);
//                String[] data2 = intent.getStringArrayExtra(FetchWeatherService.EXTRA_MORNING_DATA);
//                String[] data3 = intent.getStringArrayExtra(FetchWeatherService.EXTRA_EVENING_DATA);
//                String[] data4 = intent.getStringArrayExtra(FetchWeatherService.EXTRA_NIGHT_DATA);
//                adapter.clear();
//                for(String dayForecastStr : data) {
//                    adapter.add(dayForecastStr);
//                }
//                detail_adapter.clear();
//                for(int i = 0; i < 8; i++) {
//                    String detailFormat = new String(data1[i] + '\n' + data2[i] + '\n' + data3[i] + '\n' + data4[i] );
//                    detail_adapter.add(detailFormat);
//                }
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IFetchWeatherService.Stub.asInterface(service);

            try {
                mService.registerFetchDataListener(mFetchDataListener);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private IFetchDataListener.Stub mFetchDataListener = new IFetchDataListener.Stub() {
        @Override
        public void onWeatherDataRetrieved(String[] data) throws RemoteException {
//            String[] result_main = new String[8];
//            String[] result_description = new String[8];
//            String[] result_morning = new String[8];
//            String[] result_evening = new String[8];
//            String[] result_night = new String[8];
//            int i = 0;
//            for(String[] day : data) {
//                result_main[i] = day[0];
//                result_description[i] = day[1];
//                result_morning[i] = day[2];
//                result_evening[i] = day[3];
//                result_night[i] = day[4];
//                i += 1;
//            }
//            adapter.clear();
//            for(String dayForecastStr : data) {
//                adapter.add(dayForecastStr);
//            }
//            detail_adapter.clear();
//            for(int k = 0; k < 8; k++) {
//                String detailFormat = new String(result_description[k] + '\n' + result_morning[k] + '\n' + result_evening[k] + '\n' + result_night[k] );
//                detail_adapter.add(detailFormat);
//            }
        }
    } ;

    public ForecastFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        getActivity().bindService(new Intent(getActivity(), FetchWeatherService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        if (mService != null) {
            try {
                mService.unregisterFetchDataListener(mFetchDataListener);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        getActivity().unbindService(mServiceConnection);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.refresh:
                refreshWeatherData();
                break;

            case R.id.settings:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshWeatherData() {
//        Intent intent = new Intent(getActivity(), FetchWeatherService.class);
//        intent.setAction(FetchWeatherService.ACTION_RETRIEVE_WEATHER_DATA);
//        getActivity().startService(intent);
        if (mService != null) {
            try {
                mService.retrieveWeatherDataRetrieve();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        refreshWeatherData();
//        final LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
//        map.put("Sat 5/23 - Sunny - 31/17", "Average 26");
//        map.put("Sun 5/24 - Foggy - 21/8", "Average 16");
//        map.put("Mon 5/25 - Cloudy - 22/17", "Average 20");
//        map.put("Tue 5/26 - Rainy - 18/11", "Average 15");
//        map.put("Wed 5/27 - Foggy - 21/10", "Average 17");
//        map.put("Thurs 5/28 - TRAPPED IN WEATHERSTATION - 23/18", "Average 21");
//        map.put("Fri 5/29 - Sunny - 20/7", "Average 17");
//        List<String> weekForecast = new ArrayList<String>(map.keySet());

//        adapter =
//                new ArrayAdapter<String>(
//                        getActivity(), // The current context (this activity)
//                        R.layout.list_view_item, // The name of the layout ID.
//                        R.id.list_item_forecast_textview); // The ID of the textview to populate.
//
//        detail_adapter =
//                new ArrayAdapter<String>(
//                        getActivity(), // The current context (this activity)
//                        R.layout.list_view_item, // The name of the layout ID.
//                        R.id.list_item_forecast_textview); // The ID of the textview to populate.

        adapter = new WeatherForecastAdapter(getActivity(),null,0);

        View view = inflater.inflate(R.layout.fragment_forecast, container, false);
        ListView listView = view.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String detail_weather = new String();
                detail_weather += "Description Weather : \n" + adapter.getCursor().getString(COL_WEATHER_LONG_DESC) + "\n\n";
                detail_weather += "Morning Temperature : " + adapter.getCursor().getString(COL_WEATHER_MORNING_TEMP) + "\n";
                detail_weather += "Evening Temperature : " + adapter.getCursor().getString(COL_WEATHER_EVENING_TEMP) + "\n";
                detail_weather += "Night Temperature : " + adapter.getCursor().getString(COL_WEATHER_NIGHT_TEMP);

//                String forecast = adapter.getItem(position);
//                String detailForecast = detail_adapter.getItem(position);
//                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("data", detail_weather);
                startActivity(intent);
            }
        });
//        refreshWeatherData();
//        IntentFilter intentFilter = new IntentFilter(FetchWeatherService.ACTION_RETRIEVE_WEATHER_DATA);
//        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
        return view;
    }

    @Override
    public void onDestroyView() {
//        getActivity().unregisterReceiver(mBroadcastReceiver);
        super.onDestroyView();
    }

    private class WeatherForecastAdapter extends CursorAdapter {
        public class ViewHolder {
            public final TextView mTextView;
            public ViewHolder(View view) {
                mTextView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
            }
        }

        public WeatherForecastAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_view_item, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder)view.getTag();

            StringBuilder sb = new StringBuilder();
            long dateInMillis = cursor.getLong(COL_WEATHER_DATE);
            sb.append(getReadableDateString(dateInMillis));

            String description = cursor.getString(COL_WEATHER_DESC);
            sb.append(" - " + description);

            double high = cursor.getDouble(COL_WEATHER_MAX_TEMP);

            double low = cursor.getDouble(COL_WEATHER_MIN_TEMP);

            String highAndLow = formatHighLows(high, low);
            sb.append(" - " + highAndLow);

            String long_description = cursor.getString(COL_WEATHER_LONG_DESC);
            int morning = (int)cursor.getDouble(COL_WEATHER_MORNING_TEMP);
            int evening = (int)cursor.getDouble(COL_WEATHER_EVENING_TEMP);
            int night = (int)cursor.getDouble(COL_WEATHER_NIGHT_TEMP);

//            sb.append(" - " + long_description);
//            sb.append(" - " + morning);
//            sb.append(" - " + evening);
//            sb.append(" - " + night);
            holder.mTextView.setText(sb.toString());
//            TextView long_description_textview = (TextView)view.findViewById(R.id.detail_textview);
//            TextView morning_textview = (TextView)view.findViewById(R.id.detail_textview);
//            TextView evening_textview = (TextView)view.findViewById(R.id.detail_textview);
//            TextView night_textview = (TextView)view.findViewById(R.id.detail_textview);
//            long_description_textview.setText("3");
//            long_description_textview.setText(long_description);
//            morning_textview.setText(Integer.toString(morning));
//            evening_textview.setText(Integer.toString(evening));
//            night_textview.setText(Integer.toString(night));
        }
    }

    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherColumns.COLUMN_DATE + " ASC";

        long now = System.currentTimeMillis();
        long normalizedDate = normalizeDate(now);

        return new CursorLoader(getActivity(),
                WeatherContract.WeatherColumns.CONTENT_URI,
                FORECAST_COLUMNS,
                WeatherContract.WeatherColumns.COLUMN_DATE + " >= ?",
                new String[] {Long.toString(now)},
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}