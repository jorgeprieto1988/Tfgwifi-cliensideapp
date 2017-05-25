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
        BroadcastReceiver receiver;
        receiver = new wifip2preceiver();
        textViewObj = (TextView) findViewById(R.id.tv_wifi);
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

        this.getApplicationContext().registerReceiver(receiver, intentFilter);

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
        final TextView texto = (TextView) findViewById(R.id.tv_content);
        textView = (TextView) findViewById(R.id.tv_content);
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        texto.setMovementMethod(new ScrollingMovementMethod());
        final WifiP2pDevice deviceinfo;

        clickBorrarGrupo.setOnClickListener(new View.OnClickListener(){
            final TextView textobuscar = (TextView) findViewById(R.id.tv_wifi);
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
    void toSendingModeAuto()
    {
        //  Create a string map containing information about your service.
        final TextView textocompartir = (TextView) findViewById(R.id.tv_wifi);


        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(MainActivity.this, getMainLooper(), null);

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
        manager.createGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                /*
                Intent serviceIntent = new Intent(MainActivity.this, SocketService.class);
                serviceIntent.putExtra("lista", lista_mensajes);
                startService(serviceIntent);
                */
                Log.d("app", "Crea grupo " + channel);
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
        /*
        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener(){

            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                Log.d(TAG, "Entra en onconnectioninfoavailable" + info + " " + channel);
                if(info.groupOwnerAddress != null)
                    group_ip = info.groupOwnerAddress.getHostAddress();
            }
        });


        manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {

            @Override
            public void onGroupInfoAvailable(WifiP2pGroup info) {
                final WifiP2pGroup infoshow = info;
                Log.d(TAG, "Entra en ongroupinfoavailabe " + info + " " + channel.toString());
                if(infoshow != null)
                {
                    ssid = info.getNetworkName();
                    password = info.getPassphrase();

                    Map record = new HashMap();
                    record.put("listenport", String.valueOf("4444"));
                    record.put("buddyname", "TFGAPP:" + ssid + ":" + password + ":" + group_ip);
                    record.put("available", "visible");
                    Log.d(TAG, "Entra en ongroupinfoavailabe");
                    // Service information.  Pass it an instance name, service type
                    // _protocol._transportlayer , and the map containing
                    // information other devices will want once they connect to this one.
                    final WifiP2pDnsSdServiceInfo serviceInfo =
                            WifiP2pDnsSdServiceInfo.newInstance("_tfgapp" + ssid + ":" + password + ":" + group_ip, "_presence._tcp", record);
                    if(servicio_creado == false) {


                        manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                // Command successful! Code isn't necessarily needed here,
                                // Unless you want to update the UI or add logging statements.
                                servicio_creado = true;
                                Log.d(TAG, "Success local service");
                                Log.i(TAG, "Conectado!");
                                Log.i(TAG, "Dando servicio: " + android.os.Build.MODEL +
                                        "\n" +
                                        "Info ssid, pass, y ip:" + ssid + " " + password + " " + group_ip);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textocompartir.setText("Dando servicio: " + android.os.Build.MODEL +
                                                "\n" +
                                                "Info ssid, pass, y ip:" + ssid + " " + password + " " + group_ip);
                                        //stuff that updates ui

                                    }
                                });
                            }

                            @Override
                            public void onFailure(int arg0) {
                                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                                Log.d(TAG, "falla..." + arg0);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textocompartir.setText("Algo falla..." + channel.toString() + " y manager " + manager.toString());
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
        });*/


    }

    ///MODO ON AUTOMÁTICO ////
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    void toSendingModeAuto_m()
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

    /////MODO ON ///////////
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    void toSendingMode()
    {
        //  Create a string map containing information about your service.
        final TextView textocompartir = (TextView) findViewById(R.id.tv_wifi);
        Map record = new HashMap();
        record.put("listenport", String.valueOf("4444"));
        record.put("buddyname", "TFGAPP" +  android.os.Build.MODEL +" "+ (int) (Math.random() * 1000));
        record.put("available", "visible");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        final WifiP2pDnsSdServiceInfo serviceInfo =
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

                manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(WifiP2pGroup info) {
                        final WifiP2pGroup infoshow = info;

                        // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(infoshow != null)
                                textocompartir.setText("Lista de clientes: " + infoshow.getClientList().toString());
                                //stuff that updates ui

                            }
                        });
                        Intent serviceIntent = new Intent(MainActivity.this, SocketService.class);
                        serviceIntent.putExtra("lista", lista_mensajes);
                        startService(serviceIntent);

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
                Log.d("app", "falla..."+ arg0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textocompartir.setText("Algo falla..." + channel.toString() + " y manager " + manager.toString());
                        //stuff that updates ui

                    }
                });
            }
        });
    }

    private void connection_in_background() {
        Log.d("app", "Entro en conecction in background");


        try {

            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            Log.d("app", "Abro mis sockets");
            ServerSocket serverSocket = new ServerSocket(8888);
            Socket client = serverSocket.accept();

            /**
             * If this code is reached, a client has connected and transferred data
             * Save the input stream from the client as a JPEG file
             */
            ///////PROBAR///////
            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
            out.writeObject(lista_mensajes);

            out.close();

            ///////////////////
            serverSocket.close();
            Log.d("app", "Cierro mis sockets");
        } catch (IOException e) {
            Log.d("app", e.getMessage());
        }

        AsyncTask FileServerAsyncTask = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                try {

                    /**
                     * Create a server socket and wait for client connections. This
                     * call blocks until a connection is accepted from a client
                     */
                    Log.d("app", "Abro mis sockets");
                    ServerSocket serverSocket = new ServerSocket(8888);
                    Socket client = serverSocket.accept();

                    /**
                     * If this code is reached, a client has connected and transferred data
                     * Save the input stream from the client as a JPEG file
                     */
                    ///////PROBAR///////
                    ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                    out.writeObject(lista_mensajes);

                    out.close();

                    ///////////////////
                    serverSocket.close();
                    return lista_mensajes;
                } catch (IOException e) {
                    Log.d("app", e.getMessage());
                    return null;
                }
            }
        };

    }

    /////MODO OFF /////
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setOfflineMode()
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
                Log.d("app", "Entra a la conexión y la info es " + resourceType.toString());

                final String nameservice = instanceName;

                String infoservice = resourceType.deviceName.toString();
                String paraminfoservice[] = infoservice.split(":");
                if (paraminfoservice.length >= 4){
                    String name_ssid = paraminfoservice[1];
                    String password_service = paraminfoservice[2];
                    String ip_address = paraminfoservice[3];

                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                    WifiConfiguration wificonfig = new WifiConfiguration();
                    wificonfig.SSID = String.format("\"%s\"", name_ssid);
                    //wificonfig.SSID =  "\"" + name_ssid + "\"";
                    //String.format("\"%s\"", name_ssid);
                    //wificonfig.preSharedKey = "\""+ password_service +"\"";
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
                        int netid = wifiManager.addNetwork(wificonfig);
                        //wifiManager.saveConfiguration();
                        //wifiManager.updateNetwork(wificonfig);
                        Log.d("app", "la id de wifi es " + netid);
                        //Log.d("app", "información de wifi actual " + wifiManager.getConnectionInfo());
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(netid, true);
                        boolean reconectando = wifiManager.reconnect();
                        Log.d("app", "reconectando..." + reconectando);

                        /*
                        Intent broadcastedIntent=new Intent(MainActivity.this, NetworkStateReceiver.class);
                        broadcastedIntent.putExtra("network_id", netid);
                        sendBroadcast(broadcastedIntent);*/
                    }
                    catch(Error e) {
                        e.printStackTrace();
                    }
                    /*
                    try {
                        connectToUserON(config, InetAddress.getByName(ip_address));
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }

                    wifiManager.disconnect();
                    //this.wifiManager.reconnect();
                    */
                }

                /*
                config.deviceAddress = resourceType.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                //manager.createGroup(channel, new WifiP2pManager.ActionListener() {
                manager.connect(channel, config, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                            @Override
                            public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                                InetAddress address = wifiP2pInfo.groupOwnerAddress;
                                Toast.makeText(MainActivity.this, "GRoup owner address = " + address,
                                        Toast.LENGTH_SHORT).show();

                                    try {
                                        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                                            NetworkInterface intf = en.nextElement();
                                            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                                                InetAddress inetAddress = enumIpAddr.nextElement();
                                                if (!inetAddress.isLoopbackAddress()) {
                                                    Log.d(TAG, "Group owner address = " + address + "My ip address: " + inetAddress.getHostAddress() );
                                                }
                                            }
                                        }
                                    } catch (SocketException ex) {
                                        Log.d(TAG, ex.toString());
                                    }


                                connectToUserON(config, address);
                                //socket communication
                            }
                        });


                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                                Toast.LENGTH_SHORT).show();
                    }
                });*/

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textobuscar.setText( "Recibidos..." + nameservice + " datos: " + deviceinfo.toString());
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
        singleton.manager_off = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        singleton.channel_off = singleton.manager_off.initialize(MainActivity.this, getMainLooper(), null);

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


        singleton.manager_off.setDnsSdResponseListeners(singleton.channel_off, servListener, dnslistener);



        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        singleton.manager_off.addServiceRequest(singleton.channel_off,
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

        singleton.manager_off.discoverServices(singleton.channel_off, new WifiP2pManager.ActionListener() {

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


    private void connectToUserON(WifiP2pConfig config, InetAddress address) {
        Context context = this.getApplicationContext();
        String host;
        int port;
        int len;
        Socket socket = new Socket();
        byte buf[]  = new byte[1024];

        host = config.deviceAddress;
        port = 8888;

        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            Log.d("app","Paso por aquí");
            socket.bind(null);
            ///COMPROBAR QUÉ QUIZÁ LA INET QUE COGES ES LA PRIVADA DE LA OTRA RED??///
            socket.connect((new InetSocketAddress(address, port)), 500);
            Log.d("app","Paso por aquí 2");

            InputStream inputstream = socket.getInputStream();

            Scanner s = new Scanner(inputstream).useDelimiter("\\A");
            final String result = s.hasNext() ? s.next() : "";

            inputstream.close();
            socket.close();

            runOnUiThread(new Runnable() {
                final TextView textobuscar2 = (TextView) findViewById(R.id.tv_wifi);
                @Override
                public void run() {
                    textobuscar2.setText("Exito cogiendo mensajes! : " + result );
                    //stuff that updates ui

                }
            });


            /**
             * Create a byte stream from a JPEG file and pipe it to the output stream
             * of the socket. This data will be retrieved by the server device.
             */


        }
        catch(UnknownHostException e) {
            Log.d("app","Error por el host" + e.toString());
        }
        // Exception thrown when network timeout occurs
        catch (InterruptedIOException iioe)
        {
            System.err.println ("Remote host timed out during read operation");
        }
// Exception thrown when general network I/O error occurs
        catch (IOException ioe)
        {
            System.err.println ("Network I/O error - " + ioe);
            Log.d("app","Mi errorcito" + ioe.toString());
        }
        catch(Error e){
            Log.d("app","Mi errorcito" + e.toString());
        }

        //METER CATCH ERROR

/**
 * Clean up any open sockets when done
 * transferring or if an exception occurred.
 */
        finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        //catch logic
                    }
                }
            }
        }
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
        /*
        {
            if (mWifiConnection != null) {
                mWifiConnection.Stop();
                mWifiConnection = null;
            }
            if (mWifiAccessPoint != null) {
                mWifiAccessPoint.Stop();
                mWifiAccessPoint = null;
            }

            if (mWifiServiceSearcher != null) {
                mWifiServiceSearcher.Stop();
                mWifiServiceSearcher = null;
            }

            timeHandler.removeCallbacks(mStatusChecker);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBRReceiver);
        }
        */
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
                if(singleton.manager_m != null) {
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
                    /*
                    Intent serviceIntent = new Intent(MainActivity.this, SocketService.class);
                    serviceIntent.putExtra("lista", lista_mensajes);
                    startService(serviceIntent);
                    crearServicio_m();
                    */
                }
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

    public void crearServicio(){
        final TextView textocompartir = (TextView) findViewById(R.id.tv_wifi);

        manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup info) {
                final WifiP2pGroup infoshow = info;
                Log.d("app", "Entra en ongroupinfoavailabe " + info + " " + channel.toString());
                if(infoshow != null)
                {
                    ssid = info.getNetworkName();
                    password = info.getPassphrase();

                    Map record = new HashMap();
                    record.put("listenport", String.valueOf("4444"));
                    record.put("buddyname", "TFGAPP:" + ssid + ":" + password + ":" + group_ip);
                    record.put("available", "visible");
                    Log.d("app", "Entra en ongroupinfoavailabe");
                    // Service information.  Pass it an instance name, service type
                    // _protocol._transportlayer , and the map containing
                    // information other devices will want once they connect to this one.
                    final WifiP2pDnsSdServiceInfo serviceInfo =
                            WifiP2pDnsSdServiceInfo.newInstance("_tfgapp:" + ssid + ":" + password + ":" + group_ip, "_presence._tcp", record);
                    if(servicio_creado == false) {


                        manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                // Command successful! Code isn't necessarily needed here,
                                // Unless you want to update the UI or add logging statements.
                                servicio_creado = true;
                                Log.d("app", "Success local service");
                                Log.d("app", "Conectado!");
                                Log.d("app", "Dando servicio: " + android.os.Build.MODEL +
                                        "\n" +
                                        "Info ssid, pass, y ip:" + ssid + " " + password + " " + group_ip);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textocompartir.setText("Dando servicio: " + android.os.Build.MODEL +
                                                "\n" +
                                                "Info ssid, pass, y ip:" + ssid + " " + password + " " + group_ip);
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
                                        textocompartir.setText("Algo falla..." + channel.toString() + " y manager " + manager.toString());
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



