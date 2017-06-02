package com.example.tfg.tfgwifi;

import android.Manifest;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
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
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static android.R.attr.port;
import static android.content.ContentValues.TAG;


public class MainActivity extends AppCompatActivity {
    String ssid = "";
    String password = "";
    String group_ip = "";
    String latitud = "";
    String longitud = "";
    private static final String TAG = "MainActivity";
    ArrayList<String> lista_mensajes = new ArrayList<String>();
    private final IntentFilter intentFilter = new IntentFilter();
    private final WifiP2pConfig config = new WifiP2pConfig();
    boolean servicio_creado = false;
    public WifiP2pManager manager;
    public WifiP2pManager.Channel channel;
    boolean conectado = false;
    public static TextView textViewObj;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewObj = (TextView) findViewById(R.id.tv_wifi);
        main_algorithm singleton = main_algorithm.getInstance();
        singleton.receiver_m = new wifip2preceiver();
        singleton.manager_m = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        singleton.channel_m = singleton.manager_m.initialize(MainActivity.this, getMainLooper(), null);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, lista_mensajes);

        ArrayAdapter<String> adapter_singleton = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, singleton.lista_mensajes_m);

        ListView listView = (ListView) findViewById(R.id.lv_listamensajes);
        listView.setAdapter(adapter);

        ListView listView_singleton = (ListView) findViewById(R.id.lv_listamensajes);
        listView_singleton.setAdapter(adapter_singleton);
        Log.d("app", "Mi versión de android" + Build.VERSION.SDK_INT);

        ////CONECT P2P/////
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        this.getApplicationContext().registerReceiver(singleton.receiver_m, intentFilter);

        if(isNetworkAvailable() == false) {
            setOfflineMode_m();
        }
        else
        {
            conectado = true;
        }
        ///////////////////////////////
        Button clickButton = (Button) findViewById(R.id.bt_consultar);
        Button clickButtonCompartir = (Button) findViewById(R.id.bt_share);
        Button clickButtonBuscar = (Button) findViewById(R.id.bt_buscar);
        Button clickBorrarGrupo = (Button) findViewById(R.id.bt_borra_grupo);
        Button clickBorrarServicios = (Button) findViewById(R.id.bt_borraservicios);
        Button clickMensajes = (Button) findViewById(R.id.bt_messages);
        final TextView texto = (TextView) findViewById(R.id.tv_content);
        textView = (TextView) findViewById(R.id.tv_content);
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        texto.setMovementMethod(new ScrollingMovementMethod());

        clickMensajes.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, create_message.class);
                MainActivity.this.startActivity(intent);
            }
        });

        clickBorrarGrupo.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                main_algorithm singleton = main_algorithm.getInstance();
                if(singleton.manager_m != null && singleton.channel_m != null) {
                    singleton.manager_m.removeGroup(singleton.channel_m, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d("app", "Se borra con botón");
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d("app", "No se borra con botón");
                        }
                    });
                }
            }
        });

        clickBorrarServicios.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                main_algorithm singleton = main_algorithm.getInstance();
                if(singleton.manager_m != null && singleton.channel_m != null) {
                    singleton.manager_m.clearLocalServices(singleton.channel_m, new WifiP2pManager.ActionListener(){

                        @Override
                        public void onSuccess() {
                            Log.d("app", "Se borra servicio con botón");
                        }

                        @Override
                        public void onFailure(int reason) {
                                Log.d("app", "No se borra con botón");
                        }
                    });
                }
            }
        });

        clickButtonBuscar.setOnClickListener(new View.OnClickListener(){
            final TextView textobuscar = (TextView) findViewById(R.id.tv_wifi);
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                final HashMap<String, String> buddies = new HashMap<String, String>();
                WifiP2pManager.DnsSdTxtRecordListener dnslistener = new WifiP2pManager.DnsSdTxtRecordListener(){

                    @Override
                    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                        Log.d("app", "DnsSdTxtRecord available -" + txtRecordMap.toString());
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
                        Log.d("app", "onBonjourServiceAvailable " + instanceName);
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
                            Log.d("app", "P2P isn't supported on this device.");
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
                                final int reasontext = reason;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textocompartir.setText("falla por : " + reasontext);
                                        //stuff that updates ui

                                    }
                                });
                                Toast.makeText(MainActivity.this, "Connect failed. Retry. " + reason,
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
                                        textocompartir.setText("Lista de clientes: " + infoshow.getClientList().toString());
                                        //stuff that updates ui

                                    }
                                });
                            }
                        });
                        Log.d("app", "Conectado!");
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
                        Log.i("app", "falla..."+ arg0);
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
                                      //  lista_mensajes = new ArrayList<String>();
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
            String latitud = location.getLatitude() + "";
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
                    if (!latitud.equals("") && !longitud.equals("") && conectado) {
                        //Ha desarrollar
                        String urljson = "http://tfg-wifi.appspot.com/getmessages?latitud=" + latitud + "&longitud=" + longitud;
                        JsonArrayRequest jsarrayRequest = new JsonArrayRequest
                                (Request.Method.GET, urljson, null, new Response.Listener<JSONArray>() {

                                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        textView.setText("Response: " + response.toString());
                                        //lista_mensajes = new ArrayList<String>();
                                        for (int i = 0; i < response.length(); i++) {
                                            try {
                                                boolean existe = false;
                                                for (String string : lista_mensajes) {
                                                    if(string.matches(response.getJSONObject(i).get("mensaje").toString())){
                                                       existe = true;
                                                    }
                                                }

                                                if(!existe){
                                                   // main_algorithm singleton = main_algorithm.getInstance();
                                                    //singleton.response_m.put(response.getJSONObject(i));
                                                    lista_mensajes.add(response.getJSONObject(i).get("mensaje").toString());
                                                    toSendingModeAuto_m();
                                                }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                       /* if (lista_mensajes.size() > 0)
                                            toSendingModeAuto();*/

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


    ///MODO ON AUTOMÁTICO ////
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void toSendingModeAuto_m()
    {
        //  Create a string map containing information about your service.
        final TextView textocompartir = (TextView) findViewById(R.id.tv_wifi);


        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        main_algorithm singleton = main_algorithm.getInstance();

        singleton.manager_m = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        singleton.channel_m = singleton.manager_m.initialize(MainActivity.this, getMainLooper(), null);

        singleton.manager_m.removeGroup(singleton.channel_m, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("app","Se borra");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("app","No se borra");
            }
        });
        singleton.manager_m.createGroup(singleton.channel_m, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                /*
                Intent serviceIntent = new Intent(MainActivity.this, SocketService.class);
                serviceIntent.putExtra("lista", lista_mensajes);
                startService(serviceIntent);
                */
                main_algorithm singleton = main_algorithm.getInstance();
                Log.d("app", "Crea grupo " + singleton.channel_m);
                singleton.permiso_envio = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textocompartir.setText(textocompartir.getText() + "\n" + "Grupo creado"  + "\n") ;
                        //stuff that updates ui

                    }

                });


            }

            @Override
            public void onFailure(int reason) {
                final int reasontext = reason;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textocompartir.setText("falla por : " + reasontext);
                        //stuff that updates ui

                    }
                });
                Toast.makeText(MainActivity.this, "Connect failed. Retry. " + reason,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    /////MODO OFF /////
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setOfflineMode_m()
    {
        final TextView textobuscar = (TextView) findViewById(R.id.tv_wifi);

        final HashMap<String, String> buddies = new HashMap<String, String>();
        WifiP2pManager.DnsSdTxtRecordListener dnslistener = new WifiP2pManager.DnsSdTxtRecordListener(){

            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                Log.d("app", "DnsSdTxtRecord available -" + txtRecordMap.toString());
                buddies.put(srcDevice.deviceAddress, txtRecordMap.get("buddyname"));
            }
        };

        main_algorithm singleton = main_algorithm.getInstance();

        singleton.manager_m = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        singleton.channel_m = singleton.manager_m.initialize(MainActivity.this, getMainLooper(), null);

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
                Log.d("app", "Entra a la conexión y la info es " + resourceType.toString());

                final String nameservice = instanceName;
                Log.d("app", "Instance name is  " + nameservice.toString());
                String infoservice = nameservice.toString();
                String paraminfoservice[] = infoservice.split(":");
                if (paraminfoservice.length >= 4){
                    String name_ssid = paraminfoservice[1];
                    String password_service = paraminfoservice[2];
                    main_algorithm singleton = main_algorithm.getInstance();
                    singleton.group_ip_m = paraminfoservice[3];

                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                    WifiConfiguration wificonfig = new WifiConfiguration();
                    wificonfig.SSID = String.format("\"%s\"", name_ssid);
                    wificonfig.preSharedKey = String.format("\"%s\"", password_service);

                    Log.d("app", wificonfig.SSID.toString());
                    Log.d("app", wificonfig.preSharedKey.toString());
                    //   this.wifiManager.disconnect();

                    //wifiManager.disconnect();
                    //wificonfig.priority = 10000;
                    wifiManager.updateNetwork(wificonfig);
                    //wifiManager.saveConfiguration();
                    try {
                        //wifiManager.disconnect();
                        singleton.netid = wifiManager.addNetwork(wificonfig);
                        //wifiManager.saveConfiguration();
                        //wifiManager.updateNetwork(wificonfig);
                        Log.d("app", "la id de wifi es " + singleton.netid);
                        //Log.d("app", "información de wifi actual " + wifiManager.getConnectionInfo());
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(singleton.netid, true);
                        boolean reconectando = wifiManager.reconnect();
                        Log.d("app", "reconectando..." + reconectando);
                        /*
                        Intent broadcastedIntent=new Intent(MainActivity.this, NetworkStateReceiver.class);
                        broadcastedIntent.putExtra("network_id", singleton.netid);
                        sendBroadcast(broadcastedIntent); QUIZA HAY QUÉ DESCOMENTAR ESTO*/
                    }
                    catch(Error e) {
                        e.printStackTrace();
                    }

                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textobuscar.setText( "Recibidos..." + nameservice + " datos: " + deviceinfo.toString());
                        //stuff that updates ui

                    }
                });
            }
        };


        singleton.manager_m.setDnsSdResponseListeners(singleton.channel_m, servListener, dnslistener);



        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        singleton.manager_m.addServiceRequest(singleton.channel_m,
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

        singleton.manager_m.discoverServices(singleton.channel_m, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Success!
                Log.d("app", "Descubriendo servicios");
            }

            @Override
            public void onFailure(int code) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                if (code == WifiP2pManager.P2P_UNSUPPORTED)
                    Log.d("app", "P2P isn't supported on this device.");
            }

        });
    }



    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(manager != null)
        {
            manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d("app","Se borra");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d("app","No se borra");
                }
            });
        }
    }

    public class wifip2preceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Check to see if Wi-Fi is enabled and notify appropriate activity

            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                //Here is where the request for connection info should happen
                //As sometimes the pairing can happen but the devices
                //might still be negotiating group owner for example
                Log.d("app", "Entrando después de crear grupo");
                main_algorithm singleton = main_algorithm.getInstance();
                if(singleton.manager_m != null && singleton.permiso_envio == true) {
                    singleton.manager_m.requestConnectionInfo(singleton.channel_m, new WifiP2pManager.ConnectionInfoListener() {

                        @Override
                        public void onConnectionInfoAvailable(WifiP2pInfo info) {
                            Log.d("app", "Entra desde wifip2preceiver en onconnectioninfoavailable" + info);
                            if (info.groupOwnerAddress != null){
                                main_algorithm singleton = main_algorithm.getInstance();
                                singleton.group_ip_m = info.groupOwnerAddress.getHostAddress();

                                Intent serviceIntent = new Intent(MainActivity.this, SocketService.class);
                                serviceIntent.putExtra("lista", lista_mensajes);
                                startService(serviceIntent);
                                crearServicio_m();
                            }

                        }
                    });

                } // CAMBIAR ESTO POR UN SOLO MANAGER!
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
            }

        }
    }

    public void crearServicio_m(){
        final TextView textocompartir = (TextView) findViewById(R.id.tv_wifi);
        main_algorithm singleton = main_algorithm.getInstance();

        singleton.manager_m.requestGroupInfo(singleton.channel_m, new WifiP2pManager.GroupInfoListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup info) {
                final WifiP2pGroup infoshow = info;
                main_algorithm singleton = main_algorithm.getInstance();
                Log.d("app", "Entra en ongroupinfoavailabe " + info + " " + singleton.channel_m.toString());
                if(infoshow != null)
                {
                    singleton.ssid_m = info.getNetworkName();
                    singleton.password_m = info.getPassphrase();

                    Map record = new HashMap();
                    record.put("listenport", String.valueOf("4444"));
                    record.put("buddyname", "TFGAPP:" + singleton.ssid_m + ":" + singleton.password_m + ":" + singleton.group_ip_m);
                    record.put("available", "visible");
                    Log.d("app", "Entra en ongroupinfoavailabe");
                    // Service information.  Pass it an instance name, service type
                    // _protocol._transportlayer , and the map containing
                    // information other devices will want once they connect to this one.
                    final WifiP2pDnsSdServiceInfo serviceInfo =
                            WifiP2pDnsSdServiceInfo.newInstance("_tfgapp:" + singleton.ssid_m + ":" + singleton.password_m + ":" + singleton.group_ip_m, "_presence._tcp", record);
                    if(servicio_creado == false) {


                        singleton.manager_m.addLocalService(singleton.channel_m, serviceInfo, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                main_algorithm singleton = main_algorithm.getInstance();
                                // Command successful! Code isn't necessarily needed here,
                                // Unless you want to update the UI or add logging statements.
                                servicio_creado = true;
                                Log.d("app", "Success local service");
                                Log.d("app", "Conectado!");
                                Log.d("app", "Dando servicio: " + android.os.Build.MODEL +
                                        "\n" +
                                        "Info ssid, pass, y ip:" + singleton.ssid_m + " " + singleton.password_m + " " + singleton.group_ip_m);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        main_algorithm singleton = main_algorithm.getInstance();
                                        textocompartir.setText("Dando servicio: " + android.os.Build.MODEL +
                                                "\n" +
                                                "Info ssid, pass, y ip:" + singleton.ssid_m + " " + singleton.password_m + " " + singleton.group_ip_m);
                                        //stuff that updates ui

                                    }
                                });
                            }

                            @Override
                            public void onFailure(int arg0) {
                                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                                Log.d("app", "falla..." + arg0);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        main_algorithm singleton = main_algorithm.getInstance();
                                        textocompartir.setText("Algo falla..." + singleton.channel_m.toString() + " y manager " + singleton.manager_m.toString());
                                        //stuff that updates ui

                                    }
                                });
                            }
                        });
                    }
                }
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(infoshow != null)
                            textocompartir.setText("Lista de clientes: " + infoshow.getClientList().toString());
                        //stuff that updates ui

                    }
                });
            }
        });

    }

}



