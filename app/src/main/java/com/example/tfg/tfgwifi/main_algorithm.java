package com.example.tfg.tfgwifi;

import android.content.BroadcastReceiver;
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
    Context mContext;
    BroadcastReceiver receiver_m;
    boolean permiso_envio = false;

    private static final main_algorithm ourInstance = new main_algorithm();

    static main_algorithm getInstance() {
        return ourInstance;
    }

    private main_algorithm() {
    }
    ///MODO ON AUTOMÁTICO ////
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void toSendingModeAuto_m()
    {
        main_algorithm singleton = main_algorithm.getInstance();
        /*
        singleton.manager_m = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        singleton.channel_m = singleton.manager_m.initialize(mContext, getMainLooper(), null);
        */
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

                MainActivity.textViewObj.setText("Grupo creado");

            }

            @Override
            public void onFailure(int reason) {
                final int reasontext = reason;
                MainActivity.textViewObj.setText("Falla por:" + reasontext);
            }
        });
    }
}
