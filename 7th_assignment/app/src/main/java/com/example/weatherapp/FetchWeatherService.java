package com.example.weatherapp;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class FetchWeatherService extends Service {
    public static final String ACTION_RETRIEVE_WEATHER_DATA = "com.example.weatherapp.RETRIEVE_DATA";
    public static final String EXTRA_WEATHER_DATA = "weather-data";
    public static final String EXTRA_DESCRIPTION_DATA = "description-data";
    public static final String EXTRA_MORNING_DATA = "morning-data";
    public static final String EXTRA_EVENING_DATA = "evening-data";
    public static final String EXTRA_NIGHT_DATA = "night-data";
    public FetchWeatherService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();
        if (action.equals(ACTION_RETRIEVE_WEATHER_DATA)) {
            retrieveWeatherData(startId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void retrieveWeatherData(int startId) {
        FetchWeatherTask weatherTask = new FetchWeatherTask(startId);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String cityId = prefs.getString("city", "37.5683,126.9778");
        String[] cityLocation = cityId.split(",");
        weatherTask.execute(cityLocation);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return new FetchWeatherServiceProxy(this);
    }

    public class FetchWeatherTask extends AsyncTask<String[], Void, String[][]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        private int mStartId = -1;

        public FetchWeatherTask(int startId) {
            Log.d(LOG_TAG, "asfdasdfafd", new RuntimeException());
            mStartId = startId;
        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[][] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_DAILY = "daily";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_MAIN = "main";
            final String OWM_DESCRIPTION = "description";
            final String OWM_MORNING = "morn";
            final String OWM_EVENING = "eve";
            final String OWM_NIGHT = "night";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_DAILY);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[][] resultStrs = new String[numDays][5];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String main;
                String highAndLow;
                String description;
//                String morning;
//                String evening;
//                String night;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                main = weatherObject.getString(OWM_MAIN);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX) - 273.15;
                double low = temperatureObject.getDouble(OWM_MIN) - 273.15;
                double morning = Math.round(temperatureObject.getDouble(OWM_MORNING) - 273.15);
                double evening = Math.round(temperatureObject.getDouble(OWM_EVENING) - 273.15);
                double night = Math.round(temperatureObject.getDouble(OWM_NIGHT) - 273.15);

                highAndLow = formatHighLows(high, low);
                resultStrs[i][0] = day + " - " + main + " - " + highAndLow;
                resultStrs[i][1] = "Description Weather : " + description;
                resultStrs[i][2] = "Morning Temperature : " + (int)morning;
                resultStrs[i][3] = "Evening Temperature : " + (int)evening;
                resultStrs[i][4] = "Night Temperature : " + (int)night;
            }

            for (String s : resultStrs[0]) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }

        @Override
        protected String[][] doInBackground(String[]... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 8;

            try {
//                 Construct the URL for the OpenWeatherMap query
//                 Possible parameters are avaiable at OWM's forecast API page, at
//                 http://openweathermap.org/API#forecast
//                final String FORECAST_BASE_URL =
//                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
//                final String QUERY_PARAM = "id";
//                final String FORMAT_PARAM = "mode";
//                final String UNITS_PARAM = "units";
//                final String DAYS_PARAM = "cnt";
//                final String APPID_PARAM = "APPID";
//
//                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
//                        .appendQueryParameter(QUERY_PARAM, params[0])
//                        .appendQueryParameter(FORMAT_PARAM, format)
//                        .appendQueryParameter(UNITS_PARAM, units)
//                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
//                        .appendQueryParameter(APPID_PARAM, "3d514a3884cbb2189afb41e7869fa918")
//                        .build();
//
//                URL url = new URL(builtUri.toString());
//                Log.v(LOG_TAG, "Built URI " + builtUri.toString());
                final String FORECAST_BASE_URL =
                        "https://api.openweathermap.org/data/2.5/onecall?";
                final String QUERY_PARAM = "lat";
                final String QUERY_PARAM2 = "lon";
                final String APPID_PARAM = "APPID";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0][0])
                        .appendQueryParameter(QUERY_PARAM2, params[0][1])
                        .appendQueryParameter(APPID_PARAM, "673bbd51c8c1c57cbc11a0ab9ea05826")
                        .build();

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

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

                Log.v(LOG_TAG, "Forecast string: " + forecastJsonStr);
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

            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(String[][] result) {
            if (result != null) {

                notifyWeatherDataRetrieved(result);

            }

            stopSelf(mStartId);
        }
    }

    private void notifyWeatherDataRetrieved(String[][] result) {
        Intent intent = new Intent(ACTION_RETRIEVE_WEATHER_DATA);
        int i = 0;
        String[] result_main = new String[8];
        String[] result_description = new String[8];
        String[] result_morning = new String[8];
        String[] result_evening = new String[8];
        String[] result_night = new String[8];
        for(String[] day : result) {
            result_main[i] = day[0];
            result_description[i] = day[1];
            result_morning[i] = day[2];
            result_evening[i] = day[3];
            result_night[i] = day[4];
            i += 1;
        }
        intent.putExtra(EXTRA_WEATHER_DATA, result_main);
        intent.putExtra(EXTRA_DESCRIPTION_DATA, result_description);
        intent.putExtra(EXTRA_MORNING_DATA , result_morning);
        intent.putExtra(EXTRA_EVENING_DATA , result_evening);
        intent.putExtra(EXTRA_NIGHT_DATA , result_night);
//        intent.putExtra(EXTRA_WEATHER_DATA, result[0]);
        sendBroadcast(intent);
    }

    private ArrayList<IFetchDataListener> mListeners = new ArrayList<IFetchDataListener>();
    private void registerFetchDataListener(IFetchDataListener listener) {
        synchronized (mListeners) {
            if (mListeners.contains(listener)) {
                return;
            }
            mListeners.add(listener);
        }
    }

    private void unregisterFetchDataListener(IFetchDataListener listener) {
        synchronized (mListeners) {
            if (!mListeners.contains(listener)) {
                return;
            }

            mListeners.remove(listener);
        }
    }

    private class FetchWeatherServiceProxy extends IFetchWeatherService.Stub {
        private WeakReference<FetchWeatherService> mService = null;

        public FetchWeatherServiceProxy(FetchWeatherService service) {
            mService = new WeakReference<FetchWeatherService>(service);
        }

        @Override
        public void retrieveWeatherData() throws RemoteException {
            mService.get().retrieveWeatherData(-1);
        }

        @Override
        public void registerFetchDataListener(IFetchDataListener listener) throws RemoteException {
            mService.get().registerFetchDataListener(listener);
        }

        @Override
        public void unregisterFetchDataListener(IFetchDataListener listener) throws RemoteException {
            mService.get().unregisterFetchDataListener(listener);
        }
    }

}