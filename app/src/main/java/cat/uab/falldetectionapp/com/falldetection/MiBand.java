package cat.uab.falldetectionapp.com.falldetection;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.util.Log;
import com.jjoe64.graphview.GraphView;
import java.util.Arrays;
import cat.uab.falldetectionapp.com.falldetection.listeners.NotifyListener;
import cat.uab.falldetectionapp.com.falldetection.model.Profile;
import cat.uab.falldetectionapp.com.falldetection.model.Protocol;
import cat.uab.falldetectionapp.com.falldetection.model.BatteryInfo;

public class MiBand {

    private static final String TAG = "miband-android";

    private Context context;
    private BluetoothIO io;

    public MiBand(Context context, GraphView mScatterPlot) {
        this.context = context;
        this.io = new BluetoothIO();
    }

    public static void startScan(ScanCallback callback) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println(adapter);
        if (null == adapter) {
            System.out.println("BluetoothAdapter is null");
            return;
        }
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (null == scanner) {
            System.out.println("BluetoothLeScanner is null");
            return;
        }
        scanner.startScan(callback);
        System.out.println("started scanner");
    }

    public static void stopScan(ScanCallback callback) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
            Log.e(TAG, "BluetoothAdapter is null");
            return;
        }
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (null == scanner) {
            Log.e(TAG, "BluetoothLeScanner is null");
            return;
        }
        scanner.stopScan(callback);
        System.out.println("stopped scanner");
    }

    public void connect(BluetoothDevice device, final ActionCallback callback) {
        this.io.connect(context, device, callback);
    }
    public void disconnect() {
        this.io.disconnect();
    }

    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        this.io.setDisconnectedListener(disconnectedListener);
    }

    public void pair(final ActionCallback callback) {
        System.out.println("pairing...");
        ActionCallback ioCallback = new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                System.out.println("pair result " + Arrays.toString(characteristic.getValue()));
                if (characteristic.getValue().length == 1 && characteristic.getValue()[0] == 2) {
                    callback.onSuccess(null);
                } else {
                    callback.onFail(-1, "respone values no succ!");
                }
            }

            @Override
            public void onFail(int errorCode, String msg) {
                System.out.println("failed pairing");
                System.out.println(errorCode);
                System.out.println(msg);
                callback.onFail(errorCode, msg);
            }
        };
        this.io.writeAndRead(Profile.CUSTOM_SERVICE_AUTH_CHARACTERISTIC, Protocol.PAIR, ioCallback);
    }


    public void getBatteryInfo(final ActionCallback callback) {
        System.out.println("battery info");
        ActionCallback ioCallback = new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                System.out.println("getBatteryInfo result " + Arrays.toString(characteristic.getValue()));
                if (characteristic.getValue().length == 20) {
                    BatteryInfo info = BatteryInfo.fromByteData(characteristic.getValue());
                    callback.onSuccess(info);
                } else {
                    callback.onFail(-1, "result format wrong!");
                }
            }

            @Override
            public void onFail(int errorCode, String msg) {
                System.out.println(errorCode);
                System.out.println(msg);
                callback.onFail(errorCode, msg);
            }
        };
        this.io.readCharacteristic(Profile.UUID_CHARACTERISTIC_6_BATTERY_INFO, ioCallback);
    }
    public void authorizeMiBand(final ActionCallback callback) {
        System.out.println("authorizing...");
        ActionCallback ioCallback = new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                System.out.println("Authorized");
                callback.onSuccess(null);
            }
            @Override
            public void onFail(int errorCode, String msg) {
                System.out.println("authorizing failed");
                callback.onFail(errorCode, msg);
            }
        };
        this.io.authorizasion(Profile.CUSTOM_SERVICE_AUTH_DESCRIPTOR, ioCallback);
    }
    public void getDeviceInfo(final ActionCallback callback){
        System.out.println("getting device info");
        ActionCallback ioCallback = new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                System.out.println("got device info");
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                System.out.println("device data " + Arrays.toString(characteristic.getValue()));
                String value = characteristic.getStringValue(0);
                System.out.println(value);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                System.out.println("device info failed");
                callback.onFail(errorCode, msg);
            }
        };
        this.io.deviceInfo(Profile.UUID_SERVICE_HEARTRATE, ioCallback);
    }

    public void heartRate(final NotifyListener listener){
        this.io.startScanHeartRate(new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                listener.onNotify(data);
            }
        });
    }

    public void sensorData(Boolean activate_disactivate,  final NotifyListener listener) {
        this.io.sensorData(activate_disactivate, new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                listener.onNotify(data);
            }
        });
    }





}
