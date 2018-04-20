import java.io.Serializable;
import java.util.*;

public class User implements Serializable {
    public static final long serialVersionUID = 1L;

    public String userName;
    public String passWord;
    public String ip;
    public Vector<MailConstants> mailBox = new Vector<MailConstants>();

    public User(String user, String pass) {
        userName = user;
        passWord = pass;
    }


    public String getUserName(){return userName;}
    public String getPassWord(){return passWord;}
    public String getIP(){return ip;}
    public Vector getEmail(){return mailBox;}

    public void setUserName(String name){userName = name;}
    public void setPassWord(String pass){passWord = pass;}
    public void setIP(String address){ip = address;}

}
