// IFetchDataListener.aidl
package com.example.weatherapp;

// Declare any non-default types here with import statements

interface IFetchDataListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onWeatherDataRetrieved(out String[] data);
}
