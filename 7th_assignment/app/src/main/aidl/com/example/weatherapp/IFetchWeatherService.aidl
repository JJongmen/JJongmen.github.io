// IFetchWeatherService.aidl
package com.example.weatherapp;

// Declare any non-default types here with import statements
import com.example.weatherapp.IFetchDataListener;

interface IFetchWeatherService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void retrieveWeatherDataRetrieve();
    void registerFetchDataListener(IFetchDataListener listener);
    void unregisterFetchDataListener(IFetchDataListener listener);
}
