import java.io.*;
import java.util.*;

public class addUsers implements MailConstants{

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
            User brennan = new User();
            brennan.setUserName("brennan");
            brennan.setPassWord("12345");
            brennan.setIP("192.168.1.1");

            userOut.writeObject(brennan);//print object to object file
            userFos.close();//close output stream
        }
        /**@throws Exception - this catch is necessary for the try above*/
        catch(Exception e){System.out.println("Exception : " + e);}

    }


}