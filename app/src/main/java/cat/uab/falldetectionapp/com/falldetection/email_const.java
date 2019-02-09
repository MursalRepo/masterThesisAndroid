package cat.uab.falldetectionapp.com.falldetection;

public class email_const {

    Integer email_id = 1;
    String email = "test@gmail.com";

    public email_const(){

    }

    public email_const(Integer email_id, String email) {
        this.email_id = email_id;
        this.email = email;
    }

    public Integer getEmail_id() {
        return email_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail_id(Integer email_id) {
        this.email_id = email_id;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
