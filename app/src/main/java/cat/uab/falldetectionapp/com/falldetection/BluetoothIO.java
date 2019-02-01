package cat.uab.falldetectionapp.com.falldetection;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import cat.uab.falldetectionapp.com.falldetection.listeners.NotifyListener;
import cat.uab.falldetectionapp.com.falldetection.model.BleNamesResolver;
import cat.uab.falldetectionapp.com.falldetection.model.Profile;

class BluetoothIO extends BluetoothGattCallback {
    private static final String TAG = "BluetoothIO";
    private final Object object = new Object();
    BluetoothGatt gatt;
    ActionCallback currentCallback;

    HashMap<UUID, NotifyListener> notifyListeners = new HashMap<UUID, NotifyListener>();
    NotifyListener disconnectedListener = null;
    private Map<UUID, BluetoothGattCharacteristic> mAvailableCharacteristics;
    private final Object characteristicsMonitor = new Object();
    private final Set<UUID> mSupportedServices = new HashSet<>(4);
    private static final byte[] startSensorRead1 = new byte[]{0x01, 0x01, 0x19};
    private static final byte[] startSensorRead2 = new byte[]{0x02};
    private static final byte[] stopSensorRead = new byte[]{0x03};
    private static final byte[] startHeartMeasurementContinuous = new byte[]{0x15, Profile.COMMAND_SET__HR_CONTINUOUS, 1};
    private static Timer timer;
    private Context context;
    private byte authFlags = Profile.AUTH_BYTE;
    protected Set<UUID> getSupportedServices() {
        return mSupportedServices;
    }

    public void connect(final Context context, BluetoothDevice device, final ActionCallback callback) {
        device.connectGatt(context, false, BluetoothIO.this);
        BluetoothIO.this.currentCallback = callback;
    }

    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        this.disconnectedListener = disconnectedListener;
    }

    public BluetoothDevice getDevice() {
        if (null == gatt) {
            System.out.println("connect to miband first");
            return null;
        }
        return gatt.getDevice();
    }

    public void writeAndRead(final UUID uuid, byte[] valueToWrite, final ActionCallback callback) {
        ActionCallback readCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object characteristic) {
                BluetoothIO.this.readCharacteristic(uuid, callback);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        };
        this.writeCharacteristic(uuid, valueToWrite, readCallback);
    }

    public void writeCharacteristic(UUID characteristicUUID, byte[] value, ActionCallback callback) {
        writeCharacteristic(Profile.UUID_SERVICE_MILI, characteristicUUID, value, callback);
    }

    public void writeCharacteristic(UUID serviceUUID, UUID characteristicUUID, byte[] value, ActionCallback callback) {
        System.out.println("write characteristics");
        try {
            if (null == gatt) {
                System.out.println( "connect to miband first");
                throw new Exception("connect to miband first");
            }
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara = gatt.getService(serviceUUID).getCharacteristic(characteristicUUID);
            System.out.println(chara);
            if (null == chara) {
                System.out.println("BluetoothGattCharacteristic " + characteristicUUID + " is not exsit");
                this.onFail(-1, "BluetoothGattCharacteristic " + characteristicUUID + " is not exsit");
                return;
            }
            System.out.println("value is set");
            System.out.println(value);
            chara.setValue(value);
            if (!this.gatt.writeCharacteristic(chara)) {
                System.out.println("gatt.writeCharacteristic() return fal");
                this.onFail(-1, "gatt.writeCharacteristic() return false");
            }
        } catch (Throwable tr) {
            System.out.println("error");
            this.onFail(-1, tr.getMessage());
        }
    }

    public void readCharacteristic(UUID serviceUUID, UUID uuid, ActionCallback callback) {
        System.out.println("read charateristics");
        try {
            if (null == gatt) {
                System.out.println( "connect to miband first");
                throw new Exception("connect to miband first");
            }
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara = getCharacteristic(uuid);
            if (null == chara) {
                this.onFail(-1, "BluetoothGattCharacteristic " + uuid + " is not exsit");
                return;
            }
            if (!this.gatt.readCharacteristic(chara)) {
                this.onFail(-1, "gatt.readCharacteristic() return false");
            }
        } catch (Throwable tr) {
            System.out.println(tr);
            this.onFail(-1, tr.getMessage());
        }
    }

    public void readCharacteristic(UUID uuid, ActionCallback callback) {
        this.readCharacteristic(Profile.UUID_SERVICE_MILI, uuid, callback);
    }

    public void authorizasion2(UUID serviceUUID, UUID serviceAuthCharUUID, UUID serviceAuthDEscrUUID, UUID uuid, ActionCallback callback) {
        System.out.println(serviceUUID +", "+ serviceAuthCharUUID+", "+ serviceAuthDEscrUUID);
        try {
            BluetoothGattService service = gatt.getService(serviceUUID);
            System.out.println();
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(serviceAuthCharUUID);
            if (null == characteristic) {
                this.onFail(-1, "BluetoothGattCharacteristic " + uuid + " is not exsit");
                return;
            }
            gatt.setCharacteristicNotification(characteristic, true);
            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                if (descriptor.getUuid().equals(serviceAuthDEscrUUID)) {
                    System.out.println("Found NOTIFICATION BluetoothGattDescriptor: " + descriptor.getUuid().toString());
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                }
            }
            //characteristic.setValue(new byte[]{0x01, 0x8, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45});
            //characteristic.setValue(new byte[]{0x08, 0x01, 0x3c, 0x00, 0x04, 0x00, 0x15, 0x00, 0x00, 0x00, 0x00, 0x00});

            gatt.writeCharacteristic(characteristic);
            this.currentCallback = callback;
        } catch (Throwable tr) {
            System.out.println(tr);
            this.onFail(-1, tr.getMessage());
        }
    }

    public void authorizasion(UUID serviceUUID, UUID serviceAuthCharUUID, UUID serviceAuthDEscrUUID, UUID uuid, ActionCallback callback) {
        try {
            BluetoothGattService service = gatt.getService(serviceUUID);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(serviceAuthCharUUID);
            if (null == characteristic) {
                this.onFail(-1, "BluetoothGattCharacteristic " + uuid + " is not exsit");
                return;
            }
            boolean result = gatt.setCharacteristicNotification(characteristic, true);
            if (result) {
                BluetoothGattDescriptor notifyDescriptor = characteristic.getDescriptor(Profile.UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION);
                if (notifyDescriptor != null) {
                    int properties = characteristic.getProperties();
                    if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        System.out.println("use NOTIFICATION");
                        notifyDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        result = gatt.writeDescriptor(notifyDescriptor);
                    }
                } else {
                    System.out.println("Descriptor for characteristic " + characteristic.getUuid() + " is null");
                }
            }
            TimeUnit.SECONDS.sleep(2);
            byte[] requestAuthNumber = new byte[]{Profile.AUTH_REQUEST_RANDOM_AUTH_NUMBER, authFlags};

            if (characteristic.setValue(requestAuthNumber)) {
                gatt.writeCharacteristic(characteristic);
                System.out.println("auth number set");
            }
            this.currentCallback = callback;

        } catch (Throwable tr) {
            System.out.println(tr);
            this.onFail(-1, tr.getMessage());
        }
    }

    public void authorizasion(UUID uuid, ActionCallback callback){
        this.authorizasion(Profile.CUSTOM_SERVICE_FEE1, Profile.CUSTOM_SERVICE_AUTH_CHARACTERISTIC, Profile.CUSTOM_SERVICE_AUTH_DESCRIPTOR, uuid, callback);
    }

    public void deviceInfo(UUID uuid, ActionCallback callback){
        this.deviceInfo(Profile.UUID_CHARACTERISTIC_1_SENSOR_CONTROL,Profile.UUID_DESCRIPTOR_UPDATE_NOTIFICATION,Profile.UUID_CHARACTERISTIC_DEVICEEVENT, Profile.UUID_CHARACTERISTIC_7_REALTIME_STEPS, uuid, callback);
    }

    public void heartRate(ActionCallback callback){
        System.out.println("this done");
        callback = currentCallback;
    }

    public BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        synchronized (characteristicsMonitor) {
            if (mAvailableCharacteristics == null) {
                return null;
            }
            return mAvailableCharacteristics.get(uuid);
        }
    }

    public void sensorData(Boolean activate_disactivate, NotifyListener listener){
        UUID SERVICE = Profile.UUID_SERVICE_MILI;
        UUID SENSOR_DATA = Profile.UUID_CHARACTERISTIC_2_SENSOR_DATA;
        UUID SENSOR_CONTROL = Profile.UUID_CHARACTERISTIC_1_SENSOR_CONTROL;
        System.out.println("* Getting gatt service, UUID:" + SERVICE.toString());
        BluetoothGattService myGatService = gatt.getService(SERVICE/*UUID_SERVICE_MIBAND_SERVICE*/);
        if (myGatService != null) {
            BluetoothGattCharacteristic sensorData = myGatService.getCharacteristic(SENSOR_DATA/*UUID_BUTTON_TOUCH*/);
            final BluetoothGattCharacteristic sensorControl = myGatService.getCharacteristic(SENSOR_CONTROL/*Consts.UUID_BUTTON_TOUCH*/);
            boolean result = gatt.setCharacteristicNotification(sensorData, true);
            if (result) {
                BluetoothGattDescriptor notifyDescriptor = sensorData.getDescriptor(Profile.UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION);
                if (notifyDescriptor != null) {
                    int properties = sensorData.getProperties();
                    if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        System.out.println("use NOTIFICATION");
                        notifyDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(notifyDescriptor);
                    }
                } else {
                    System.out.println("Descriptor for characteristic " + sensorData.getUuid() + " is null");
                }
            } else {
                System.out.println("Unable to enable notification for ");
            }
            if(activate_disactivate){
                timer = new Timer();
                timer.schedule( new TimerTask() {
                    public void run() {
                        try{
                            Thread.sleep(100);
                        }
                        catch(InterruptedException ex){
                            Thread.currentThread().interrupt();
                        }
                        if (sensorControl.setValue(startSensorRead1)) {
                            gatt.writeCharacteristic(sensorControl);
                        }
                        try{
                            Thread.sleep(100);
                        }
                        catch(InterruptedException ex){
                            Thread.currentThread().interrupt();
                        }
                        if (sensorControl.setValue(startSensorRead2)) {
                            gatt.writeCharacteristic(sensorControl);
                        }
                    }
                }, 0, 10*1000);

            }else{
                timer.cancel();
                timer.purge();
                try{
                    Thread.sleep(100);
                }
                catch(InterruptedException ex){
                    Thread.currentThread().interrupt();
                }
                if (sensorControl.setValue(stopSensorRead)) {
                    gatt.writeCharacteristic(sensorControl);
                }
            }

        }
        this.notifyListeners.put(SENSOR_DATA, listener);
    }

    public void deviceInfo(UUID UUID_CHARACTERISTIC_1_SENSOR_CONTROL, UUID UUID_DESCRIPTOR_UPDATE_NOTIFICATION, UUID UUID_CHARACTERISTIC_DEVICEEVENT, UUID sensor, UUID uid, ActionCallback currentCallback) {
        BluetoothGattService myGatService = gatt.getService(Profile.DEVICE_INFORMATION_SERVICE/*Consts.UUID_SERVICE_MIBAND_SERVICE*/);
        BluetoothGattCharacteristic characteristic = myGatService.getCharacteristic(Profile.UUID_CHARACTERISTIC_DEVICE_INFO);
        gatt.setCharacteristicNotification(characteristic, true);
        gatt.readCharacteristic(characteristic);
        this.currentCallback = currentCallback;

    }


    public void setNotifyListener(UUID serviceUUID, UUID characteristicId, NotifyListener listener) {
        if (null == gatt) {
            System.out.println("connect to miband first");
            return;
        }
        BluetoothGattCharacteristic chara = gatt.getService(serviceUUID).getCharacteristic(characteristicId);
        if (chara == null) {
            System.out.println("characteristicId " + characteristicId.toString() + " not found in service " + serviceUUID.toString());
            return;
        }
        System.out.println("setNofityListener");
        System.out.println(chara);
        this.gatt.setCharacteristicNotification(chara, true);
        BluetoothGattDescriptor descriptor = chara.getDescriptor(Profile.UUID_DESCRIPTOR_UPDATE_NOTIFICATION);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        this.gatt.writeDescriptor(descriptor);
        this.notifyListeners.put(characteristicId, listener);
    }
    private void gattServicesDiscovered(List<BluetoothGattService> discoveredGattServices) {
        if (discoveredGattServices == null) {
            System.out.println("No gatt services discovered: null!");
            return;
        }
        Set<UUID> supportedServices = getSupportedServices();
        Map<UUID, BluetoothGattCharacteristic> newCharacteristics = new HashMap<>();
        for (BluetoothGattService service : discoveredGattServices) {
            if (!supportedServices.contains(service.getUuid())) {
                System.out.println("discovered supported service: " + BleNamesResolver.resolveServiceName(service.getUuid().toString()) + ": " + service.getUuid());
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                if (characteristics == null || characteristics.isEmpty()) {
                    System.out.println("Supported LE service " + service.getUuid() + "did not return any characteristics");
                    continue;
                }
                HashMap<UUID, BluetoothGattCharacteristic> intmAvailableCharacteristics = new HashMap<>(characteristics.size());
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    intmAvailableCharacteristics.put(characteristic.getUuid(), characteristic);
                }
                newCharacteristics.putAll(intmAvailableCharacteristics);

                synchronized (characteristicsMonitor) {
                    mAvailableCharacteristics = newCharacteristics;
                }
            } else {
                System.out.println("discovered unsupported service: " + BleNamesResolver.resolveServiceName(service.getUuid().toString()) + ": " + service.getUuid());
            }
        }
    }
    private void listenHeartRate(){
        BluetoothGattService myGatService = gatt.getService(Profile.UUID_SERVICE_HEARTRATE);
        if (myGatService != null) {
            System.out.println("listening heart rate");
            BluetoothGattCharacteristic heartRateChar = myGatService.getCharacteristic(Profile.UUID_NOTIFICATION_HEARTRATE);
            gatt.setCharacteristicNotification(heartRateChar, true);
            BluetoothGattDescriptor descriptor = heartRateChar.getDescriptor(Profile.CUSTOM_SERVICE_AUTH_DESCRIPTOR);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }else{
            System.out.println("service is null");
        }
    }


    private void notifyListenHeartRate(NotifyListener listener){
        BluetoothGattCharacteristic chara = gatt.getService(Profile.UUID_SERVICE_HEARTRATE).getCharacteristic(Profile.UUID_NOTIFICATION_HEARTRATE);
        gatt.setCharacteristicNotification(chara, true);
        BluetoothGattDescriptor descriptor = chara.getDescriptor(Profile.UUID_DESCRIPTOR_UPDATE_NOTIFICATION);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
        notifyListeners.put(Profile.UUID_NOTIFICATION_HEARTRATE, listener);
        System.out.println("Notified.....");
    }

    void startScanHeartRate(NotifyListener listener) {
        BluetoothGattService myGatService = gatt.getService(Profile.UUID_SERVICE_HEARTRATE);
        if (myGatService != null) {
            System.out.println("listening heart rate");
            BluetoothGattCharacteristic heartRateChar = myGatService.getCharacteristic(Profile.UUID_NOTIFICATION_HEARTRATE);
            gatt.setCharacteristicNotification(heartRateChar, true);
            BluetoothGattDescriptor descriptor = heartRateChar.getDescriptor(Profile.CUSTOM_SERVICE_AUTH_DESCRIPTOR);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }else{
            System.out.println("service is null");
        }
        try{
            Thread.sleep(500);
        }catch (InterruptedException e){

        }
        System.out.println("start heart scan button clicked");
        BluetoothGattCharacteristic bchar = gatt.getService(Profile.UUID_SERVICE_HEARTRATE)
                .getCharacteristic(Profile.UUID_CHAR_HEARTRATE);
        System.out.println(bchar);
        bchar.setValue(startHeartMeasurementContinuous);
        gatt.writeCharacteristic(bchar);
        this.notifyListeners.put(Profile.UUID_NOTIFICATION_HEARTRATE, listener);
        //private static final byte[] startHeartMeasurementContinuous = new byte[]{0x15, MiBandService.COMMAND_SET__HR_CONTINUOUS, 1};

    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        System.out.println("state changed");
        super.onConnectionStateChange(gatt, status, newState);

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices();
        }
        else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            gatt.close();
            if (this.disconnectedListener != null)
                this.disconnectedListener.onNotify(null);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        System.out.println("Characteristict read");
        super.onCharacteristicRead(gatt, characteristic, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(characteristic);
        } else {
            this.onFail(status, "onCharacteristicRead fail");
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(characteristic);
        } else {
            this.onFail(status, "onCharacteristicWrite fail");
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(rssi);
        } else {
            this.onFail(status, "onCharacteristicRead fail");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        gattServicesDiscovered(gatt.getServices());
        if (status == BluetoothGatt.GATT_SUCCESS) {
            this.gatt = gatt;
            this.onSuccess(null);
        } else {
            this.onFail(status, "onServicesDiscovered fail");
        }
        System.out.println("onservicesDiscovered");
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] requestAuthNumber = new byte[]{Profile.AUTH_REQUEST_RANDOM_AUTH_NUMBER, authFlags};
        super.onCharacteristicChanged(gatt, characteristic);
        if (this.notifyListeners.containsKey(characteristic.getUuid())) {
            //this.notifyListeners.get(characteristic.getUuid()).onNotify(characteristic.getValue());
        }
        UUID characteristicUUID = characteristic.getUuid();
        if (Profile.UUID_CHARACTERISTIC_2_SENSOR_DATA.equals(characteristicUUID)) {
            this.notifyListeners.get(characteristic.getUuid()).onNotify(characteristic.getValue());
            //this.notifyListeners.get(characteristic.getUuid()).onNotify(characteristic.getValue());
            //handleSensorData(characteristic.getValue());
            //this.onSuccess("aaa");
            return;
        }
        if(Profile.UUID_NOTIFICATION_HEARTRATE.equals(characteristicUUID)){
            this.notifyListeners.get(characteristic.getUuid()).onNotify(characteristic.getValue());
            //handleHeartRate(characteristic);
            return;
        }
        if (Profile.UUID_CHARACTERISTIC_AUTH.equals(characteristicUUID)) {
            try {
                byte[] value = characteristic.getValue();
                if (value[0] == Profile.AUTH_RESPONSE &&
                        value[1] == Profile.AUTH_SEND_KEY &&
                        value[2] == Profile.AUTH_SUCCESS) {
                    if (characteristic.setValue(requestAuthNumber)) {
                        gatt.writeCharacteristic(characteristic);
                        System.out.println("this is first one");
                    }
                }
                else if (value[0] == Profile.AUTH_RESPONSE &&
                        value[1] == Profile.AUTH_REQUEST_RANDOM_AUTH_NUMBER &&
                        value[2] == Profile.AUTH_SUCCESS) {
                    byte[] eValue = handleAESAuth(value, getSecretKey());
                    byte[] responseValue = org.apache.commons.lang3.ArrayUtils.addAll(
                            new byte[]{Profile.AUTH_SEND_ENCRYPTED_AUTH_NUMBER, authFlags}, eValue);
                    if (characteristic.setValue(responseValue)) {
                        gatt.writeCharacteristic(characteristic);
                    }
                }
                else if (value[0] == Profile.AUTH_RESPONSE &&
                        value[1] == Profile.AUTH_SEND_ENCRYPTED_AUTH_NUMBER &&
                        value[2] == Profile.AUTH_SUCCESS) {
                        System.out.println("second else");
                }
                else {
                    System.out.println("non of the if");
                }
            } catch (Exception e) {
                System.out.println("Error authenticating Mi Band 2");
            }
        } else {
            System.out.println("Unhandled characteristic changed");
        }
    }

    private void onSuccess(Object data) {
        if (this.currentCallback != null) {
            ActionCallback callback = this.currentCallback;
            this.currentCallback = null;
            callback.onSuccess(data);
        }
    }

    private void onFail(int errorCode, String msg) {
        if (this.currentCallback != null) {
            ActionCallback callback = this.currentCallback;
            this.currentCallback = null;
            callback.onFail(errorCode, msg);
        }
    }
    private byte[] handleAESAuth(byte[] value, byte[] secretKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
        byte[] mValue = Arrays.copyOfRange(value, 3, 19);
        Cipher ecipher = Cipher.getInstance("AES/ECB/NoPadding");
        SecretKeySpec newKey = new SecretKeySpec(secretKey, "AES");
        ecipher.init(Cipher.ENCRYPT_MODE, newKey);
        byte[] enc = ecipher.doFinal(mValue);
        return enc;
    }

    private byte[] getSecretKey() {
        return new byte[]{0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45};
    }

    private void handleSensorData(byte[] value) {
        int counter=0, step=0;
        double xAxis=0.0, yAxis=0.0, zAxis=0.0;
        double scale_factor = 250.0;
        //double gravity = 9.81;
        double gravity = 10;

        if ((value.length - 2) % 6 != 0) {
            System.out.println("wrong value");
        }
        else {
            counter = (value[0] & 0xff) | ((value[1] & 0xff) << 8);
            for (int idx = 0; idx < ((value.length - 2) / 6); idx++) {
                step = idx * 6;
                // Analyse X-axis data
                int xAxisRawValue = (value[step+2] & 0xff) | ((value[step+3] & 0xff) << 8);
                int xAxisSign = (value[step+3] & 0x30) >> 4;
                if (xAxisSign == 0) {
                    xAxis = xAxisRawValue & 0xfff;
                }
                else {
                    xAxis = (xAxisRawValue & 0xfff) - 4097;
                }
                xAxis = (xAxis*1.0 / scale_factor) * gravity;
                // Analyse Y-axis data
                int yAxisRawValue = (value[step+4] & 0xff) | ((value[step+5] & 0xff) << 8);
                int yAxisSign = (value[step+5] & 0x30) >> 4;
                if (yAxisSign == 0) {
                    yAxis = yAxisRawValue & 0xfff;
                }
                else {
                    yAxis = (yAxisRawValue & 0xfff) - 4097;
                }
                yAxis = (yAxis / scale_factor) * gravity;
                // Analyse Z-axis data
                int zAxisRawValue = (value[step+6] & 0xff) | ((value[step+7] & 0xff) << 8);
                int zAxisSign = (value[step+7] & 0x30) >> 4;
                if (zAxisSign == 0) {
                    zAxis = zAxisRawValue & 0xfff;
                }
                else {
                    zAxis = (zAxisRawValue & 0xfff) - 4097;
                }
                zAxis = (zAxis / scale_factor) * gravity;
                //mainView.plot_mi_band(xAxis, yAxis, zAxis);
//                realtime_diagram rd = new realtime_diagram();
//                rd.mi_band_plot(xAxis-1, yAxis-1, zAxis-1);
                System.out.println("x-axis:"+ String.format("%.03f",xAxis-2)+" y-axis:"+String.format("%.03f",yAxis-1)+" z-axis:"+String.format("%.03f",zAxis-1)+";");
                double result = Math.sqrt(Math.pow(Math.abs(xAxis-1), 2) + Math.pow(Math.abs(yAxis-1), 2) + Math.pow(Math.abs(zAxis-1), 2));
                //System.out.println(String.format("%.03f",result));
                if(result > 20){
                    //mainView.showToastMethod();
                }
            }
        }
    }

    public void handleHeartRate(final BluetoothGattCharacteristic characteristic){
        byte[] data = characteristic.getValue();
        for (byte i: data){
            System.out.println("hearth data "+ i);
        }
    }

    public static void showToastMethod(Context context) {
        Toast.makeText(context, "mymessage ", Toast.LENGTH_SHORT).show();
    }
}
