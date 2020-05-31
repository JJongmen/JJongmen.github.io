package com.example.weatherapp;

interface IFetchDataListener {

    void onWeatherDataRetrieved(out String[] data);
}