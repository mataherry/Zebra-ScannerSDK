package com.mataherry.rfd8500;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebra.scannercontrol.SDKHandler;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IDcsSdkApiDelegate {
    SDKHandler sdkHandler;
    ArrayList<DCSScannerInfo> mScannerInfoList = new ArrayList<>();
    static int connectedScannerID;

    Button btnConnect;
    EditText txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sdkHandler.dcssdkEstablishCommunicationSession(mScannerInfoList.get(0).getScannerID());
            }
        });
        txtResult = findViewById(R.id.txtResult);

        sdkHandler = new SDKHandler(this);
        sdkHandler.dcssdkSetDelegate(this);

        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);

        int notifications_mask = 0;

        // We would like to subscribe to all scanner available/not-available events
        notifications_mask |=
                DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value |
                        DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value;
        // We would like to subscribe to all scanner connection events
        notifications_mask |=
                DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value |
                        DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value;
        // We would like to subscribe to all barcode events
        notifications_mask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value;
        // subscribe to events set in notification mask
        sdkHandler.dcssdkSubsribeForEvents(notifications_mask);

        sdkHandler.dcssdkEnableAvailableScannersDetection(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sdkHandler = null;
    }

    @Override
    public void dcssdkEventScannerAppeared(DCSScannerInfo availableScanner) {
        mScannerInfoList.add(availableScanner);
        if (mScannerInfoList.size() > 0) {
            DCSScannerInfo reader = mScannerInfoList.get(0);
            txtResult.setText(reader.getScannerName());
        }
    }

    @Override
    public void dcssdkEventScannerDisappeared(int scannerID) {

    }

    @Override
    public void dcssdkEventCommunicationSessionEstablished(DCSScannerInfo activeScanner) {
        connectedScannerID = activeScanner.getScannerID();
        txtResult.append("... Connected");
    }

    @Override
    public void dcssdkEventCommunicationSessionTerminated(int scannerID) {

    }

    @Override
    public void dcssdkEventBarcode(final byte[] barcodeData, final int barcodeType, final int fromScannerID) {
        String code = new String(barcodeData);
        dataHandler.obtainMessage(Constants.BARCODE_RECEIVED, code).sendToTarget();
    }

    @Override
    public void dcssdkEventImage(byte[] bytes, int i) {

    }

    @Override
    public void dcssdkEventVideo(byte[] bytes, int i) {

    }

    @Override
    public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent var1) {

    }

    private final Handler dataHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.BARCODE_RECEIVED:
                    String code = (String) msg.obj;
                    txtResult.append("\n" + code);
                    break;
            }
        }
    };
}
