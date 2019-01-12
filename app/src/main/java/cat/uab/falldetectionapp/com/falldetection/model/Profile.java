package cat.uab.falldetectionapp.com.falldetection.model;

import java.util.UUID;

public class Profile {
    // ========================== 服务部分 ============================
    /**
     * 主要的service
     */
    public static final UUID UUID_SERVICE_MILI = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_BATTERYINFO_MI2 = UUID.fromString("00000006-0000-3512-2118-0009af100700");
    public static final UUID UUID_last_TEST = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHARACTERISTIC_AUTH = UUID.fromString("00000009-0000-3512-2118-0009af100700");

    /**
     * 震动
     */
    public static final UUID UUID_SERVICE_VIBRATION = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");

    /**
     * 心率
     */
    public static final UUID UUID_SERVICE_HEARTRATE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");


    /**
     * 未知作用
     */
    public static final UUID UUID_SERVICE_UNKNOWN1 = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_UNKNOWN2 = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_UNKNOWN4 = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_UNKNOWN5 = UUID.fromString("0000fee7-0000-1000-8000-00805f9b34fb");
    // ========================== 服务部分 end ============================

    // ========================== 描述部分 ============================
    public static final UUID UUID_DESCRIPTOR_UPDATE_NOTIFICATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final UUID UUID_NOTIFICATION_HEARTRATE = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    public static final byte COMMAND_SET__HR_CONTINUOUS = 0x1;

    // ========================== 描述部分 end ============================

    // ========================== 特性部分 ============================
    public static final UUID UUID_CHAR_DEVICE_INFO = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_DEVICE_NAME = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");
    /**
     * 通用通知
     */
    public static final UUID UUID_CHAR_NOTIFICATION = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");

    /**
     * 用户信息，读写
     */
    public static final UUID UUID_CHAR_USER_INFO = UUID.fromString("0000ff04-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHARACTERISTIC_8_USER_SETTINGS = UUID.fromString("00000008-0000-3512-2118-0009af100700");
    /**
     * 控制,如震动等
     */
    public static final UUID UUID_CHAR_CONTROL_POINT = UUID.fromString("0000ff05-0000-1000-8000-00805f9b34fb");

    /**
     * 实时步数通知 通知
     */
    public static final UUID UUID_CHAR_REALTIME_STEPS = UUID.fromString("0000ff06-0000-1000-8000-00805f9b34fb");


    public static final UUID UUID_CHAR_ACTIVITY = UUID.fromString("0000ff07-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_FIRMWARE_DATA = UUID.fromString("0000ff08-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_LE_PARAMS = UUID.fromString("0000ff09-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_DATA_TIME = UUID.fromString("0000ff0a-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_STATISTICS = UUID.fromString("0000ff0b-0000-1000-8000-00805f9b34fb");

    /**
     * 电池,只读,通知
     */
    public static final UUID UUID_CHAR_BATTERY = UUID.fromString("0000ff0c-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHARACTERISTIC_6_BATTERY_INFO = UUID.fromString("00000006-0000-3512-2118-0009af100700");

    /**
     * 自检,读写
     */
    public static final UUID UUID_CHAR_TEST = UUID.fromString("0000ff0d-0000-1000-8000-00805f9b34fb");

    /**
     * 配对,读写
     */
    public static final UUID UUID_CHAR_SENSOR_DATA = UUID.fromString("0000ff0e-0000-1000-8000-00805f9b34fb");

    /**
     * 配对,读写
     */

    public static final UUID UUID_CHAR_PAIR = UUID.fromString("0000ff0f-0000-1000-8000-00805f9b34fb");
    public static UUID CUSTOM_SERVICE_FEE1 = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb");
    public static UUID CUSTOM_SERVICE_AUTH_CHARACTERISTIC = UUID.fromString("00000009-0000-3512-2118-0009af100700");
    public static UUID CUSTOM_SERVICE_AUTH_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static UUID DEVICE_INFORMATION_SERVICE = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static UUID UUID_CHARACTERISTIC_7_REALTIME_STEPS = UUID.fromString("00000007-0000-3512-2118-0009af100700");
    public static UUID UUID_CHARACTERISTIC_1_SENSOR_CONTROL = UUID.fromString("00000001-0000-3512-2118-0009af100700");
    public static UUID UUID_CHARACTERISTIC_2_SENSOR_DATA = UUID.fromString("00000002-0000-3512-2118-0009af100700");
    public static final String BASE_UUID = "0000%s-0000-1000-8000-00805f9b34fb";
    public static final UUID UUID_SERVICE_MIBAND2_SERVICE = UUID.fromString(String.format(BASE_UUID, "FEE1"));
    public static final UUID UUID_CHARACTERISTIC_DEVICEEVENT = UUID.fromString("00000010-0000-3512-2118-0009af100700");
    public static final UUID UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION = UUID.fromString((String.format(BASE_UUID, "2902")));
    /**
     * 震动
     */
    public static final UUID UUID_CHAR_VIBRATION = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");

    /**
     * 心率
     */
    public static final UUID UUID_CHAR_HEARTRATE = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb");
    // ========================== 特性部分 end ============================

    public static final UUID UUID_SERVICE_GENERIC = UUID.fromString(String.format(BASE_UUID, "1800"));
    public static final UUID UUID_SERVICE_MIBAND_SERVICE = UUID.fromString(String.format(BASE_UUID, "FEE0"));
    //public static final UUID UUID_SERVICE_MIBAND2_SERVICE = UUID.fromString(String.format(BASE_UUID, "FEE1"));
    public static final UUID UUID_SERVICE_HEARTBEAT = UUID.fromString(String.format(BASE_UUID, "180D"));

    // General service
    public static final UUID UUID_CHARACTERISTIC_DEVICE_NAME = UUID.fromString(String.format(BASE_UUID, "2A00"));
    public static final UUID UUID_CHARACTERISTIC_DEVICE_INFO = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb");

    // Miband service 1
    public static final UUID UUID_BUTTON_TOUCH = UUID.fromString("00000010-0000-3512-2118-0009af100700");
    public static final UUID UUID_START_HEARTRATE_CONTROL_POINT = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb");
    public static final byte[] BYTE_LAST_HEART_RATE_SCAN = {21, 1, 1};
    public static final byte[] BYTE_NEW_HEART_RATE_SCAN = {21, 2, 1};

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        //http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
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
    // [3, 8, 23, -37, -41, -128, 8, -88, -38, 21, 6, 110, -1, -4, -7, -128, 81, -23]
    // [1, 8, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 64, 65, 66, 67, 68, 69]

//    discovered supported service: Generic Access: 00001800-0000-1000-8000-00805f9b34fb
//    2018-12-16 16:09:46.251 23618-23631/miband2.uabthesis.com.myproject I/System.out: discovered supported service: Generic Attribute: 00001801-0000-1000-8000-00805f9b34fb
//    2018-12-16 16:09:46.251 23618-23631/miband2.uabthesis.com.myproject I/System.out: discovered supported service: Device Information: 0000180a-0000-1000-8000-00805f9b34fb
//    2018-12-16 16:09:46.251 23618-23631/miband2.uabthesis.com.myproject I/System.out: discovered supported service: (Propr: Xiaomi Weight Service): 00001530-0000-3512-2118-0009af100700
//    2018-12-16 16:09:46.251 23618-23631/miband2.uabthesis.com.myproject I/System.out: discovered supported service: Alert Notification Service: 00001811-0000-1000-8000-00805f9b34fb
//    2018-12-16 16:09:46.251 23618-23631/miband2.uabthesis.com.myproject I/System.out: discovered supported service: Immediate Alert: 00001802-0000-1000-8000-00805f9b34fb
//    2018-12-16 16:09:46.253 23618-23631/miband2.uabthesis.com.myproject I/System.out: discovered supported service: Heart Rate: 0000180d-0000-1000-8000-00805f9b34fb
//    2018-12-16 16:09:46.253 23618-23631/miband2.uabthesis.com.myproject I/System.out: discovered supported service: Unknown Service: 0000fee0-0000-1000-8000-00805f9b34fb
//    2018-12-16 16:09:46.254 23618-23631/miband2.uabthesis.com.myproject I/System.out: discovered supported service: Unknown Service: 0000fee1-0000-1000-8000-00805f9b34fb
}
