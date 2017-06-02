package com.example.tfg.tfgwifi;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by troko on 01/06/2017.
 */

public class create_message extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_message);
        Button clickButton_mycoords = (Button) findViewById(R.id.bt_mi_pos);
        final TextView et_latitud = (TextView) findViewById(R.id.et_latitud);
        final TextView et_longitud = (TextView) findViewById(R.id.et_longitud);
        clickButton_mycoords.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                LocationManager locationManager = (LocationManager)
                        getSystemService(Context.LOCATION_SERVICE);

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    String latitud = "";
                    String longitud = "";
                    Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(locationGPS != null)
                    {

                        et_latitud.setText(locationGPS.getLatitude() + "");
                        et_longitud.setText(locationGPS.getLongitude() + "");

                    }
                    else if(locationNet != null)
                    {

                        et_latitud.setText(locationNet.getLatitude() + "");
                        et_longitud.setText(locationNet.getLongitude() + "");
                    }
                    else
                    {
                        et_latitud.setText("No se ha podido establecer la posición, encienda el gps");
                    }

                }
                else{
                    int mi_permiso2 = 1;
                    if(ContextCompat.checkSelfPermission(create_message.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(create_message.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                mi_permiso2);
                    }
                }
            }
        });
    }
}
