package com.example.tfg.tfgwifi;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.support.annotation.RequiresApi;
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
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.attr.port;

public class MainActivity extends AppCompatActivity {
    String latitud = "";
    String longitud = "";
    private static final String TAG = "MainActivity";
    ArrayList<String> lista_mensajes = new ArrayList<String>();
    private final IntentFilter intentFilter = new IntentFilter();
    private final WifiP2pConfig config = new WifiP2pConfig();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ////CONECT P2P/////
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        ///////////////////////////////
        Button clickButton = (Button) findViewById(R.id.bt_consultar);
        Button clickButtonCompartir = (Button) findViewById(R.id.bt_share);
        Button clickButtonBuscar = (Button) findViewById(R.id.bt_buscar);
        final TextView texto = (TextView) findViewById(R.id.tv_content);
        textView = (TextView) findViewById(R.id.tv_content);
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        texto.setMovementMethod(new ScrollingMovementMethod());
        final WifiP2pDevice deviceinfo;

        clickButtonBuscar.setOnClickListener(new View.OnClickListener(){
            final TextView textobuscar = (TextView) findViewById(R.id.tv_wifi);
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                final HashMap<String, String> buddies = new HashMap<String, String>();
                WifiP2pManager.DnsSdTxtRecordListener dnslistener = new WifiP2pManager.DnsSdTxtRecordListener(){

                    @Override
                    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                        Log.d(TAG, "DnsSdTxtRecord available -" + txtRecordMap.toString());
                        buddies.put(srcDevice.deviceAddress, txtRecordMap.get("buddyname"));
                    }
                };

                final WifiP2pManager manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
                final WifiP2pManager.Channel channel = manager.initialize(MainActivity.this, getMainLooper(), null);

                WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
                    @Override
                    public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                        WifiP2pDevice resourceType) {

                        // Update the device name with the human-friendly version from
                        // the DnsTxtRecord, assuming one arrived.
                        resourceType.deviceName = buddies
                                .containsKey(resourceType.deviceAddress) ? buddies
                                .get(resourceType.deviceAddress) : resourceType.deviceName;

                        // Add to the custom adapter defined specifically for showing
                        // wifi devices.
                        final WifiP2pDevice deviceinfo = resourceType;
                        Log.d(TAG, "onBonjourServiceAvailable " + instanceName);
                        final String nameservice = instanceName;

                        config.deviceAddress = resourceType.deviceAddress;
                        config.wps.setup = WpsInfo.PBC;
                        //manager.createGroup(channel, new WifiP2pManager.ActionListener() {
                        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                            }

                            @Override
                            public void onFailure(int reason) {
                                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textobuscar.setText("Recibidos..." + nameservice + " datos: " + deviceinfo.toString());
                                //stuff that updates ui

                            }
                        });
                    }
                };


                    manager.setDnsSdResponseListeners(channel, servListener, dnslistener);



                WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
                manager.addServiceRequest(channel,
                        serviceRequest,
                        new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                // Success!
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textobuscar.setText("Success service request");
                                        //stuff that updates ui

                                    }
                                });
                            }

                            @Override
                            public void onFailure(int code) {
                                final int error = code;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textobuscar.setText("Fallando service request " + error);
                                        //stuff that updates ui

                                    }
                                });
                                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                            }
                        });

                manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        // Success!
                    }

                    @Override
                    public void onFailure(int code) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                        if (code == WifiP2pManager.P2P_UNSUPPORTED)
                            Log.d(TAG, "P2P isn't supported on this device.");
                    }

                    });
            }
        });


        ////////CREAR SERVICIO //////////////////////
        clickButtonCompartir.setOnClickListener(new View.OnClickListener(){

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {

                //  Create a string map containing information about your service.
                final TextView textocompartir = (TextView) findViewById(R.id.tv_wifi);
                Map record = new HashMap();
                record.put("listenport", String.valueOf("4444"));
                record.put("buddyname", "TFGAPP" +  android.os.Build.MODEL +" "+ (int) (Math.random() * 1000));
                record.put("available", "visible");

                // Service information.  Pass it an instance name, service type
                // _protocol._transportlayer , and the map containing
                // information other devices will want once they connect to this one.
                WifiP2pDnsSdServiceInfo serviceInfo =
                        WifiP2pDnsSdServiceInfo.newInstance("_tfgapp" + android.os.Build.MODEL, "_presence._tcp", record);

                // Add the local service, sending the service info, network channel,
                // and listener that will be used to indicate success or failure of
                // the request.

                final WifiP2pManager manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
                final WifiP2pManager.Channel channel = manager.initialize(MainActivity.this, getMainLooper(), null);
                manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Command successful! Code isn't necessarily needed here,
                        // Unless you want to update the UI or add logging statements.
                        manager.createGroup(channel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {

                                }

                            @Override
                            public void onFailure(int reason) {
                                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                            @Override
                            public void onGroupInfoAvailable(WifiP2pGroup info) {
                                final WifiP2pGroup infoshow = info;
                                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textocompartir.setText(infoshow.getClientList().toString());
                                        //stuff that updates ui

                                    }
                                });
                            }
                        });
                        Log.i(TAG, "Conectado!");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textocompartir.setText("Conectando y dando servicio... mi modelo es " + android.os.Build.MODEL);
                                //stuff that updates ui

                            }
                        });
                    }

                    @Override
                    public void onFailure(int arg0) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                        Log.i(TAG, "falla..."+ arg0);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textocompartir.setText("Algo falla...");
                                //stuff that updates ui

                            }
                        });
                    }
                });
            }
        });
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
