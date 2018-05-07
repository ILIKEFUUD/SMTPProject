import java.io.*;
import java.util.*;
import java.net.*;

/*
 * @author - Jordan K. Albrecht, Brennan Jackson
 * addUsers - simple script called by the main SMTPServer to create a new user.obj
 * file each time the server is started
 */

public class addUsers implements Serializable{
   public static final long serialVersionUID = 1L;

   private File userFile = new File("user.obj");
   
   public void makeUsers(){
      try{
         //File IO
         FileOutputStream userFos = new FileOutputStream(userFile);
         ObjectOutputStream userOut = new ObjectOutputStream(userFos);
         /*create new user*/
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
         User ISTE = new User("ISTE121","ISTE121");
         ISTE.getEmail().add(new MailConstants(false, "Test", "Welcome Aboard", "None", "Today", "Welcome", "Welcome to our email server!"));
         
         InetAddress IP = InetAddress.getLocalHost();
      
         brennan.setIP(IP.getHostAddress());
         student.setIP(IP.getHostAddress());
         test.setIP(IP.getHostAddress());
         relay.setIP(IP.getHostAddress());
         guest.setIP(IP.getHostAddress());
         ISTE.setIP(IP.getHostAddress());
      
         userOut.writeObject(brennan);//print object to object file
         userOut.writeObject(student);
         userOut.writeObject(test);
         userOut.writeObject(guest);
         userOut.writeObject(ISTE);
         userOut.writeObject(relay);
         userFos.close();//close output stream
         System.out.println("DONE");
      }
      /**@throws Exception - this catch is necessary for the try above*/
      catch(Exception e){System.out.println("Exception : " + e);}
   
   }


}