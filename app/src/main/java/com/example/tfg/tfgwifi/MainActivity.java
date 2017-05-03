package com.example.tfg.tfgwifi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    String latitud = "";
    String longitud = "";

    ArrayList<String> lista_mensajes = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///////////////////////////////
        Button clickButton = (Button) findViewById(R.id.bt_consultar);
        final TextView texto = (TextView) findViewById(R.id.tv_content);
        textView = (TextView) findViewById(R.id.tv_content);
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        texto.setMovementMethod(new ScrollingMovementMethod());

        clickButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                texto.setText("");
                LocationManager locationManager = (LocationManager)
                        getSystemService(Context.LOCATION_SERVICE);
                boolean prueba = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                int mi_permiso = 1;
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            mi_permiso);
                }

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (locationGPS != null) {
                        latitud = locationGPS.getLatitude() + "";
                        longitud = locationGPS.getLongitude() + "";
                        texto.setText("Latitud: " + latitud + "\n" + "Longitud: " + longitud + "\n" + "using gps");

                    } else if (locationNet != null) {
                        latitud = locationNet.getLatitude() + "";
                        longitud = locationNet.getLongitude() + "";
                        texto.setText("Latitud: " + latitud + "\n" + "Longitud: " + longitud + "\n" + "using net");
                    } else {
                        texto.setText("No se ha podido establecer la posición");
                    }

                    if (!latitud.equals("") && !longitud.equals("")) {
                        //Ha desarrollar
                        String urljson = "http://tfg-wifi.appspot.com/getmessages?latitud=" + latitud + "&longitud=" + longitud;
                        JsonArrayRequest jsarrayRequest = new JsonArrayRequest
                                (Request.Method.GET, urljson, null, new Response.Listener<JSONArray>() {

                                    @Override
                                    public void onResponse(JSONArray response) {
                                        texto.setText("Response: " + response.toString());
                                        lista_mensajes = new ArrayList<String>();
                                        for (int i = 0; i < response.length(); i++) {
                                            try {
                                                lista_mensajes.add(response.getJSONObject(i).get("mensaje").toString());
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                                                android.R.layout.simple_list_item_1, lista_mensajes);

                                        ListView listView = (ListView) findViewById(R.id.lv_listamensajes);
                                        listView.setAdapter(adapter);

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

                } else {
                    int mi_permiso2 = 1;
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                mi_permiso2);
                    }
                }

            }


        });
    }

    private LocationManager locationManager;
    private TextView textView;
    private final LocationListener gpsLocationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            final String tvTxt = textView.getText().toString();
            switch (status) {
                case LocationProvider.AVAILABLE:
                    textView.setText(tvTxt + "GPS available again\n");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    textView.setText(tvTxt + "GPS out of service\n");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    textView.setText(tvTxt + "GPS temporarily unavailable\n");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            textView.setText(textView.getText().toString()
                    + "GPS Provider Enabled\n");
        }

        @Override
        public void onProviderDisabled(String provider) {
            textView.setText(textView.getText().toString()
                    + "GPS Provider Disabled\n");
        }

        @Override
        public void onLocationChanged(Location location) {
            locationManager.removeUpdates(networkLocationListener);
            textView.setText(textView.getText().toString()
                    + "New GPS location: "
                    + String.format("%9.6f", location.getLatitude()) + ", "
                    + String.format("%9.6f", location.getLongitude()) + "\n");
            String latitude = location.getLatitude() + "";
            String longitud = location.getLongitude() + "";
            if (!location.equals("") && !longitud.equals("")) {
                //Ha desarrollar
                String urljson = "http://tfg-wifi.appspot.com/getmessages?latitud=" + latitud + "&longitud=" + longitud;
                JsonArrayRequest jsarrayRequest = new JsonArrayRequest
                        (Request.Method.GET, urljson, null, new Response.Listener<JSONArray>() {

                            @Override
                            public void onResponse(JSONArray response) {
                                textView.setText("Response: " + response.toString());
                                lista_mensajes = new ArrayList<String>();
                                for (int i = 0; i < response.length(); i++) {
                                    try {
                                        lista_mensajes.add(response.getJSONObject(i).get("mensaje").toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                                        android.R.layout.simple_list_item_1, lista_mensajes);

                                ListView listView = (ListView) findViewById(R.id.lv_listamensajes);
                                listView.setAdapter(adapter);

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO Auto-generated method stub
                                textView.setText("Error:" + error.toString());
                            }
                        });

                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                queue.add(jsarrayRequest);
            }
        }
    };
    private final LocationListener networkLocationListener =
            new LocationListener() {

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    final String tvTxt = textView.getText().toString();
                    switch (status) {
                        case LocationProvider.AVAILABLE:
                            textView.setText(tvTxt + "Network location available again\n");
                            break;
                        case LocationProvider.OUT_OF_SERVICE:
                            textView.setText(tvTxt + "Network location out of service\n");
                            break;
                        case LocationProvider.TEMPORARILY_UNAVAILABLE:
                            textView.setText(tvTxt
                                    + "Network location temporarily unavailable\n");
                            break;
                    }
                }

                @Override
                public void onProviderEnabled(String provider) {
                    textView.setText(textView.getText().toString()
                            + "Network Provider Enabled\n");
                }

                @Override
                public void onProviderDisabled(String provider) {
                    textView.setText(textView.getText().toString()
                            + "Network Provider Disabled\n");
                }

                @Override
                public void onLocationChanged(Location location) {
                    textView.setText(textView.getText().toString()
                            + "New network location: "
                            + String.format("%9.6f", location.getLatitude()) + ", "
                            + String.format("%9.6f", location.getLongitude()) + "\n");
                    String latitud = location.getLatitude() + "";
                    String longitud = location.getLongitude() + "";
                    if (!latitud.equals("") && !longitud.equals("")) {
                        //Ha desarrollar
                        String urljson = "http://tfg-wifi.appspot.com/getmessages?latitud=" + latitud + "&longitud=" + longitud;
                        JsonArrayRequest jsarrayRequest = new JsonArrayRequest
                                (Request.Method.GET, urljson, null, new Response.Listener<JSONArray>() {

                                    @Override
                                    public void onResponse(JSONArray response) {
                                        textView.setText("Response: " + response.toString());
                                        lista_mensajes = new ArrayList<String>();
                                        for (int i = 0; i < response.length(); i++) {
                                            try {
                                                lista_mensajes.add(response.getJSONObject(i).get("mensaje").toString());
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                                                android.R.layout.simple_list_item_1, lista_mensajes);

                                        ListView listView = (ListView) findViewById(R.id.lv_listamensajes);
                                        listView.setAdapter(adapter);

                                    }
                                }, new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        // TODO Auto-generated method stub
                                        textView.setText("Error de aquí:" + error.networkResponse.toString());
                                    }
                                });

                        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                        queue.add(jsarrayRequest);
                    }
                }
            };

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 5000, 0,
                networkLocationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                3000, 0, gpsLocationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(networkLocationListener);
        locationManager.removeUpdates(gpsLocationListener);
    }
}
