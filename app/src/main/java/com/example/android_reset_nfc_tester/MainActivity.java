package com.example.android_reset_nfc_tester;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private final BroadcastReceiver nfcReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, action);
        }
    };

    public static String ACTION_PAYMENT_SUCCESS = TAG + ".ActionPaymentSuccess";
    public static String ACTION_PAYMENT_FAIL = TAG + ".ActionPaymentFail";
    public static String ACTION_RECEIPT_SUCCESS = TAG + ".ActionReceiptSuccess";
    public static String ACTION_RECEIPT_FAIL = TAG + ".ActionReceiptFail";
    private Button startExternalApp;
    private Button nfcEndButton;
    private NfcAdapter nfcAdapter;
    private boolean isReceiverRegistered = false;;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        this.startExternalApp = findViewById(R.id.startExternalApp);
        this.nfcEndButton = findViewById(R.id.nfcEndButton);
        initNfc();
        registerNfcReceiver();
        togglePolling(false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        startExternalApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToExternalApp();
            }
        });
    }

    private void togglePolling(boolean shouldPoll) {
        if (shouldPoll) {
            System.out.println("reset discovery technology");
            nfcAdapter.resetDiscoveryTechnology(this);
        } else {
            System.out.println("set discovery technology, reader disabled");
            nfcAdapter.setDiscoveryTechnology(this, NfcAdapter.FLAG_READER_DISABLE, NfcAdapter.FLAG_LISTEN_NFC_PASSIVE_A);
        }
    }

    private void initNfc() {
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            this.nfcAdapter = ((android.nfc.NfcManager) getSystemService(Context.NFC_SERVICE)).getDefaultAdapter();
        }
    }

    private boolean isNfcEnabled() {
        return nfcAdapter != null && nfcAdapter.isEnabled();
    }

    private void changeToExternalApp() {
        // Event is on January 23, 2021 -- from 7:30 AM to 10:30 AM.
        Intent calendarIntent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2021, 0, 23, 7, 30);
        Calendar endTime = Calendar.getInstance();
        endTime.set(2021, 0, 23, 10, 30);
        calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis());
        calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis());
        calendarIntent.putExtra(CalendarContract.Events.TITLE, "Ninja class");
        calendarIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, "Secret dojo");

        startActivity(calendarIntent);
    }


    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        if (isNfcEnabled()) {
            togglePolling(true);
            CardEmulation.getInstance(nfcAdapter).unsetPreferredService(this);
        }
        unregisterNfcReceiver();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "[on resume]");
        super.onResume();
        if (isNfcEnabled()) {
            CardEmulation.getInstance(nfcAdapter).setPreferredService(this, new ComponentName(this, MyHostApduService.class));
            togglePolling(false);
        }

        registerNfcReceiver();
    }

    private void registerNfcReceiver() {
        if (!isReceiverRegistered) {
            Log.d(TAG, "registering NFC receiver");
            IntentFilter filter = new IntentFilter(ACTION_PAYMENT_SUCCESS);
            filter.addAction(ACTION_RECEIPT_SUCCESS);
            filter.addAction(ACTION_PAYMENT_FAIL);
            filter.addAction(ACTION_RECEIPT_FAIL);

            LocalBroadcastManager.getInstance(this).registerReceiver(nfcReceiver, filter);
            isReceiverRegistered = true;
        }
    }

    private void unregisterNfcReceiver() {
        if (isReceiverRegistered) {
            Log.d(TAG, "unregistering NFC receiver");

            LocalBroadcastManager.getInstance(this).unregisterReceiver(nfcReceiver);
            isReceiverRegistered = false;
        }
    }
}