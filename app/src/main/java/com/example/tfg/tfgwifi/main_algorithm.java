package com.example.tfg.tfgwifi;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.os.Looper.getMainLooper;

/**
 * Created by troko on 24/05/2017.
 */

class main_algorithm {
    String ssid_m = "";
    String password_m = "";
    String group_ip_m = "";
    String latitud_m = "";
    String longitud_m = "";
    static final String TAG_m = "MainActivity";
    ArrayList<String> lista_mensajes_m = new ArrayList<String>();
    final IntentFilter intentFilter_m = new IntentFilter();
    final WifiP2pConfig config_m = new WifiP2pConfig();
    boolean servicio_creado_m = false;
    public WifiP2pManager manager_m;
    public WifiP2pManager.Channel channel_m;
    public WifiP2pManager manager_off;
    public WifiP2pManager.Channel channel_off;
    boolean conectado_m = false;
    int netid = 0;

    private static final main_algorithm ourInstance = new main_algorithm();

    static main_algorithm getInstance() {
        return ourInstance;
    }

    private main_algorithm() {
    }
    /*
    ///MODO ON AUTOMÁTICO ////
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)

    void toSendingModeAuto_m()
    {
        //  Create a string map containing information about your service.
        final TextView textocompartir = (TextView) findViewById(R.id.tv_wifi);

        manager_m = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel_m = manager_m.initialize(getApplicationContext(), getMainLooper(), null);

        manager_m.removeGroup(channel_m, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("app","Se borra");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("app","No se borra");
            }
        });
        manager_m.createGroup(channel_m, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {

                Intent serviceIntent = new Intent(MainActivity.this, SocketService.class);
                serviceIntent.putExtra("lista", lista_mensajes);
                startService(serviceIntent);

                Log.d("app", "Crea grupo " + channel_m);
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
            }
        });

    }
    */
}
