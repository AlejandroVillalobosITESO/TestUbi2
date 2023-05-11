package com.example.testubi2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ubidots.ApiClient;
import com.ubidots.Value;
import com.ubidots.Variable;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private GoogleMap mMap;
    private MapView mMapView;
    private Handler mHandler;

    private TextView mHeartRateTextView;

    private static final String API_KEY = "[API_KEY]";
    private static final String HEART_RATE_VARIABLE_ID = "[HEART_RATE_VARIABLE_ID]";
    private static final String LOCATION_VARIABLE_ID = "[LOCATION_VARIABLE_ID]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHeartRateTextView = findViewById(R.id.heart_rate_text_view);

        mHandler = new Handler(Looper.getMainLooper());
        startRepeatingTask();
        initGoogleMap(savedInstanceState);
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        mMapView = findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateMapLocation();
    }

    private void updateMapLocation() {
        ApiClient apiClient = new ApiClient(API_KEY);
        Variable locationVariable = apiClient.getVariable(LOCATION_VARIABLE_ID);
        locationVariable.getValues(new ApiCallback<List<Value>>() {
            @Override
            public void onResponse(List<Value> values) {
                if (values != null && values.size() > 0) {
                    Value value = values.get(0);
                    double latitude = value.getLatitude();
                    double longitude = value.getLongitude();
                    LatLng latLng = new LatLng(latitude, longitude);
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                    mMap.clear();
                    mMap.addMarker(markerOptions);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            }

            @Override
            public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                Toast.makeText(MainActivity.this, "Failed to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                updateHeartRate();
            } finally {
                mHandler.postDelayed(mStatusChecker, 5000);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    private void updateHeartRate() {
        ApiClient apiClient = new ApiClient(API_KEY);
        Variable heartRateVariable = apiClient.getVariable(HEART_RATE_VARIABLE_ID);
        heartRateVariable.getValues(new ApiCallback<List<Value>>() {
            @Override
            public void onResponse(List<Value> values) {
                if (values != null && values.size() > 0) {
                    Value value = values.get(0);
                    double heartRate = value.getValue();
                    mHeartRateTextView.setText(String.format(Locale.getDefault(), "%.0f", heartRate));
                }
            }

            @Override
            public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                Toast.makeText(MainActivity.this, "Failed to get heart rate", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }
}