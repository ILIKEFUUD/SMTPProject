import java.io.Serializable;
import java.util.*;


/**
 @author Ben, Brennan, Jordan, Rahul

 User.java
 User object used in the SMTP Sever. Used to verify login credentials and store user mail boxes. 
 Each user contains UserName, PassWord, IP address, and Mailbox attributes.
 */

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

   /**getUserName - returns User's username*/
   public String getUserName(){
      return userName;}
   
   /**getPassWord - returns User's password*/
   public String getPassWord(){
      return passWord;}
   
   /**getIP - returns User's ip address*/
   public String getIP(){
      return ip;}
   
   /**getEmail - returns User's mailbox*/
   public Vector getEmail(){
      return mailBox;}

   /**setUserName - sets User's username
   @param String - login username for user
   */
   public void setUserName(String name){userName = name;}
   
   /**setPassWord - sets User's password
   @param String - password for use
   */
   public void setPassWord(String pass){passWord = pass;}
   
   /**setIP - sets User's ip address
   @param String - ip address assigned to user
   */
   public void setIP(String address){ip = address;}
   
   /**addMail - adds an MailConstant to the users inbox
   @param MailConstants - Email to be added to mailbox
   */
   public void addMail(MailConstants mail){mailBox.add(mail);}

}
