package com.andlit.ui.andlitDevice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.andlit.R;

public class BluetoothConfigActivity extends Activity {

    BluetoothSocket mmSocket;

    Spinner devicesSpinner;
    Button refreshDevicesButton;
    TextView ssidTextView;
    TextView pskTextView;
    Button startButton;
    TextView messageTextView;

    private DeviceAdapter adapter_devices;

    final UUID uuid = UUID.fromString("815425a5-bfac-47bf-9321-c5ff980b5e11");
    final byte delimiter = 33;
    int readBufferPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_config);

        ssidTextView = findViewById(R.id.ssid_text);
        pskTextView = findViewById(R.id.psk_text);
        messageTextView = findViewById(R.id.messages_text);
        messageTextView.setMovementMethod(new ScrollingMovementMethod());

        devicesSpinner = findViewById(R.id.devices_spinner);

        refreshDevicesButton = findViewById(R.id.refresh_devices_button);
        startButton = findViewById(R.id.start_button);

        refreshDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshDevices();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid = ssidTextView.getText().toString();
                String psk = pskTextView.getText().toString();

                BluetoothDevice device = (BluetoothDevice) devicesSpinner.getSelectedItem();
                (new Thread(new workerThread(ssid, psk, device))).start();
            }
        });

        refreshDevices();
    }

    private void refreshDevices() {
        adapter_devices = new DeviceAdapter(this, R.layout.spinner_devices, new ArrayList<BluetoothDevice>());
        devicesSpinner.setAdapter(adapter_devices);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapter_devices.add(device);
            }
        }
    }

    final class workerThread implements Runnable {
        private String ssid;
        private String psk;
        private BluetoothDevice device;

        public workerThread(String ssid, String psk, BluetoothDevice device) {
            this.ssid = ssid;
            this.psk = psk;
            this.device = device;
        }

        public void run() {
            clearOutput();
            writeOutput("Starting config update.");

            writeOutput("Device: " + device.getName() + " - " + device.getAddress());

            try {
                mmSocket = device.createRfcommSocketToServiceRecord(uuid);
                if (!mmSocket.isConnected()) {
                    mmSocket.connect();
                    Thread.sleep(1000);
                }

                OutputStream mmOutputStream = mmSocket.getOutputStream();
                final InputStream mmInputStream = mmSocket.getInputStream();

                waitForResponse(mmInputStream, 100);

                mmOutputStream.write(ssid.getBytes());
                mmOutputStream.flush();
                waitForResponse(mmInputStream, 100);

                mmOutputStream.write(psk.getBytes());
                mmOutputStream.flush();
                waitForResponse(mmInputStream, 100);

                mmSocket.close();
            } catch (Exception e) { writeOutput("Error: " + e.getLocalizedMessage()); }
        }
    }

    private void writeOutput(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String currentText = messageTextView.getText().toString();
                messageTextView.setText(currentText + "\n" + text);
            }
        });
    }

    private void clearOutput() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageTextView.setText("");
            }
        });
    }

    private void waitForResponse(InputStream mmInputStream, long timeout) throws IOException {
        int bytesAvailable;

        while (true) {
            bytesAvailable = mmInputStream.available();
            if (bytesAvailable > 0) {
                byte[] packetBytes = new byte[bytesAvailable];
                byte[] readBuffer = new byte[1024];
                mmInputStream.read(packetBytes);

                for (int i = 0; i < bytesAvailable; i++) {
                    byte b = packetBytes[i];

                    if (b == delimiter) {
                        byte[] encodedBytes = new byte[readBufferPosition];
                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                        final String data = new String(encodedBytes, "US-ASCII");

                        writeOutput("Received:" + data);

                        return;
                    } else {
                        readBuffer[readBufferPosition++] = b;
                    }
                }
            }else {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}