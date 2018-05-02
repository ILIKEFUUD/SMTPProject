import java.io.*;
import java.util.*;
import java.net.*;

public class addUsers implements Serializable{
    public static final long serialVersionUID = 1L;

    private File userFile = new File("user.obj");

    public static void main(String[] args){
        new addUsers();
    }

    public addUsers(){
        try{
            //File IO
            FileOutputStream userFos = new FileOutputStream(userFile);
            ObjectOutputStream userOut = new ObjectOutputStream(userFos);
            /*create new order*/
            User brennan = new User("test", "test");
            brennan.getEmail().add(new MailConstants(false, "Brennan", "Welcome Aboard", "None", "Today", "Welcome", "Welcome to our email server!"));
            User student = new User("student", "student");
            student.getEmail().add(new MailConstants(false, "Student", "Welcome Aboard", "None", "Today", "Welcome", "Welcome to our email server!"));
            User test = new User("test123", "test123");
            test.getEmail().add(new MailConstants(false, "Test", "Welcome Aboard", "None", "Today", "Welcome", "Welcome to our email server!"));
            User guest = new User("iste121", "iste121");
            guest.getEmail().add(new MailConstants(false, "Test", "Welcome Aboard", "None", "Today", "Welcome", "Welcome to our email server!"));
            User relay = new User("server","server");
            relay.getEmail().add(new MailConstants(false, "Test", "Welcome Aboard", "None", "Today", "Welcome", "Welcome to our email server!"));
            
            InetAddress IP = InetAddress.getLocalHost();

            brennan.setIP(IP.getHostAddress());
            student.setIP(IP.getHostAddress());
            test.setIP(IP.getHostAddress());

            userOut.writeObject(brennan);//print object to object file
            userOut.writeObject(student);
            userOut.writeObject(test);
            userFos.close();//close output stream
        }
        /**@throws Exception - this catch is necessary for the try above*/
        catch(Exception e){System.out.println("Exception : " + e);}

    }


}