package cat.uab.falldetectionapp.com.falldetection.model;

import java.util.UUID;

public class Profile {
    public static final UUID UUID_SERVICE_MILI = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHARACTERISTIC_AUTH = UUID.fromString("00000009-0000-3512-2118-0009af100700");
    public static final UUID UUID_SERVICE_HEARTRATE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");

    public static final UUID UUID_DESCRIPTOR_UPDATE_NOTIFICATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final UUID UUID_NOTIFICATION_HEARTRATE = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    public static final byte COMMAND_SET__HR_CONTINUOUS = 0x1;
    public static final byte COMMAND_SET_HR_MANUAL = 0x2;


    public static final UUID UUID_CHARACTERISTIC_8_USER_SETTINGS = UUID.fromString("00000008-0000-3512-2118-0009af100700");

    public static final UUID UUID_CHAR_CONTROL_POINT = UUID.fromString("0000ff05-0000-1000-8000-00805f9b34fb");

    public static final UUID UUID_CHARACTERISTIC_6_BATTERY_INFO = UUID.fromString("00000006-0000-3512-2118-0009af100700");
    public static UUID CUSTOM_SERVICE_FEE1 = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb");
    public static UUID CUSTOM_SERVICE_AUTH_CHARACTERISTIC = UUID.fromString("00000009-0000-3512-2118-0009af100700");
    public static UUID CUSTOM_SERVICE_AUTH_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static UUID UUID_CHARACTERISTIC_7_REALTIME_STEPS = UUID.fromString("00000007-0000-3512-2118-0009af100700");
    public static UUID UUID_CHARACTERISTIC_1_SENSOR_CONTROL = UUID.fromString("00000001-0000-3512-2118-0009af100700");
    public static UUID UUID_CHARACTERISTIC_2_SENSOR_DATA = UUID.fromString("00000002-0000-3512-2118-0009af100700");
    public static final String BASE_UUID = "0000%s-0000-1000-8000-00805f9b34fb";
    public static final UUID UUID_CHARACTERISTIC_DEVICEEVENT = UUID.fromString("00000010-0000-3512-2118-0009af100700");
    public static final UUID UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION = UUID.fromString((String.format(BASE_UUID, "2902")));
    public static final UUID UUID_CHAR_HEARTRATE = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHARACTERISTIC_DEVICE_INFO = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb");
    public static UUID DEVICE_INFORMATION_SERVICE = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    public static final byte AUTH_SEND_KEY = 0x01;
    public static final byte AUTH_RESPONSE = 0x10;
    public static final byte AUTH_SUCCESS = 0x01;
    public static final byte AUTH_BYTE = 0x08;
    public static final byte AUTH_REQUEST_RANDOM_AUTH_NUMBER = 0x02;
    public static final byte AUTH_SEND_ENCRYPTED_AUTH_NUMBER = 0x03;
}
