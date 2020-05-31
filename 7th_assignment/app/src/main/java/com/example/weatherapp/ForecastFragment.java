package com.example.weatherapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import android.widget.ListView;
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
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> adapter;
    private ArrayAdapter<String> detail_adapter;
    private IFetchWeatherService mService;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(FetchWeatherService.ACTION_RETRIEVE_WEATHER_DATA)) {
                String[] data = intent.getStringArrayExtra(FetchWeatherService.EXTRA_WEATHER_DATA);
                String[] data1 = intent.getStringArrayExtra(FetchWeatherService.EXTRA_DESCRIPTION_DATA);
                String[] data2 = intent.getStringArrayExtra(FetchWeatherService.EXTRA_MORNING_DATA);
                String[] data3 = intent.getStringArrayExtra(FetchWeatherService.EXTRA_EVENING_DATA);
                String[] data4 = intent.getStringArrayExtra(FetchWeatherService.EXTRA_NIGHT_DATA);
                adapter.clear();
//                for(String dayForecastStr : data) {
//                    adapter.add(dayForecastStr);
//                }
                detail_adapter.clear();
                for(int i = 0; i < 8; i++) {
                    String detailFormat = new String(data1[i] + '\n' + data2[i] + '\n' + data3[i] + '\n' + data4[i] );
                    detail_adapter.add(detailFormat);
                }
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
            adapter.clear();
            for(String dayForecastStr : data) {
                adapter.add(dayForecastStr);
            }
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

        adapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_view_item, // The name of the layout ID.
                        R.id.list_item_forecast_textview); // The ID of the textview to populate.

        detail_adapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_view_item, // The name of the layout ID.
                        R.id.list_item_forecast_textview); // The ID of the textview to populate.


        View view = inflater.inflate(R.layout.fragment_forecast, container, false);
        ListView listView = view.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String forecast = adapter.getItem(position);
//                String detailForecast = detail_adapter.getItem(position);
                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), DetailActivity.class);
//                intent.putExtra("data", detailForecast);
                startActivity(intent);
            }
        });
        refreshWeatherData();
//        IntentFilter intentFilter = new IntentFilter(FetchWeatherService.ACTION_RETRIEVE_WEATHER_DATA);
//        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
        return view;
    }

    @Override
    public void onDestroyView() {
//        getActivity().unregisterReceiver(mBroadcastReceiver);
        super.onDestroyView();
    }
}