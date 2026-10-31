package com.example.curs;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.util.Log;

import androidx.annotation.NonNull;


public class NetworkMonitor {
    private final ConnectivityManager connectivityManager;
    private final PetRepository repository;
    private final ConnectivityManager.NetworkCallback networkCallback;

    public NetworkMonitor(Context context, PetRepository repository) {
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.repository = repository;

        this.networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                Log.d("Network", "Интернет появился, синхронизируем...");
                repository.syncPendingData();
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                Log.d("Network", "Интернет пропал, работаем офлайн");
            }
        };
    }

    public void startMonitoring() {
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    public void stopMonitoring() {
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }
}