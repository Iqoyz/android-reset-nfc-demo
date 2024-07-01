package com.example.android_reset_nfc_tester;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;

public class MyHostApduService extends HostApduService {
    private static final String TAG = "MyHostApduService";

    private static final byte[] SELECT_APDU_HEADER = {
            (byte) 0x00, // CLA (Class of instruction)
            (byte) 0xA4, // INS (Instruction code)
            (byte) 0x04, // P1 (Parameter 1)
            (byte) 0x00  // P2 (Parameter 2)
    };

    private static final byte[] SELECT_OK_SW = {
            (byte) 0x90, // SW1 (Status byte 1)
            (byte) 0x00  // SW2 (Status byte 2)
    };

    private static final byte[] UNKNOWN_CMD_SW = {
            (byte) 0x6F, // SW1 (Status byte 1)
            (byte) 0x00  // SW2 (Status byte 2)
    };


    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        Log.d(TAG, "Received APDU: " + Arrays.toString(apdu));
        if (apdu == null) {
            return UNKNOWN_CMD_SW;
        }

        if (Arrays.equals(SELECT_APDU_HEADER, Arrays.copyOf(apdu, 4))) {
            // Process SELECT APDU
            return SELECT_OK_SW;
        }

        // Unknown command
        return UNKNOWN_CMD_SW;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "\"" + TAG + "\": Service create");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "\"" + TAG + "\": Service destroy");
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "Deactivated: " + reason);
    }
}

