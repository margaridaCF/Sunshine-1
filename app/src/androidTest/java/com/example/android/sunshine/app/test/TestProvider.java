/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app.test;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.app.data.WeatherDbHelper;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();
    public static String TEST_LOCATION = "99705";
    public static String TEST_DATE = "20141205";

    /*public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        Log.v(LOG_TAG, "testDeleteDb");
    }*/
    public void testDeleteAllRecordsStart(){
        deleteAllRecords();
    }

    public void testInsertReadProvider() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);

        ContentValues testValues = TestDb.createNorthPoleLocationValues();

        long locationRowId;
        locationRowId = insertNorthPole(testValues);
        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = TestDb.createWeatherValues(locationRowId);
        insertWeather(weatherValues);
        tQueryLocationId(testValues, locationRowId);
        tQueryLocation(testValues);
        tQueryJoin(testValues, weatherValues);
        tQueryLocationSettingAndDate(weatherValues);
        dbHelper.close();
    }

    private void tQueryJoin(ContentValues testValues, ContentValues weatherValues) {
        //Log.v(LOG_TAG, "tQueryJoin");
        // Test Joins
        // Add the location values in with the weather data so that we can make
        // sure that the join worked and we actually get all the values back
        addAllContentValues(weatherValues, testValues);

        // Get the joined Weather and Location data
        ContentResolver contentResolver = mContext.getContentResolver();
        assertNotNull(contentResolver);
        Uri uriWeatherLocation = WeatherEntry.buildWeatherLocation(TEST_LOCATION);
        assertNotNull(uriWeatherLocation);
        Cursor weatherCursor = contentResolver.query(
                uriWeatherLocation,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestDb.validateCursor(weatherCursor, weatherValues);

        // Get the joined Weather and Location data with a start date
        Uri uriWeatherLocationWithStartDate = WeatherEntry.buildWeatherLocationWithStartDate(
                TEST_LOCATION, TEST_DATE);
        assertNotNull(uriWeatherLocationWithStartDate);
        weatherCursor = contentResolver.query(
                uriWeatherLocationWithStartDate,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestDb.validateCursor(weatherCursor, weatherValues);
    }

    private void tQueryLocationSettingAndDate(ContentValues weatherValues){
        //Log.v(LOG_TAG, "tQueryLocationSettingAndDate");
        ContentResolver contentResolver = mContext.getContentResolver();
        assertNotNull(contentResolver);
        Uri uriWeatherLocation = WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION, TEST_DATE);
        assertNotNull(uriWeatherLocation);
        Cursor weatherCursor = contentResolver.query(
                uriWeatherLocation,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestDb.validateCursor(weatherCursor, weatherValues);
    }

    private void tQueryLocation(ContentValues testValues) {
        //Log.v(LOG_TAG, "tQueryLocation");
        // Test location
        // A cursor is your primary interface to the query results.
        Cursor cursor;
        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );
        TestDb.validateCursor(cursor, testValues);
        cursor.close();
    }

    private void tQueryLocationId(ContentValues testValues, long locationRowId) {
        //Log.v(LOG_TAG, "tQueryLocationId");
        Cursor cursor;// Test LocationId
        //Uri locationId = Uri.withAppendedPath(LocationEntry.CONTENT_URI, "/" + locationRowId);
        cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );
        //Log.v(LOG_TAG, "locationRowId:"+locationRowId);
        StringBuilder row = new StringBuilder();
        cursor.moveToFirst();
        DatabaseUtils.dumpCurrentRow(cursor, row);
        //Log.v(LOG_TAG, row.toString());
        TestDb.validateCursor(cursor, testValues);
        cursor.close();
    }

    private void insertWeather(ContentValues weatherValues) {
        Log.v(LOG_TAG, "insertWeather");
        Uri uri = mContext.getContentResolver().insert(
                WeatherEntry.CONTENT_URI,  weatherValues);
        //long weatherRowId = ContentUris.parseId(uri);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestDb.validateCursor(weatherCursor, weatherValues);
        weatherCursor.close();
    }

    private long insertNorthPole(ContentValues testValues) {
        Log.v(LOG_TAG, "insertNorthPole");
        long locationRowId;
        Uri insertUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);
        assertNotNull(insertUri);
        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.
        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestDb.validateCursor(cursor, testValues);
        cursor.close();
        return ContentUris.parseId(insertUri);
    }

    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(TEST_LOCATION));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION, TEST_DATE));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    // The target api annotation is needed for the call to keySet -- we wouldn't want
    // to use this in our app, but in a test it's fine to assume a higher target.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void addAllContentValues(ContentValues destination, ContentValues source) {
        for (String key : source.keySet()) {
            destination.put(key, source.getAsString(key));
        }
    }

    // brings our database to an empty state
    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    public void setUp() {
        Log.v(LOG_TAG, "setUp");
        deleteAllRecords();
    }

    static final String KALAMAZOO_LOCATION_SETTING = "kalamazoo";
    static final String KALAMAZOO_WEATHER_START_DATE = "20140625";

    long locationRowId;

    static ContentValues createKalamazooWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, KALAMAZOO_WEATHER_START_DATE);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.5);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 85);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 35);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Cats and Dogs");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 3.4);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 42);

        return weatherValues;
    }

    static ContentValues createKalamazooLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(LocationEntry.COLUMN_LOCATION_SETTING, KALAMAZOO_LOCATION_SETTING);
        testValues.put(LocationEntry.COLUMN_CITY_NAME, "Kalamazoo");
        testValues.put(LocationEntry.COLUMN_COORD_LAT, 42.2917);
        testValues.put(LocationEntry.COLUMN_COORD_LONG, -85.5872);

        return testValues;
    }


    // Inserts both the location and weather data for the Kalamazoo data set.
    public void insertKalamazooData() {
        ContentValues kalamazooLocationValues = createKalamazooLocationValues();
        Uri locationInsertUri = mContext.getContentResolver()
                .insert(LocationEntry.CONTENT_URI, kalamazooLocationValues);
        assertTrue(locationInsertUri != null);

        locationRowId = ContentUris.parseId(locationInsertUri);

        ContentValues kalamazooWeatherValues = createKalamazooWeatherValues(locationRowId);
        Uri weatherInsertUri = mContext.getContentResolver()
                .insert(WeatherEntry.CONTENT_URI, kalamazooWeatherValues);
        assertTrue(weatherInsertUri != null);
    }

    public void testUpdateAndReadWeather() {
        insertKalamazooData();
        String newDescription = "Cats and Frogs (don't warn the tadpoles!)";

        // Make an update to one value.
        ContentValues kalamazooUpdate = new ContentValues();
        kalamazooUpdate.put(WeatherEntry.COLUMN_SHORT_DESC, newDescription);

        mContext.getContentResolver().update(
                WeatherEntry.CONTENT_URI, kalamazooUpdate, null, null);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make the same update to the full ContentValues for comparison.
        ContentValues kalamazooAltered = createKalamazooWeatherValues(locationRowId);
        kalamazooAltered.put(WeatherEntry.COLUMN_SHORT_DESC, newDescription);

        TestDb.validateCursor(weatherCursor, kalamazooAltered);
    }

    public void testRemoveHumidityAndReadWeather() {
        insertKalamazooData();

        mContext.getContentResolver().delete(WeatherEntry.CONTENT_URI,
                WeatherEntry.COLUMN_HUMIDITY + " = " + locationRowId, null);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make the same update to the full ContentValues for comparison.
        ContentValues kalamazooAltered = createKalamazooWeatherValues(locationRowId);
        kalamazooAltered.remove(WeatherEntry.COLUMN_HUMIDITY);

        TestDb.validateCursor(weatherCursor, kalamazooAltered);
    }

    public void testDeleteAllRecordsEnd(){
        deleteAllRecords();
    }
}
