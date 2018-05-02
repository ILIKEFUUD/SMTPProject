import javax.swing.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Relay implements Runnable{

    String mailToName = null;
    String ip = null;
    MailConstants newEmail = null;
    String userName = "server";
    String passWord = "server";
    
    private Socket socket = null;
    private PrintWriter pwt = null;
    private Scanner scan = null;

    /* Relay
     * @parem _userName -sets userName to _userName
     * @parem _ip -sets ip to _ip
     * @parem _newEmail -sets newEmail to _newEmail
     */
    public Relay(String _mailToName, String _ip, MailConstants _newEmail){
        mailToName = _mailToName;
        ip = _ip;
        newEmail = _newEmail;
        
    }

    public void run(){
        try {
            socket = new Socket(ip, 42069);
            pwt = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            scan = new Scanner(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error relaying message" + e);
        }
        SMTPSend(newEmail);
    }
    
    private void doLogin(){
    	
    	pwt.println(userName);
    	pwt.flush();
    	pwt.println(passWord);
    	pwt.flush();
    	
    	if (!scan.nextLine().equals("ACCEPTED")){
    		JOptionPane.showMessageDialog(null, this, "Login Failed", 0);
    		return;
    	}
    	else{
    		pwt.println("LOGGED IN");
    		pwt.flush();
    		if(!scan.nextLine().contains("220"))
    			JOptionPane.showMessageDialog(null, this, "Login Failed", 0);
    		else
    			SMTPSend(newEmail);
    	}
    }
    
    public void SMTPSend(MailConstants email){
        //when sending email, say HELO first
        try{
            System.out.println("running send");

            pwt.println("HELO server@"); //REORGANIZE SO WE CAN ACCESS CLIENT VARS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            pwt.flush();
            //get reply
            String reply =  scan.nextLine();
            System.out.println("reading in reply");
            if(reply.substring(0,3).equals("250")){

                //ok to send the from
                String send = "MAIL FROM:<" + email.getFrom() + ">";

                pwt.println(send);
                pwt.flush();
                //get reply
                reply =  scan.nextLine();
                if(reply.substring(0,3).equals("250")){
                    //ok to send to's
                    //send the one 'to'
                    pwt.println("RCPT TO:<" + email.getTo() + ">");
                    pwt.flush();

                    //now send DATA, then send actual email stuff
                    reply =  scan.nextLine();
                    if(reply.substring(0,3).equals("250")){
                        //ok to send DATA
                        //tell server I am sending an email over
                        pwt.println("DATA");
                        pwt.flush();
                        reply =  scan.nextLine();
                        if(reply.substring(0,3).equals("354")){
                            //send every data field in MailConstants
                            //send if encrypted
                            if(email.getEncrypted()){
                                pwt.println("_ENCRYPTED_");
                                pwt.flush();
                            }else{
                                pwt.println("_NOT_ENCRYPTED_");
                                pwt.flush();
                            }
                            pwt.println("From:" + email.getFrom());
                            pwt.flush();

                            pwt.println("To:" + email.getTo());
                            pwt.flush();

                            pwt.println("Cc:" + email.getCC());
                            pwt.flush();

                            pwt.println("Date:" + email.getDate());
                            pwt.flush();
     
                            pwt.println("Subject:" + email.getSubject());
                            pwt.flush();

                            //send message
                            String[] message = email.getMessage().split("\n");
                            for(String line : message){//send message line by line
                                pwt.println(line);
                                pwt.flush();
                            }
                            //endof message
                            //send the carriage return lf
                            System.out.println("end of message");
                            pwt.println("\r\n.\r\n"); //SMTP required end of message ~~~~~~
                            pwt.flush();
                            System.out.println("should have returned");
                            //see if server responded with OK
                            reply =  scan.nextLine();
                            if(reply.substring(0,3).equals("250")){
                                //reply with QUIT
                                pwt.println("QUIT");
                                pwt.flush();

                                //server replies with 221 bye
                                reply =  scan.nextLine();
                                if(reply.substring(0,3).equals("221")){
                                    //done sending email
                                    System.out.println("should dispose");

                                }

                            }else{
                                System.out.println("broke");
                            }
                        }else{
                            System.out.println("broke");
                        }


                    }else{
                        System.out.println("broke");
                    }

                }else{
                    System.out.println("broke");

                }
            }else{
                //SERVICE NOT AVAILABLE
                System.out.println(reply);
                return;//break out of send
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}