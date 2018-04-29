import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import javax.swing.event.*;
import javax.swing.table.*;




/**
 @author Ben, Brennan, Jordan, Rahul
 SMTP_Client.java
 Main driver for the client.
 Contains inner classes for the different windows: Login, Inbox, and Draft.
 Main driver handles all of the SMTP
 */
public class SMTP_Client{
   public PrintWriter pwt;
   public Scanner scan;
   public ObjectInputStream ois;
   public Socket cSocket = null;
   public String username = null;
   public HashMap<Character,Character> rot13 = null;
   /**
    @param String array of arguments from command line
    creates new client object
    */
   public static void main(String[] args){
      new SMTP_Client();
   }

   /**
    constructor for main client class
    creates new login object, first window to show to user
    */
   public SMTP_Client(){
      new Login();                                                      //currently does nothing but create login window
   }





   /**
    Login inner class
    GUI lets user enter server ip, username, and password
    connects client to email server if correct
    opens up inbox
    */
   class Login extends JFrame implements ActionListener{

      //GUI components
      private JPanel jpIcon = new JPanel();
      private JPanel jpBoxes = new JPanel();
      private JPanel jpUser = new JPanel();
      private JLabel jlUser = new JLabel("Username");
      private JTextField jtfUser = new JTextField(10);
      private JPanel jpIP = new JPanel();
      private JLabel jlIP = new JLabel("IP Address");
      private JTextField jtfIP = new JTextField(10);
      private JPanel jpPass = new JPanel();
      private JLabel jlPass = new JLabel("Password");
      private JTextField jtfPass = new JTextField(10);
      private JPanel jpButtons = new JPanel();
      private JButton jbLogin = new JButton("Login");
      private JButton jbExit = new JButton("Exit");

      private final static int SERVER_PORT = 42069;





      /**
       Constructor for Login window
       */
      public Login(){
         this.setTitle("SMTP Client Login");
         this.setSize(230, 365);
         this.setLocation(100, 100);
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setLayout(new BorderLayout());

         //add image icon
         try{
            BufferedImage image = ImageIO.read(new File("email-icon.png"));
            JLabel label = new JLabel(new ImageIcon(image));
            jpIcon.add(label);
            this.add(jpIcon, BorderLayout.NORTH);
         }
         catch(IOException ioe){
            JOptionPane.showMessageDialog(null, "Icon file email-icon.png not found" + ioe, "Icon Not Found", JOptionPane.ERROR_MESSAGE);
         }
         //add components
         jpIP.add(jlIP);
         jpIP.add(jtfIP);

         jpUser.add(jlUser);
         jpUser.add(jtfUser);

         jpPass.add(jlPass);
         jpPass.add(jtfPass);

         jpBoxes.setLayout(new GridLayout(3, 1));
         jpBoxes.add(jpIP);
         jpBoxes.add(jpUser);
         jpBoxes.add(jpPass);

         jpButtons.add(jbLogin);
         jpButtons.add(jbExit);
         jbLogin.addActionListener(this);
         jbExit.addActionListener(this);

         this.add(jpBoxes, BorderLayout.CENTER);
         this.add(jpButtons, BorderLayout.SOUTH);

         this.setVisible(true);
      }

      /**
       @param ActionEvent button click: login or exit
       handles login and exit buttons
       */
      public void actionPerformed(ActionEvent ae){
         switch(ae.getActionCommand()){
            case "Login":
               doLogin();
               break;
            case "Exit":
               jtfUser.setText("");
               jtfPass.setText("");
               System.exit(0);
               break;
         }
      }
      /**
       doLogin method
       uses text fields to establish connection to server and
       verify user
       Creates main inbox value
       */
      public void doLogin(){
         System.out.println("Clicked login");
         boolean login = doConnect(jtfUser.getText(), jtfPass.getText(), jtfIP.getText());
         if(login == true){
            try{
               new Inbox();
               //System.out.println(test);
               pwt.println("LOGGED IN");
               pwt.flush();
               username = jtfUser.getText();
               this.setVisible(false); //destroys login window
            }catch(Exception e){
               e.printStackTrace();
               try{
                  pwt.println("RECEPTION FAILED");
               }
               catch(Exception ne){
                  JOptionPane.showMessageDialog(null, "Exception: " + e, "Another Error while throwing error", JOptionPane.ERROR_MESSAGE);
               }
            }
         }
         else{
            JOptionPane.showMessageDialog(null, "Login was unsuccesful, incorrect login", "Error", JOptionPane.ERROR_MESSAGE);
         }


      }

      /**
       doConnect method
       @param String userName - username entered by user
       @param String password - password of the user entered by user
       @param String ip - IP of email server the client is trying to connect to
       */
      public boolean doConnect(String userName, String password, String ip){
         try{

            cSocket = new Socket(ip, SERVER_PORT); //create client socket
            //create output and inputstreams to communicate between login and server
            pwt = new PrintWriter(new OutputStreamWriter(cSocket.getOutputStream()));
            scan = new Scanner(new InputStreamReader(cSocket.getInputStream()));

            //make object inputstream for getting mailbox



            //send server the username and password
            pwt.println(userName);
            pwt.flush();
            pwt.println(password);
            pwt.flush();

            //read in returned string from the server
            String conn = "";
            try{
               conn = scan.nextLine();
               System.out.println(conn);

            }catch(Exception e){
               //weird error ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
               e.printStackTrace();
            }
            if(conn.contains("ACCEPTED")){//username and password is correct
               return true;
            }else{
               System.out.println(conn);
               return false;
            }
         }
         catch(Exception e){ //could not connect to server, wrong IP or some other error
            JOptionPane.showMessageDialog(null, "Unable to connect to server: " + e, "Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
         }//doConnect
      }

   }
   /**
    Inbox inner class
    Manages the inbox of the user,
    Displays emails so user can view them
    Has buttons for drafting/composing new emails and for exiting
    */
   class Inbox extends JFrame implements ActionListener{
      //GUI Components
      private JMenuBar jmbBar = new JMenuBar();
      private JMenu jmMenu = new JMenu("File");
      private JMenuItem jmiDraft = new JMenuItem("Draft");
      private JMenuItem jmiExit = new JMenuItem("Exit");
      private JMenuItem jmiMailbox = new JMenuItem("Refresh");

      //JTable for showing emails
      private Vector<MailConstants> mailbox;
      private Vector<String> columnNames = new Vector<String>();
      private Vector<Vector<String>> emailInfo = new Vector<Vector<String>>();
      private JTable jtInbox;
      private int mailCount = 0;




      /**
       Constructor for Inbox object, sets up the GUI
       */
      public Inbox(){


         //hashmap for encryption
         rot13 = new HashMap<Character, Character>();
         String[] normalAlpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
         String[] shiftAlpha = "mnopqrstuvwxyzabcdefghijklMNOPQRSTUVWXYZABCDEFGHIJKL".split("");

         //put into hashmap
         for(int i = 0; i < normalAlpha.length; i++){
            rot13.put(normalAlpha[i].charAt(0), shiftAlpha[i].charAt(0));
         }




         //now there is a hashmap for the ROT13


         //column names
         columnNames.add("From");
         columnNames.add("Subject");
         columnNames.add("Date");

         this.setTitle("SMTP Client Inbox");
         this.setSize(600, 300);
         this.setLocation(100, 100);
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setLayout(new BorderLayout());

         this.setJMenuBar(jmbBar);
         jmbBar.add(jmMenu);
         jmMenu.add(jmiDraft);
         jmMenu.add(jmiExit);
         jmMenu.add(jmiMailbox);


         jmiDraft.addActionListener(this);
         jmiExit.addActionListener(this);
         jmiMailbox.addActionListener(this);
         this.setVisible(true);

      }



      /**
       @param ActionEvent ae - draft or exit button click

       */
      public void actionPerformed(ActionEvent ae){
         switch(ae.getActionCommand()){
            case "Draft":
               new Draft();
               break;
            case "Exit":
               System.exit(0);
               break;
            case "Refresh":
               System.out.println("test");
               refresh();
               break;
         }
      }



      /*
      refresh method
      asks server for the mailbox initially
      -optional, just what our group wanted to do
      */
      public void refresh(){

         //create vector for JTable
         pwt.println("MLBX");									//command to server
         pwt.flush();
         int count = Integer.parseInt(scan.nextLine());				//how many emails are coming
         Vector<MailConstants> inbox = new Vector<MailConstants>();	//inbox vector
         for(int i=0;i<count;i++){
            MailConstants email = new MailConstants(true, scan.nextLine(), scan.nextLine(), scan.nextLine(), scan.nextLine(), scan.nextLine(), scan.nextLine());
//       		email.setEncrypted(scan.nextLine());
//       		email.setTo(scan.nextLine());
//       		email.setFrom(scan.nextLine());
//       		email.setCc(scan.nextLine());
//       		email.setDate(scan.nextLine());
//       		email.setSubject(scan.nextLine());
//       		email.setMessage(scan.nextLine());
            inbox.add(email);
         }

         Vector<Vector> data = new Vector<Vector>(); //2d vector for data
         for(MailConstants m : inbox){
            Vector<String> emailData = new Vector<String>(); //individual data for each email
            String from = m.getFrom();
            String subject = m.getSubject();
            String date = m.getDate();
            emailData.add(from);
            emailData.add(subject);
            emailData.add(date);
            data.add(emailData);
            emailInfo.add(emailData);
         }

         jtInbox = new JTable(data, columnNames);

         jtInbox.addMouseListener(
                 new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                       int row = jtInbox.rowAtPoint(evt.getPoint());
                       MailConstants called = inbox.get(row);
                       new EmailDisplay(called);
                    }
                 });

         JScrollPane jspInbox = new JScrollPane(jtInbox);
         this.add(jspInbox, BorderLayout.CENTER);
         this.setVisible(true);
      }

   }

   /**
    email GUI display class
    */
   class EmailDisplay extends JFrame{
      //GUI components
      private JPanel jpHeader = new JPanel();
      private JPanel jpFrom = new JPanel();
      private JLabel jlFrom = new JLabel("From: ");
      private JTextField jtfFrom = new JTextField(49);
      private JPanel jpSubject = new JPanel();
      private JLabel jlSubject = new JLabel("Subject: ");
      private JTextField jtfSubject = new JTextField(46);
      private JTextArea jtaMessage = new JTextArea();
      private JScrollPane jspMessage = new JScrollPane(jtaMessage);



      /**
       EmailDisplay constructor
       @param email object e
       */
      public EmailDisplay(MailConstants e){
         this.setTitle("Email");
         this.setSize(600, 300);
         this.setLocation(100, 100);
         this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         this.setLayout(new BorderLayout());

         jpFrom.setLayout(new FlowLayout(FlowLayout.LEFT));
         jpFrom.add(jlFrom);
         jpFrom.add(jtfFrom);
         jtfFrom.setText(e.getFrom());
         jtfFrom.setEditable(false);

         jpSubject.setLayout(new FlowLayout(FlowLayout.LEFT));
         jpSubject.add(jlSubject);
         jpSubject.add(jtfSubject);
         jtfSubject.setText(e.getSubject());
         jtfSubject.setEditable(false);


         jpHeader.setLayout(new GridLayout(2, 1));
         jpHeader.add(jpFrom);
         jpHeader.add(jpSubject);

         jtaMessage.setEditable(false);
         //decrypt if encrypted
         if(e.getEncrypted()){
            jtaMessage.setText(rot(e.getMessage())); //set text as decrypted message
         }else{
            jtaMessage.setText(e.getMessage());
         }


         this.add(jpHeader, BorderLayout.NORTH);
         this.add(jspMessage, BorderLayout.CENTER);

         this.setVisible(true);
      }

   }

   /**
    Draft inner class
    Shows GUI for user to enter in fields for Email object
    */
   class Draft extends JFrame implements ActionListener{

      private JPanel jpHeader = new JPanel();
      private JPanel jpTo = new JPanel();
      private JLabel jlTo = new JLabel("To: ");
      private JTextField jtfTo = new JTextField(49);
      private JPanel jpSubject = new JPanel();
      private JLabel jlSubject = new JLabel("Subject: ");
      private JTextField jtfSubject = new JTextField(46);
      private JTextArea jtaMessage = new JTextArea();
      private JScrollPane jspMessage = new JScrollPane(jtaMessage);
      private JPanel jpRahul = new JPanel();
      private JButton jbSend = new JButton("Send");
      private JButton jbExit = new JButton("Exit");
      private JCheckBox jcbEncrypted = new JCheckBox("Enrypted");


      //private JMenuBar jmbBar = new JMenuBar();
      //private JMenu jmMenu = new JMenu("File");
      //private JMenuItem jmiDraft = new JMenuItem("Send");
      //private JMenuItem jmiSExit = new JMenuItem("Save and Exit");
      //private JMenuItem jmiDExit = new JMenuItem("Discard and Exit");

      /**
       Draft constructor
       creates and sets up GUI
       */
      public Draft(){

         this.setTitle("Draft");
         this.setSize(600, 300);
         this.setLocation(100, 100);
         this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         this.setLayout(new BorderLayout());

         //this.setJMenuBar(jmbBar);
         //jmbBar.add(jmMenu);
         //jmMenu.add(jmiDraft);
         //jmMenu.add(jmiSExit);
         //jmMenu.add(jmiDExit);

         //jmiDraft.addActionListener(this);
         //jmiSExit.addActionListener(this);
         //jmiDExit.addActionListener(this);
         jbSend.addActionListener(this);
         jbExit.addActionListener(this);

         jpTo.setLayout(new FlowLayout(FlowLayout.LEFT));
         jpTo.add(jlTo);
         jpTo.add(jtfTo);
         jpTo.add(jcbEncrypted);

         jpSubject.setLayout(new FlowLayout(FlowLayout.LEFT));
         jpSubject.add(jlSubject);
         jpSubject.add(jtfSubject);

         jpHeader.setLayout(new GridLayout(2, 1));
         jpHeader.add(jpTo);
         jpHeader.add(jpSubject);

         jpRahul.setLayout(new FlowLayout(FlowLayout.RIGHT));
         jpRahul.add(jbExit);
         jpRahul.add(jbSend);


         this.add(jpHeader, BorderLayout.NORTH);
         this.add(jspMessage, BorderLayout.CENTER);
         this.add(jpRahul, BorderLayout.SOUTH);

         this.setVisible(true);
      }

      /**
       actionPerformed method for Draft GUI
       handles Send, Save and Exit, and Discard and Exit buttons
       */
      public void actionPerformed(ActionEvent ae){
         switch(ae.getActionCommand()){
            case "Send":
               doSend();
               this.dispose();
               break;
            case "Save and Exit":
               //Theoretically save the written data to a file, maybe CSV?
               break;
            case "Discard and Exit":
               //Just exit
               this.dispose();
               break;
         }
      }

      /**
       doSend method
       creates email object and sends it to server using SMTP
       */
      public void doSend() {
         MailConstants sending = new MailConstants(false, "", "", "", "", "", "");
         sending.setTo(jtfTo.getText());
         sending.setFrom(username);
         sending.setCC(jtfTo.getText());
         //get current date
         Date d = new Date(System.currentTimeMillis());
         sending.setDate(d.toString());
         sending.setSubject(jtfSubject.getText());


         //get if encrypted
         if(jcbEncrypted.isSelected()){
            //encrypted is true
            sending.setEncrypted(true);
            //change message
            sending.setMessage(rot(jtaMessage.getText()));
         }else{ //message not encrypted
            sending.setMessage(jtaMessage.getText());
         }

         //email object created
         //SMTP it
         SMTPSend(sending);
      }

      /**
       uses SMTP conversation between server and client
       */
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
                              this.dispose();
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

   }//end of class draft

   /**
    Decrypts and Encrypts the given message
    @param message to be encrypted or decrypted
    */
   public String rot(String message){
      //ROT13 decrypt
      String decrypted = "";
      System.out.println(message);
      for(int i = 0; i < message.length(); i++){//for every letter in the message
         //subtract 13 to the char value and append to decrypted
         char letter = message.charAt(i);
         if((letter >= 65 && letter <= 90 ) || (letter >= 97 && letter <= 122)){
            decrypted += rot13.get(letter); //if letter, encrypt it
         }else{
            decrypted += letter;
         }

      }
      System.out.println(decrypted);
      return decrypted;

   }

}//end of class