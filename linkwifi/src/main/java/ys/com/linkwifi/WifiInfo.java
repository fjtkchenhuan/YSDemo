package ys.com.linkwifi;

public class WifiInfo {
    private String wifi;
    private String pwd;

    public String getWifi() {
        return wifi;
    }

    public void setWifi(String wifi) {
        this.wifi = wifi;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    @Override
    public String toString() {
        return "WifiInfo{" +
                "wifi='" + wifi + '\'' +
                ", pwd='" + pwd + '\'' +
                '}';
    }

}
