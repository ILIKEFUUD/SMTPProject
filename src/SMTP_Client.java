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




/**
@author Ben, Brennan, Jordan, Rahul

SMTP_Client.java
Main driver for the client.
Contains inner classes for the different windows: Login, Inbox, and Draft.
Main driver handles all of the SMTP
*/
public class SMTP_Client{

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



}

/**
   Login inner class
   GUI lets user enter server ip, username, and password
   connects client to email server if correct
   opens up inbox
*/
class Login extends JFrame implements ActionListener, MailConstants{

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

   private final static int SERVER_PORT = 32001;
   private Socket cSocket = null;
   private ObjectOutputStream oos;
   private ObjectInputStream ois;



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
            System.out.println("Clicked login");
            boolean login = doConnect(jtfUser.getText(), jtfPass.getText(), jtfIP.getText());
            if(login == true){
               try{
                  new Inbox((Vector<MyEmail>) ois.readObject()); //creates Inbox for now
               }catch(Exception e){
                  System.out.println("ahhh");
               }
               this.dispose(); //destroys login window
            }
            break;
         case "Exit":
            jtfUser.setText("");
            jtfPass.setText("");
            System.exit(0);
            break;
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
         oos = new ObjectOutputStream(cSocket.getOutputStream());
         ois = new ObjectInputStream(cSocket.getInputStream());
         //send server the username and password
         oos.writeObject(userName);
         oos.flush();
         oos.writeObject(password);
         oos.flush();

         //read in returned string from the server
         String conn = "";
         try{
            conn = (String)ois.readObject();
         }catch(Exception e){
            //weird error ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
         }
         if(conn.equals("CONNECTED")){//username and password is correct
            return true;
         }
         else
            return false;

      }
      catch(Exception e){ //could not connect to server, wrong IP or some other error
         JOptionPane.showMessageDialog(null, "Unable to connect to server: " + e, "Connection Error", JOptionPane.ERROR_MESSAGE);
         e.printStackTrace();
         return false;
      }
   }

}
/**
   Inbox inner class
   Manages the inbox of the user,
   Displays emails so user can view them
   Has buttons for drafting/composing new emails and for exiting
*/
class Inbox extends JFrame implements ActionListener, MailConstants{
   //GUI Components
   private JMenuBar jmbBar = new JMenuBar();
   private JMenu jmMenu = new JMenu("File");
   private JMenuItem jmiDraft = new JMenuItem("Draft");
   private JMenuItem jmiExit = new JMenuItem("Exit");

   //JTable for showing emails
   private Vector<MyEmail> mailbox;
   private Vector<String> columnNames = new Vector<String>();
   private Vector<Vector<String>> emailInfo = new Vector<Vector<String>>();
   private JTable jtInbox;
   private int mailCount = 0;

   /**
      Constructor for Inbox object, sets up the GUI
   */
   public Inbox(Vector<MyEmail> mail){

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

      //create vector for JTable
      mailbox = mail;
      Vector<Vector> data = new Vector<Vector>(); //2d vector for data


      for(MyEmail m : mailbox){
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

      jtInbox = new JTable(emailInfo, columnNames); //"I AM A NEGATIVE BITCH " --> Ben
      
      //implement clicking on email to open email
      jtInbox.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
         public void valueChanged(ListSelectionEvent event){
            //do stuff
            
         }
      });

      JScrollPane jspInbox = new JScrollPane(jtInbox);
      this.add(jspInbox, BorderLayout.CENTER);

      jmiDraft.addActionListener(this);
      jmiExit.addActionListener(this);

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
      }
   }

}

/**
email GUI display class
*/
class EmailDisplay extends JFrame{
   
   private JPanel jpHeader = new JPanel();
   private JPanel jpFrom = new JPanel();
   private JLabel jlFrom = new JLabel("From: ");
   private JTextField jtfFrom = new JTextField(49);
   private JPanel jpSubject = new JPanel();
   private JLabel jlSubject = new JLabel("Subject: ");
   private JTextField jtfSubject = new JTextField(46);
   private JTextArea jtaMessage = new JTextArea();
   private JScrollPane jspMessage = new JScrollPane(jtaMessage);

   public EmailDisplay(MyEmail e){
      this.setTitle("Email");
      this.setSize(600, 300);
      this.setLocation(100, 100);
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
      jtaMessage.setText(e.getMessage());

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

   //private JMenuBar jmbBar = new JMenuBar();
   //private JMenu jmMenu = new JMenu("File");
   //private JMenuItem jmiDraft = new JMenuItem("Send");
   //private JMenuItem jmiSExit = new JMenuItem("Save and Exit");
   //private JMenuItem jmiDExit = new JMenuItem("Discard and Exit");

   public Draft(){
      this.setTitle("Draft");
      this.setSize(600, 300);
      this.setLocation(100, 100);
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
   

   public void actionPerformed(ActionEvent ae){
      switch(ae.getActionCommand()){
         case "Send":
            //
            break;
         case "Save and Exit":
            //
            break;
         case "Discard and Exit":
            //
            break;
      }
   }

}
