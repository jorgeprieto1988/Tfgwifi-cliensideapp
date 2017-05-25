package com.example.tfg.tfgwifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import static android.content.ContentValues.TAG;

public class NetworkStateReceiver extends BroadcastReceiver {
    WifiP2pManager manager;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("app","Entra en onReceive");
            String action = intent.getAction();
        int id_network_group =intent.getIntExtra("network_id", 0);
        final MainActivity main = new MainActivity();

        Log.d("app","Network connectivity change");
        if(intent.getExtras()!=null) {
            NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED) {
                Log.i("app","Network "+ni.getTypeName()+" connected");

                main_algorithm singleton = main_algorithm.getInstance();
                if(singleton.netid != 0)
                {
                    try {
                        InetAddress address = InetAddress.getByName(singleton.group_ip_m);
                        connectToUserON(address);

                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                Log.d("app","There's no network connectivity" + intent.getExtras().toString());
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


    private void connectToUserON(InetAddress address) {
        String host;
        int port;
        int len;
        Socket socket = new Socket();
        byte buf[]  = new byte[1024];

        port = 9999;

        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            Log.d("app","Paso por aquí");
            socket.bind(null);

            Log.d("app","Ip es: " + address.toString());

            socket.connect((new InetSocketAddress(address, port)), 5000);
            Log.d("app","Paso por aquí 2");

            InputStream inputstream = socket.getInputStream();

            Scanner s = new Scanner(inputstream).useDelimiter("\\A");
            final String result = s.hasNext() ? s.next() : "";
            MainActivity.textViewObj.setText(result);
            inputstream.close();
            socket.close();
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

}
