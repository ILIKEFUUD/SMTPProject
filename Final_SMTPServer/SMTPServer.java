import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.text.*;

/*
 * @author - Jordan K. Albrecht, Brennan Jackson
 * SMTPServer - main server class that handles SMTP protocol for recieving, processing, and relaying 
 * simple emails
 */
 
public class SMTPServer extends JFrame implements ActionListener {

   private JTextArea jtaLog = new JTextArea(10, 35);
   private JButton jbStart = new JButton("Start");
   private JTextField jtfIP = new JTextField(16);
   private JLabel jlLog = new JLabel("Log:");
   private JPanel jpNorth = new JPanel(new FlowLayout(FlowLayout.CENTER));
   private JPanel jpCenter = new JPanel();
   private JPanel jpSouth = new JPanel(new FlowLayout(FlowLayout.CENTER));
   private ServerThread sThread;

   private Vector<User> users = new Vector<User>();//Vector for a users on the server
   private File userFile = new File("user.obj");//file that contains users

   private FIFOQueue<MailConstants> fifo = new FIFOQueue<MailConstants>();
   private FIFOHandler queue = new FIFOHandler();//Create new FIFOHandler

   public static int SERVER_PORT = 42069;
   private ServerSocket sSocket;

   public static void main(String[] args) {
      new SMTPServer();
   }

   public SMTPServer() {
      addUsers add = new addUsers();//create new user.obj file
      add.makeUsers();
      doLoadUsers();
      setupWindow();
   
      jpNorth.add(jbStart);//add jbStart to GUI
      this.add(jpNorth, BorderLayout.NORTH);
   
      /*The cursor in the log will constantly adjust to the lowest line*/
      DefaultCaret caret = (DefaultCaret) jtaLog.getCaret();
      caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
      jtaLog.setLineWrap(true);
      jtaLog.setWrapStyleWord(true);
      jtaLog.setEditable(false);
   
      jpCenter.add(jlLog);//add Log to the GUI
      jpCenter.add(new JScrollPane(jtaLog));
      this.add(jpCenter, BorderLayout.CENTER);
      
      try{
         jtfIP.setText("" + InetAddress.getLocalHost().getHostAddress());
         jtfIP.setEditable(false);
      }
      catch(Exception e){}
      jpSouth.add(jtfIP);
      this.add(jpSouth, BorderLayout.SOUTH);
      
      jbStart.addActionListener(this);
   
      /*If the window for the server GUI is closed the server threads are killed*/
      this.addWindowListener(
                new WindowAdapter() {
                   public void windowClosing(WindowEvent e) {
                      try {
                         doStop();
                      } catch (NullPointerException npe) {
                         System.exit(0);
                      }
                   }
                
                });
   
      this.setVisible(true);
   
   }

   /**
    * setupWindow - Sets parameters for the GUI window
    */
   public void setupWindow() {
   
      this.setTitle("SMTPServer");
      this.setSize(450, 300);
      this.setLocation(600, 50);
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }

   public void actionPerformed(ActionEvent ae) {
      switch (ae.getActionCommand()) {
         case "Start":
            doStart();
            break;
      
         case "Stop":
            doStop();
            break;
      }
   }

   /**
    * doStart - creates and starts new serverThread
    */
   public void doStart() {
      sThread = new ServerThread();
      sThread.start();
      jbStart.setText("Stop");
      try{
         queue.start();//When the server is started the FIFOHAndler will start as well. No Clients need to be connected to handle incoming mail
      }
      catch(Exception e){}
   }

   /**
    * doStop - calls kill method which ends the server
    */
   public void doStop() {
      sThread.kill();
      jbStart.setText("Start");
   
   }

   /**
    * doLoadUsers - searches for User.obj file and reads it in. Users from the file are laoded into a vector of users
    */
   public void doLoadUsers() {
      try {
         /*File IO to look for file*/
         FileInputStream userFis;
         ObjectInputStream userIn;
         /*get current directory*/
         String currentDir = System.getProperty("user.dir");
         String fileName = "user.obj";//set file name
      
         File tempFile = new File(fileName);//set file to orders.obj in current directory
      
         /*Check to see if user.obj exists and load objects into an array list. */
         if (tempFile.exists()) {
            userFis = new FileInputStream(tempFile);
            userIn = new ObjectInputStream(userFis);
            for (int i = 0; i < tempFile.length(); i++) {
               users.add((User) userIn.readObject());//read in and add users
            }
            userFis.close();//close input stream
         
         }
         /* if file doesn't exist display window for user*/
         else {
            JOptionPane.showMessageDialog(this, "Error: No User File Found");
         }
      }
      /**@throws Exception - this catch is necessary for the try above*/ 
      catch (Exception e) {
         System.out.println("Exception :" + e);
      }
   
   }

   /**
    * ServerThread - creates new server thread. Handles verification for client login
    */
   class ServerThread extends Thread {
      private boolean running = true;
      private ObjectOutputStream output;
      private ObjectInputStream input;
      private PrintWriter pwt;
      private Scanner scn;
      private User tempUser;
   
   
      public ServerThread() {
      }
   
      /*run - waits for client to connect to the socket. calls doCheck to verify user login creds*/
      public void run() {
      
         try {
            sSocket = new ServerSocket(SERVER_PORT);
         } catch (IOException ioe) {
            System.out.println(ioe);
         }
      
         while (running == true) {
            Socket cSocket = null;
         
            try {
               cSocket = sSocket.accept();
            
               pwt = new PrintWriter(new OutputStreamWriter(cSocket.getOutputStream()));
               scn = new Scanner(new InputStreamReader(cSocket.getInputStream()));
            
               /*read in username and password from the client*/
               String userName = scn.nextLine();
               String passWord = scn.nextLine();
            
               tempUser = doCheck(userName, passWord);//check creds against user list
               /*if user is not returned as null, then a new client thread is created*/
               if (tempUser != null) {
                  jtaLog.append("Server: User Connected: " + tempUser.getUserName() + "\n");
                  ClientThread ct = new ClientThread(cSocket, tempUser, pwt, scn);
                  pwt.println("ACCEPTED");
                  pwt.flush();
                  jtaLog.append("Server: Login Accepted" + "\n");
                 // jtaLog.append(scn.nextLine() + "\n");
                  pwt.println("220 OK");
                  pwt.flush();
                  ct.start();
               
               }
               /*If no user exists, return error to user*/
               else {
                  pwt.println("421 SERVICE NOT AVAILABLE");
                  pwt.flush();
                  jtaLog.append("Server: 421 SERVICE NOT AVAILABLE" + "\n");
               }
            
            } catch (Exception e) {
            }
         }
      
      }
   
      /*kill - closes server socket and ends run loop*/
      public void kill() {
         try {
            running = false;
            sSocket.close();
            queue.interrupt();//interupt running FIFOHandler
         } catch (Exception e) {
         }
      }
   
      /**
       * doCheck - compares client creds against the creds stored in user.obj
       */
      public User doCheck(String user, String pass) {
         for (User userObj : users) {
            if (user.equals(userObj.getUserName()) && pass.equals(userObj.getPassWord())) {
               return userObj;
            } else {
               continue;
            }
         }
         return null;
      }
   }

   /**
    * ClientThread - each client receives its own thread. All functions of the SMTP Server are handled in this class
    */
   class ClientThread extends Thread {
      private Socket clientSocket;
      private PrintWriter pwt;
      private Scanner scn;
      private String name;
      private User clientUser;
   
      /**
       * ClientThread - passed in socket, user, oos, and ois
       * @param Socket - socket user connected with
       * @param User - the user who is logged in
       * @param  PrintWriter - the user's printwriter established when they connected
       * @param Scaner - the user's scanner established when they connected
       */
      public ClientThread(Socket socket, User _user, PrintWriter Out, Scanner In) {
         try {
            clientSocket = socket;
            clientUser = _user;
            pwt = Out;
            scn = In;
         } catch (Exception e) {
            jtaLog.append(e + "\n");
         }
      
        /*get the ip address from the client*/
         name = "<" + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + "> ";//name is composed if the clients IP address and port number
      }
       
       /* run - awaits a command from the client to determine what methods and class to call to process emails */
      public void run() {
         try {
         
            while (true) {
               String command = scn.nextLine();
               jtaLog.append(name + command + "\n");
               command = command.substring(0, 4);
               commands(command);
            }
         } catch (Exception e) {/*jtaLog.append(e + "\n"); e.printStackTrace();*/}
      }
   
      public void commands(String command) {
         switch (command) {
         
            case "HELO":
               doMail();
               break;
         
            case "QUIT":
               doQuit();
               break;
         
            case "MLBX":
               doSendMailbox();
               break;
         
            default:
               break;
         
         }
      }
   
      /*doSendMailbox*/
      public synchronized void doSendMailbox() {
      
         try {
            Vector<MailConstants> sendBox;
            sendBox = clientUser.getEmail();
            int count = sendBox.size();
            pwt.println("" + count);
            pwt.flush();
            for (int i = 0; i < count; i++) {
               MailConstants send = sendBox.get(i);
               synchronized (clientSocket) {
                  pwt.println(send.getTo());
                  pwt.flush();
                  pwt.println(send.getFrom());
                  pwt.flush();
                  pwt.println(send.getCC());
                  pwt.flush();
                  pwt.println(send.getDate());
                  pwt.flush();
                  pwt.println(send.getSubject());
                  pwt.flush();
                  pwt.println(send.getMessage());
                  pwt.flush();
                  pwt.println("_DONE_");
                  pwt.flush();
               }
            }
            jtaLog.append("Server: Mailbox Sent" + "\n");
         
            String receive = (String) scn.nextLine();
            if (receive.equals("RECEPTION COMPLETE")) {
               jtaLog.append(name + receive + "\n");
            } else {
               jtaLog.append(name + "Inbox was not received." + "\n");
            }
         } catch (Exception e) {
            jtaLog.append("Server: Error sending Mailbox" + "\n");
         }
      }
      
      /* doMail - handles most SMTP codes and is what is responsible for recieving emails from the client */
      public synchronized void doMail() {
         String response;
         String mailTo = "";
         String mailFrom;
         String date;
         String subject;
         String message = "";
         String ccAddress;
         Boolean encrypt = false;
         int beginning;
         int end;
         int counter = 0;
      
         try {
            pwt.println("250 HELO" + name + "Nice to meet you");
            pwt.flush();
            jtaLog.append("250 HELO Nice to meet you" + "\n");
         
            /*MAILFROM BLOCK*/
            response = scn.nextLine();
            jtaLog.append(name + response + "\n");
         
            if (!response.contains("MAIL")) {
               pwt.println("421 SERVICE NOT AVAILABLE");
               pwt.flush();
               jtaLog.append("Server: 421 SERVICE NOT AVAILABLE " + "\n");
               return;//break out of method
            } else {
               beginning = response.indexOf("<");
               end = response.indexOf(">");
               mailFrom = response.substring(beginning + 1, end);
               pwt.println("250 OK");
               pwt.flush();
               jtaLog.append("Server: 250 OK Mail From : " + mailFrom + "\n");
            }
            /*End MAILFROM BLOCK*/
         
            /*RCPT TO BLOCK*/
            response = scn.nextLine();
            jtaLog.append(name + response + "\n");
         
            if (!response.contains("RCPT")) {
            
               pwt.println("421 SERVICE NOT AVAILABLE");
               pwt.flush();
               jtaLog.append("Server: 421 SERVICE NOT AVAILABLE" + "\n");
               return;
            } else {
               beginning = response.indexOf("<");
               end = response.indexOf(">");
               mailTo = response.substring(beginning + 1, end);
               pwt.println("250 OK");
               pwt.flush();
               jtaLog.append("Server: 250 OK Mail To: " + mailTo + "\n");
            }
                /*END RCPT BLOCK*/
         
            /*DATA BLOCK*/
            response = scn.nextLine();
            jtaLog.append(name + response + "\n");
         
            if (!response.contains("DATA")) {
               pwt.println("421 SERVICE NOT AVAILABLE");
               pwt.flush();
               jtaLog.append("Server: 421 SERVICE NOT AVAILABLE");
            } else {
               pwt.println("354 End DATA <CR><LF>.<CR><LF>");
               pwt.flush();
               jtaLog.append("Server: 354 End DATA <CR><LF>.<CR><LF>" + "\n");
            }
            //appned to to log
            response = scn.nextLine();
            jtaLog.append(name + response + "\n");
            //append from to log
            response = scn.nextLine();
            jtaLog.append(name + response + "\n");
         
            response = scn.nextLine();
            jtaLog.append(name + response + "\n");
            beginning = response.indexOf(":");
            ccAddress = response.substring(beginning + 1);
            // String[] addresses = ccAddress.split(",");
            
            response = scn.nextLine();
            jtaLog.append(name + response + "\n");
            beginning = response.indexOf(":");
            date = response.substring(beginning + 1);
         
            response = scn.nextLine();
            jtaLog.append(name + response + "\n");
            beginning = response.indexOf(":");
            subject = response.substring(beginning + 1);
         
            while (counter < 1) {
               response = scn.nextLine();
               if (response.equals("."))
                  counter++;
               else if (response.contains("_ENCRYPTED_"))
                  encrypt = true;
                
               message += response + "\n";
               jtaLog.append("Server: Message Received : " + response + "\n");
            }
         
            pwt.println("250 OK Queued");
            pwt.flush();
            jtaLog.append("Server: 250 OK" + "\n");
           
            /*DATA BLOCK END*/
            MailConstants newEmail = new MailConstants(encrypt, mailTo, mailFrom, ccAddress, date, subject, message);
            fifo.enqueue(newEmail);
         
         }//try
         
         catch (Exception e) {
            jtaLog.append("Server Exception: " + e);
         }//end catch
      }//end doMail
   
      public synchronized void doQuit() {
         try {
            pwt.println("221 BYE");
            pwt.flush();
            jtaLog.append("Server: 221 BYE" + "\n");
            yield();
         } catch (Exception e) {
            jtaLog.append("Server: " + e + "\n");
         }
      }
   
   
   }//end ClientThread

   /**
    * FIFOHandler -creates a thread and calls process
    */
   public class FIFOHandler extends Thread {
      MailConstants newEmail;
   
      public FIFOHandler() {
      
      }
   
      public void run() {
         while (true) {
            process();
         }
      }
   
      /**
       * process -adds email object to the FIFOQueue and breaks apart the information to determine
       * who sent the mail and who needs to receive it
       */
      private synchronized void process() {
         if (fifo.empty() == true)
            return;
      
         try {
            newEmail = fifo.dequeue();
         } catch (FIFOQueueException e) {
            jtaLog.append("Server: " + e);
         }
      
         String To = newEmail.getTo();
         int breakPoint = To.indexOf("@");
         String mailTo = To.substring(0, breakPoint);
         String ip = To.substring(breakPoint + 1);
         User locateUser = onServer(mailTo, ip);
      
         if (locateUser != null) {
            saveEmail(locateUser, newEmail);
         } else {
            Relay newRelay = new Relay(mailTo, ip, newEmail);
            newRelay.start();
         }
      
      
      }//process
   
      /**
       * saveEmail saves the user email to a vector and to a user file stored on the server system
       *
       * @param userName -this is the userName to be given to the file for mail storage
       * @param newMail  -this is the actual mail object to be broken down and appended to the user mailbox
       */
      private synchronized void saveEmail(User userName, MailConstants newMail) {
         /*Add email to users mailBox vector then save vector to a file*/
         PrintWriter mpwt = null;
         File mbox = new File(userName.getUserName() + ".txt");
      
         try {
            userName.addMail(newMail);
         
            mpwt = new PrintWriter(new FileOutputStream(mbox, true));
         
            mpwt.println("Encrypted: " + newMail.getEncrypted());
            mpwt.println("To: " + newMail.getTo());
            mpwt.println("From: " + newMail.getFrom());
            mpwt.println("CC: " + newMail.getCC());
            mpwt.println("Date: " + newMail.getDate());
            mpwt.println("Subject: " + newMail.getSubject());
         
            for (String s : newMail.getMessage().split("\n"))
               mpwt.println(s);
         
            mpwt.println("");
         
            mpwt.close();
         } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error handling mailbox");
         }
      }
   
      /**
       * @param String name -name is used to check if the destination user is on the server or not
       * @param String ip   -used to check if the destination ip matches the server ip
       * @return -returns null if the user is not on the server
       */
      private User onServer(String name, String ip) {
      
         for (User user : users) {
            if (name.equals(user.getUserName()) && ip.equals(user.getIP())) {
               return user;
            } else {
               continue;
            }
         }
         return null;
      }
   }

}//end SMTPServer