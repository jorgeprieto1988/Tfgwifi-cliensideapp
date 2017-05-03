package com.example.tfg.tfgwifi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    String latitud = "";
    String longitud = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///////////////////////////////
        Button clickButton = (Button) findViewById(R.id.bt_consultar);
        final TextView texto = (TextView) findViewById(R.id.tv_content);
        texto.setMovementMethod(new ScrollingMovementMethod());

        clickButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                texto.setText("");
                LocationManager locationManager = (LocationManager)
                        getSystemService(Context.LOCATION_SERVICE);
                boolean prueba = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                int mi_permiso = 1;
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            mi_permiso);
                }

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(locationGPS != null)
                    {
                        latitud = locationGPS.getLatitude() + "";
                        longitud = locationGPS.getLongitude() + "";
                        texto.setText("Latitud: " + latitud +  "\n" + "Longitud: " + longitud + "\n" +"using gps" );

                    }
                    else if(locationNet != null)
                    {
                        latitud = locationNet.getLatitude() + "";
                        longitud = locationNet.getLongitude() + "";
                        texto.setText("Latitud: " + latitud +  "\n" + "Longitud: " + longitud + "\n" +"using net" );
                    }
                    else
                    {
                        texto.setText("No se ha podido establecer la posición");
                    }

                    if(!latitud.equals("") && !longitud.equals(""))
                    {
                        //Ha desarrollar
                        String urljson = "http://tfg-wifi.appspot.com/getmessages?latitud=" + latitud + "&longitud=" + longitud;
                        JsonArrayRequest jsarrayRequest = new JsonArrayRequest
                                (Request.Method.GET, urljson, null, new Response.Listener<JSONArray>() {

                                    @Override
                                    public void onResponse(JSONArray response) {
                                        texto.setText("Response: " + response.toString());
                                    }
                                }, new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        // TODO Auto-generated method stub
                                        texto.setText("Error:" + error.toString());
                                    }
                                });

                        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                        queue.add(jsarrayRequest);
                    }

                }
                else{
                    int mi_permiso2 = 1;
                    if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                mi_permiso2);
                    }
                }

            }


        });
    }
}
