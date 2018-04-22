import java.io.*;
import java.util.*;

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
            User brennan = new User("brennan", "123");
            brennan.getEmail().add(new MailConstants(false, "Brennan", "Welcome Aboard", "None", "Today", "Welcome", "Welcome to our email server!"));


            userOut.writeObject(brennan);//print object to object file
            userFos.close();//close output stream
        }
        /**@throws Exception - this catch is necessary for the try above*/
        catch(Exception e){System.out.println("Exception : " + e);}

    }


}