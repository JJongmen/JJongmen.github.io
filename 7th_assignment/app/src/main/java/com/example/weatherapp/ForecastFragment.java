package com.example.weatherapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    public ForecastFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        String[] data = {
//                "Sat 5/23 - Sunny - 31/17",
//                "Sun 5/24 - Foggy - 21/8",
//                "Mon 5/25 - Cloudy - 22/17",
//                "Tue 5/26 - Rainy - 18/11",
//                "Wed 5/27 - Foggy - 21/10",
//                "Thurs 5/28 - TRAPPED IN WEATHERSTATION - 23/18",
//                "Fri 5/29 - Sunny - 20/7"
//        };
//
//        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));
        final LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("Sat 5/23 - Sunny - 31/17","Average 26");
        map.put("Sun 5/24 - Foggy - 21/8","Average 16");
        map.put("Mon 5/25 - Cloudy - 22/17","Average 20");
        map.put("Tue 5/26 - Rainy - 18/11","Average 15");
        map.put("Wed 5/27 - Foggy - 21/10","Average 17");
        map.put("Thurs 5/28 - TRAPPED IN WEATHERSTATION - 23/18","Average 21");
        map.put("Fri 5/29 - Sunny - 20/7","Average 17");
        List<String> weekForecast = new ArrayList<String>(map.keySet());

        adapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_view_item, // The name of the layout ID.
                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
                        weekForecast);

        View view = inflater.inflate(R.layout.fragment_forecast, container, false);
        ListView listView = view.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String forecast = adapter.getItem(position);
                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("data", map.get(forecast));
                startActivity(intent);
            }
        });
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.execute();

        return view;
    }

    public class FetchWeatherTask extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                String baseUrl = "http://api.openweathermap.org/data/2.5/forecast?q=Ansan-si";
                String apiKey = "&appid=673bbd51c8c1c57cbc11a0ab9ea05826";
                URL url = new URL(baseUrl.concat(apiKey));

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }
    }
}
