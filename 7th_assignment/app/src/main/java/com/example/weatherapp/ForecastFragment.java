package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

        return view;
    }
}
