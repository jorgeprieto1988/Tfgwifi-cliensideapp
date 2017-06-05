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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by troko on 01/06/2017.
 */

public class create_message extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_message);
        Button clickButton_mycoords = (Button) findViewById(R.id.bt_mi_pos);
        Button clickButton_enviar = (Button) findViewById(R.id.bt_enviar_msg);
        final TextView et_latitud = (TextView) findViewById(R.id.et_latitud);
        final TextView et_longitud = (TextView) findViewById(R.id.et_longitud);
        final TextView et_mensaje = (TextView) findViewById(R.id.et_mensaje);
        final TextView et_tiempo = (TextView) findViewById(R.id.et_tiempo);
        final TextView et_area = (TextView) findViewById(R.id.et_area);

        clickButton_enviar.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {


                try {
                    String contenido = URLEncoder.encode(et_mensaje.getText().toString(), "UTF-8");

                    String urljson = "http://tfg-wifi.appspot.com/setmessage?contenido=" + contenido +
                            "&area_mensaje=" + et_area.getText() +
                            "&tiempo_vida" + et_tiempo.getText() +
                            "&latitud=" + et_latitud.getText() + "&longitud=" + et_longitud.getText();

                    JsonArrayRequest jsarrayRequest = new JsonArrayRequest
                            (Request.Method.GET, urljson, null, new Response.Listener<JSONArray>() {

                                @Override
                                public void onResponse(JSONArray response) {
                                    et_mensaje.setText("Response: " + response.toString());
                                    //  lista_mensajes = new ArrayList<String>();

                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // TODO Auto-generated method stub
                                    et_mensaje.setText("Error:" + error.toString());
                                }
                            });
                    RequestQueue queue = Volley.newRequestQueue(create_message.this);
                    queue.add(jsarrayRequest);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            }
        });
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
