package com.example.tfg.tfgwifi;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class SocketService extends IntentService {
    int len;
    byte buf[]  = new byte[1024];


    public SocketService() {
        super("SocketThread");
    }
    public SocketService(String name) {
        super(name);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onHandleIntent(Intent workIntent) {
        //CONNECT SOCKET HERE
        try {
            ArrayList<String> lista_mensajes;
            Bundle extras = workIntent.getExtras();
            lista_mensajes = extras.getStringArrayList("lista");
            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            main_algorithm singleton = main_algorithm.getInstance();
            InetAddress addr = InetAddress.getByName(singleton.group_ip_m);
            ServerSocket serverSocket = new ServerSocket(8888, 8888, addr);

            Log.e(TAG, "Abro mis sockets yas con la ip " + addr.getHostAddress());
            Socket client = serverSocket.accept();
            Log.e(TAG, "Quizá no sigo?");
            OutputStream outputStream = client.getOutputStream();
            InputStream inputStream = null;
            inputStream = new ByteArrayInputStream(lista_mensajes.get(0).getBytes(StandardCharsets.UTF_8));
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();


            /**
             * If this code is reached, a client has connected and transferred data
             * Save the input stream from the client as a JPEG file
             */
            ///////PROBAR///////

            ///////////////////
            serverSocket.close();
            Log.e(TAG, "Cierro mis sockets");
        } catch (IOException e) {
            // Log.e(TAG, e.getMessage());
            //  return null;
        }
    }

}
