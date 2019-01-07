package cat.uab.falldetectionapp.com.falldetection;

public interface ActionCallback {
    public void onSuccess(Object data);

    public void onFail(int errorCode, String msg);
}
