package cat.uab.falldetectionapp.com.falldetection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    byte authFlags = Profile.AUTH_BYTE;
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
            System.out.println(chara);
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
//            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
//                if (descriptor.getUuid().equals(serviceAuthDEscrUUID)) {
//                    System.out.println("Found Notify: " + descriptor.getUuid().toString());
//                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                    result = gatt.writeDescriptor(descriptor);
//                }
//            }
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
            System.out.println(result);
            TimeUnit.SECONDS.sleep(1);
            //characteristic.setValue(new byte[]{0x01, 0x8, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45});
                                                // [3, 8, -57, 115, -112, 112, -107, 60, -64, -40, -122, 100, 38, -83, 51, -102, -55, -31]
            //characteristic.setValue(new byte[]{0x08, 0x01, 0x3c, 0x00, 0x04, 0x00, 0x15, 0x00, 0x00, 0x00, 0x00, 0x00});
            //gatt.writeCharacteristic(characteristic);

            //builder.write(getCharacteristic(HuamiService.UUID_CHARACTERISTIC_AUTH), requestAuthNumber());
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

    public void sensorData(ActionCallback currentCallback){
        UUID SERVICE = Profile.UUID_SERVICE_MILI;
        UUID SENSOR_DATA = Profile.UUID_CHARACTERISTIC_2_SENSOR_DATA;
        UUID SENSOR_CONTROL = Profile.UUID_CHARACTERISTIC_1_SENSOR_CONTROL;


        System.out.println("* Getting gatt service, UUID:" + SERVICE.toString());
        BluetoothGattService myGatService = gatt.getService(SERVICE/*UUID_SERVICE_MIBAND_SERVICE*/);
        if (myGatService != null) {
            BluetoothGattCharacteristic sensorData = myGatService.getCharacteristic(SENSOR_DATA/*UUID_BUTTON_TOUCH*/);
            BluetoothGattCharacteristic sensorControl = myGatService.getCharacteristic(SENSOR_CONTROL/*Consts.UUID_BUTTON_TOUCH*/);

            boolean result = gatt.setCharacteristicNotification(sensorData, true);
            if (result) {
                BluetoothGattDescriptor notifyDescriptor = sensorData.getDescriptor(Profile.UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION);
                if (notifyDescriptor != null) {
                    int properties = sensorData.getProperties();
                    if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        System.out.println("use NOTIFICATION");
                        notifyDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        result = gatt.writeDescriptor(notifyDescriptor);
                    }
                } else {
                    System.out.println("Descriptor for characteristic " + sensorData.getUuid() + " is null");
                }
            } else {
                System.out.println("Unable to enable notification for ");
            }
            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }

            System.out.println("* Statring listening");
            // second parametes is for starting\stopping the listener.
            //boolean status =  gatt.setCharacteristicNotification(sensorData, true);
            System.out.println("* Set notification status :" + result);
            if (sensorControl.setValue(startSensorRead1)) {
                BluetoothGattCharacteristic temp = myGatService.getCharacteristic(SENSOR_CONTROL/*Consts.UUID_BUTTON_TOUCH*/);
                gatt.writeCharacteristic(sensorControl);
                System.out.println("value 1 set");
            }

            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }

            if (sensorControl.setValue(startSensorRead2)) {
                BluetoothGattCharacteristic temp = myGatService.getCharacteristic(SENSOR_CONTROL/*Consts.UUID_BUTTON_TOUCH*/);
                gatt.writeCharacteristic(sensorControl);
                System.out.println("value 2 set");
            }

        }

//        BluetoothGattCharacteristic sensorData1 = getCharacteristic(SENSOR_DATA);
//        System.out.println(sensorData1);
//        if (sensorData1 != null) {
//                System.out.println("* Statring listening");
//                boolean status =  gatt.setCharacteristicNotification(sensorData1, true);
//                System.out.println("* Set notification status :" + status);
//                BluetoothGattCharacteristic sensorControl = getCharacteristic(SENSOR_CONTROL);
//                if (sensorControl.setValue(startSensorRead1)) {
//                    gatt.writeCharacteristic(sensorControl);
//                    System.out.println("value 1 setted");
//                }
//            if (sensorControl.setValue(startSensorRead2)) {
//                gatt.writeCharacteristic(sensorControl);
//                System.out.println("value 2 setted");
//            }
//            }
        System.out.println("this done");
        //this.currentCallback = currentCallback;
    }

    public void deviceInfo(UUID UUID_CHARACTERISTIC_1_SENSOR_CONTROL, UUID UUID_DESCRIPTOR_UPDATE_NOTIFICATION, UUID UUID_CHARACTERISTIC_DEVICEEVENT, UUID sensor, UUID uid, ActionCallback currentCallback) {
        UUID service = Profile.UUID_SERVICE_MIBAND_SERVICE;
        UUID char_ = Profile.UUID_BUTTON_TOUCH;
        System.out.println("* Getting gatt service, UUID:" + service.toString());
        BluetoothGattService myGatService = gatt.getService(service/*Consts.UUID_SERVICE_MIBAND_SERVICE*/);
        if (myGatService != null) {
            System.out.println("* Getting gatt Characteristic. UUID: " + char_.toString());
            BluetoothGattCharacteristic myGatChar = myGatService.getCharacteristic(char_/*Consts.UUID_BUTTON_TOUCH*/);
            if (myGatChar != null) {
                System.out.println("* Statring listening");
                // second parametes is for starting\stopping the listener.
                boolean status =  gatt.setCharacteristicNotification(myGatChar, true);
                System.out.println("* Set notification status :");
            }
        }
        //Consts.UUID_SERVICE_HEARTBEAT,
        //Consts.UUID_START_HEARTRATE_CONTROL
        // _POINT,
        //Consts.BYTE_NEW_HEART_RATE_SCAN
//        UUID UUID_SERVICE_HEARTBEAT = Profile.UUID_SERVICE_HEARTBEAT;
//        UUID UUID_CHAR_HEARTRATE = Profile.UUID_CHAR_HEARTRATE;
//        System.out.println("* Getting gatt service, UUID:" + UUID_SERVICE_HEARTBEAT.toString());
//        BluetoothGattService myGatService2 = gatt.getService(UUID_SERVICE_HEARTBEAT /*Consts.UUID_SERVICE_HEARTBEAT*/);
//        if (myGatService2 != null) {
//            BluetoothGattCharacteristic myGatChar2 = myGatService2.getCharacteristic(UUID_CHAR_HEARTRATE /*Consts.UUID_START_HEARTRATE_CONTROL_POINT*/);
//            if (myGatChar2 != null) {
//                System.out.println("* Writing trigger");
//                myGatChar2.setValue(Profile.BYTE_NEW_HEART_RATE_SCAN /*Consts.BYTE_NEW_HEART_RATE_SCAN*/);
//                boolean status =  gatt.writeCharacteristic(myGatChar2);
//                System.out.println("* Writting trigger status :" + status);
//            }
//        }

        //        System.out.println("device info");
//        BluetoothGattCharacteristic devic_evente = getCharacteristic(Profile.UUID_CHARACTERISTIC_2_SENSOR_DATA);
//        BluetoothGattDescriptor notifyDescriptor = devic_evente.getDescriptor(Profile.UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION);
//        notifyDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        gatt.writeDescriptor(notifyDescriptor);
//
//        BluetoothGattCharacteristic sensorData1 = getCharacteristic(Profile.UUID_CHARACTERISTIC_1_SENSOR_CONTROL);
//        if (sensorData1.setValue(startSensorRead1)) {
//            gatt.writeCharacteristic(sensorData1);
//        }
//        if (sensorData1.setValue(startSensorRead2)) {
//            gatt.writeCharacteristic(sensorData1);
//        }
        this.currentCallback = currentCallback;

        /// builder.write(getCharacteristic(HuamiService.UUID_CHARACTERISTIC_1_SENSOR_CONTROL), startSensorRead2);
        //BluetoothGattCharacteristic sensorData = getCharacteristic(sensor);

//        System.out.println(notifyDescriptor);
//        BluetoothGattCharacteristic sensor_control = getCharacteristic(UUID_CHARACTERISTIC_1_SENSOR_CONTROL);
//        System.out.println(startSensorRead1);
//        System.out.println(startSensorRead2);
//        System.out.println(sensor_control);
//        gatt.setCharacteristicNotification(sensorData, true);
//        gatt.setCharacteristicNotification(sensor_control, true);
//        for (BluetoothGattDescriptor descriptor : sensorData.getDescriptors()) {
//            if (descriptor.getUuid().equals(UUID_DESCRIPTOR_UPDATE_NOTIFICATION)) {
//                System.out.println("Found NOTIFICATION BluetoothGattDescriptor: " + descriptor.getUuid().toString());
//                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            }
//        }
//        sensorData.setValue(startSensorRead1);
//        sensorData.setValue(startSensorRead2);
//        sensor_control.setValue(startSensorRead1);
//        sensor_control.setValue(startSensorRead2);
//        byte [] value1 = sensorData.getValue();
//        System.out.println(Arrays.toString(value1));
//        for(byte a: value1){
//            System.out.println(a);
//        }
//        byte [] value2 = sensor_control.getValue();
//        System.out.println(Arrays.toString(value2));
//        for(byte a: value2){
//            System.out.println(a);
//        }


//        try {
//            BluetoothGattService service = gatt.getService(serviceUUID);
//            System.out.println(service);
//            BluetoothGattCharacteristic heartRateCharacteristic = service.getCharacteristic(NOTIFICATION_HEARTRATE);
//            BluetoothGattDescriptor heartRateDescriptor = heartRateCharacteristic.getDescriptor(CUSTOM_SERVICE_AUTH_DESCRIPTOR);
//            System.out.println(heartRateDescriptor);
//            gatt.setCharacteristicNotification(heartRateCharacteristic, true);
//            heartRateDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            gatt.writeDescriptor(heartRateDescriptor);
//            System.out.println(heartRateDescriptor);
//            byte[] data = heartRateCharacteristic.getValue();
//            System.out.println(heartRateCharacteristic.getValue());
////            System.out.println("length > " + data.length);
////            for (byte i: data){
////                System.out.println("hearth data "+ i);
////            }
////            System.out.println("heart > "+heartRateCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0).toString());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

//    public boolean run_notify(BluetoothGattDescriptor notifyDescriptor ) {
//            if (notifyDescriptor != null) {
//                int properties = getCharacteristic().getProperties();
//                if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                    LOG.debug("use NOTIFICATION");
//                    notifyDescriptor.setValue(enableFlag ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//                    result = gatt.writeDescriptor(notifyDescriptor);
//                } else if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
//                    LOG.debug("use INDICATION");
//                    notifyDescriptor.setValue(enableFlag ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//                    result = gatt.writeDescriptor(notifyDescriptor);
//                    hasWrittenDescriptor = true;
//                } else {
//                    hasWrittenDescriptor = false;
//                }
//            } else {
//                LOG.warn("Descriptor CLIENT_CHARACTERISTIC_CONFIGURATION for characteristic " + getCharacteristic().getUuid() + " is null");
//                hasWrittenDescriptor = false;
//            }
//        return result;
//    }

    public void readRssi(ActionCallback callback) {
        try {
            if (null == gatt) {
                System.out.println( "connect to miband first");
                throw new Exception("connect to miband first");
            }
            this.currentCallback = callback;
            this.gatt.readRemoteRssi();
        } catch (Throwable tr) {
            System.out.println(tr);
            this.onFail(-1, tr.getMessage());
        }

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
        System.out.println("all..");
        System.out.println(discoveredGattServices);
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

    void startScanHeartRate() {
        System.out.println("start heart scan button clicked");
        BluetoothGattCharacteristic bchar = gatt.getService(Profile.UUID_SERVICE_HEARTRATE)
                .getCharacteristic(Profile.UUID_NOTIFICATION_HEARTRATE);
        bchar.setValue(new byte[]{21, 2, 1});
        gatt.writeCharacteristic(bchar);
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
        System.out.println("on characteristics write");
        System.out.println(characteristic.getValue());
        System.out.println(status);
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
        System.out.println("discovered . . . ");
        System.out.println(gatt.getServices());
        //listenHeartRate();
        notifyListenHeartRate(new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                System.out.println("notified...");
                System.out.println("___");
                System.out.println(data);
            }
        });
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        System.out.println("onCharacteristicChanged");
        System.out.println(characteristic.getUuid());
        byte[] requestAuthNumber = new byte[]{Profile.AUTH_REQUEST_RANDOM_AUTH_NUMBER, authFlags};
        super.onCharacteristicChanged(gatt, characteristic);
        if (this.notifyListeners.containsKey(characteristic.getUuid())) {
            this.notifyListeners.get(characteristic.getUuid()).onNotify(characteristic.getValue());
        }
        UUID characteristicUUID = characteristic.getUuid();
        System.out.println(characteristicUUID);
        if (Profile.UUID_CHARACTERISTIC_2_SENSOR_DATA.equals(characteristicUUID)) {
            //handleSensorData(characteristic.getValue());
            System.out.println("yes this");
            System.out.println(characteristic.getValue());
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
                    // md5??
                    System.out.println("first else");
                    byte[] eValue = handleAESAuth(value, getSecretKey());
                    byte[] responseValue = org.apache.commons.lang3.ArrayUtils.addAll(
                            new byte[]{Profile.AUTH_SEND_ENCRYPTED_AUTH_NUMBER, authFlags}, eValue);
                    //byte[] responseValue = new byte[]{0x01, 0x8, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45};
                    if (characteristic.setValue(responseValue)) {
                        gatt.writeCharacteristic(characteristic);
                        System.out.println("this is first one");
                    }
                }
                else if (value[0] == Profile.AUTH_RESPONSE &&
                        value[1] == Profile.AUTH_SEND_ENCRYPTED_AUTH_NUMBER &&
                        value[2] == Profile.AUTH_SUCCESS) {
                    System.out.println("second else");
                    //getSupport().enableFurtherNotifications(builder, true);
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

}
