package com.example.tfg.tfgwifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onReceive(Context context, Intent intent) {
        Log.d("app","Network connectivity change");
        if(intent.getExtras()!=null) {
            NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED) {
                Log.i("app","Network "+ni.getTypeName()+" connected");
            }
            else
            {
                Log.d("app","There's no network connectivity");
               // MainActivity main = new MainActivity();
               // main.setOfflineMode();
            }
        }
        else
        {
            Log.d("app","There's no network connectivity");
        }
        if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
            Log.d("app","There's no network connectivity");
        }
    }

}
