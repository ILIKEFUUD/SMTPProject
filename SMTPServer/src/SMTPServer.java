import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.text.*;

public class SMTPServer extends JFrame implements ActionListener{

    private JTextArea jtaLog = new JTextArea(10, 35);
    private JButton jbStart = new JButton("Start");
    private JLabel jlLog = new JLabel("Log:");
    private JPanel jpNorth = new JPanel(new FlowLayout(FlowLayout.CENTER));
    private JPanel jpCenter = new JPanel();
    private ServerThread sThread;

    private Vector<User> users = new Vector<User>();//Vector for a users on the server
    private File userFile = new File("user.obj");//file that contains users

    private FIFOQueue<MailConstants> fifo = new FIFOQueue<MailConstants>();
    private FIFOHandler queue = new FIFOHandler();//Create new FIFOHandler

    public static int SERVER_PORT = 42069;
    private ServerSocket sSocket;

    public static void main(String[] args){
        new SMTPServer();
    }

    public SMTPServer(){
        doLoadUsers();
        setupWindow();

        jpNorth.add(jbStart);//add jbStart to GUI
        this.add(jpNorth, BorderLayout.NORTH);

        /*The cursor in the log will constantly adjust to the lowest line*/
        DefaultCaret caret = (DefaultCaret)jtaLog.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        jtaLog.setLineWrap(true);
        jtaLog.setWrapStyleWord(true);


        jpCenter.add(jlLog);//add Log to the GUI
        jpCenter.add(new JScrollPane(jtaLog));
        this.add(jpCenter, BorderLayout.CENTER);

        jbStart.addActionListener(this);

        /*If the window for the server GUI is closed the server threads are killed*/
        this.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        try{
                            doStop();
                        }
                        catch(NullPointerException npe){
                            System.exit(0);
                        }
                    }

                });


        this.setVisible(true);

    }
    /** setupWindow - Sets parameters for the GUI window*/
    public void setupWindow(){

        this.setTitle("SMTPServer");
        this.setSize(450, 300);
        this.setLocation(600, 50);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void actionPerformed(ActionEvent ae){
        switch(ae.getActionCommand()){

            case "Start":
                doStart();
                break;

            case "Stop":
                doStop();
                break;

        }
    }
    /**doStart - creates and starts new serverThread*/
    public void doStart(){
        sThread = new ServerThread();
        sThread.start();
        jbStart.setText("Stop");
        queue.start();//When the server is started the FIFOHAndler will start as well. No Clients need to be connected to handle incoming mail
    }
    /**doStop - calls kill method which ends the server*/
    public void doStop(){
        sThread.kill();
        jbStart.setText("Start");

    }
    /**doLoadUsers - searches for User.obj file and reads it in. Users from the file are laoded into a vector of users*/
    public void doLoadUsers(){
        try{
            /*File IO to look for file*/
            FileInputStream userFis;
            ObjectInputStream userIn;
            /*get current directory*/
            String currentDir = System.getProperty("user.dir");
            String fileName = "user.obj";//set file name

            File tempFile = new File(currentDir + "\\" + fileName);//set file to orders.obj in current directory

            /*Check to see if user.obj exists and load objects into an array list. */
            if(tempFile.exists()){
                userFis = new FileInputStream(tempFile);
                userIn = new ObjectInputStream(userFis);
                for(int i = 0; i < tempFile.length(); i++){
                    users.add((User)userIn.readObject());//read in and add users
                }
                userFis.close();//close input stream

            }
            /* if file doesn't exist display window for user*/
            else{
                JOptionPane.showMessageDialog(this, "Error: No User File Found");
            }
        }
        /**@throws Exception - this catch is necessary for the try above*/
        catch(Exception e){System.out.println("Exception :" + e);}

    }

    /**ServerThread - creates new server thread. Handles verification for client login*/
    class ServerThread extends Thread{
        private boolean running = true;
        private ObjectOutputStream output;
        private ObjectInputStream input;
        private PrintWriter pwt;
        private Scanner scn;
        private User tempUser;


        public ServerThread(){
        }
        /*run - waits for client to connect to the socket. calls doCheck to verify user login creds*/
        public void run(){

            try{
                sSocket = new ServerSocket(SERVER_PORT);
            }

            catch(IOException ioe){
                System.out.println(ioe);
            }

            while(running == true){
                Socket cSocket = null;

                try{
                    cSocket = sSocket.accept();

                    pwt = new PrintWriter(new OutputStreamWriter(cSocket.getOutputStream()));
                    scn = new Scanner(new InputStreamReader(cSocket.getInputStream()));

                    /*read in username and password from the client*/
                    String userName = scn.nextLine();
                    String passWord = scn.nextLine();

                    tempUser = doCheck(userName, passWord);//check creds against user list
                    /*if user is not returned as null, then a new client thread is created*/
                    if(tempUser != null){
                        jtaLog.append("Server: User Connected: " + tempUser.getUserName() + "\n");
                        ClientThread ct = new ClientThread(cSocket, tempUser, pwt, scn);
                        pwt.println("ACCEPTED");
                        pwt.flush();
                        jtaLog.append("Server: Login Accepted" + "\n");
                        ct.start();

                    }
                    /*If no user exists, return error to user*/
                    else{
                        pwt.println("421 SERVICE NOT AVAILABLE");
                        pwt.flush();
                        jtaLog.append("Server: 421 SERVICE NOT AVAILABLE");
                    }

                }
                catch(Exception e){}
            }

        }
        /*kill - closes server socket and ends run loop*/
        public void kill(){
            try{
                running = false;
                sSocket.close();
                queue.interrupt();//interupt running FIFOHandler

            }
            catch(Exception e){}
        }
        /**doCheck - compares client creds against the creds stored in user.obj*/
        public User doCheck(String user, String pass){
            for(User userObj : users){
                if(user.equals(userObj.getUserName()) && pass.equals(userObj.getPassWord())){
                    return userObj;
                }
                else{
                    continue;
                }
            }
            return null;
        }
    }
    /**ClientThread - each client receives its own thread. All functions of the SMTP Server are handled in this class*/
    class ClientThread extends Thread{
        private Socket clientSocket;

        private PrintWriter pwt;
        private Scanner scn;
        private String name;
        private User clientUser;

        /**ClientThread - passed in socket, user, oos, and ois*/
        public ClientThread(Socket socket, User _user, PrintWriter Out, Scanner In){
           try {
               clientSocket = socket;
               clientUser = _user;
               pwt = Out;
               scn = In;
           }

           catch(Exception e){jtaLog.append(e + "\n");}

           /*get the ip address from the client*/
            name = "<" + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + "> ";//name is composed if the clients IP address and port number
        }

        public void run(){
            try{

                while(true){
                   String command = scn.nextLine();
                   jtaLog.append(name + command + "\n");
                    command = command.substring(0, 4);
                    commands(command);
               }
            }

            catch(Exception e){/*jtaLog.append(e + "\n"); e.printStackTrace();*/}
        }

        public void commands(String command){
            switch(command){

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
             ObjectOutputStream oos;
             ObjectInputStream ois;
           try {
               oos = new ObjectOutputStream(clientSocket.getOutputStream());
               ois = new ObjectInputStream(clientSocket.getInputStream());
               Vector<MailConstants> sendBox;
               sendBox = clientUser.getEmail();
               oos.writeObject(sendBox);
               oos.flush();
               jtaLog.append("Server: Mailbox Sent" + "\n");
               String receive = (String) ois.readObject();

               if(receive.equals("INBOX RECEIVED")) {
                   jtaLog.append(name + receive + "\n");
               }
               else{
                   jtaLog.append(name + "Inbox was not received." + "\n");
                   }
               oos.close();
               ois.close();
           }
               catch(Exception e){jtaLog.append("Server: Error sending Mailbox" + "\n");}
           }

           public synchronized void doMail(){
            String response;
            String mailTo = "";
            String mailFrom;
            String date;
            String subject;
            String message = "";
            String ccAddress;
            Boolean encrypt = true;
            int beginning;
            int end;

            try {
                pwt.println("250 Hello" + name + "Nice to meet you");
                pwt.flush();
                jtaLog.append("250 Hello Nice to meet you" + "\n");

                /*MAILFROM BLOCK*/
                response = scn.nextLine();
                jtaLog.append(name + response + "\n");

                if (!response.contains("MAIL")) {
                    pwt.println("421 SERVICE NOT AVAILABLE");
                    pwt.flush();
                    jtaLog.append("Server: 421 SERVICE NOT AVAILABLE " + "\n");
                    return;//break out of method?
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

                if(!response.contains("RCPT")){

                  pwt.println("421 SERVICE NOT AVAILABLE");
                  pwt.flush();
                  jtaLog.append("Server: 421 SERVICE NOT AVAILABLE" + "\n");
                  return;
                }

                else{
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

                if(!response.contains("DATA")){
                    pwt.println("421 SERVICE NOT AVAILABLE");
                    pwt.flush();
                    jtaLog.append("Server: 421 SERVICE NOT AVAILABLE");
                }
                else{
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
                ccAddress = response.substring(beginning);

                response = scn.nextLine();
                jtaLog.append(name + response + "\n");
                beginning = response.indexOf(":");
                date = response.substring(beginning);

                response = scn.nextLine();
                jtaLog.append(name + response + "\n");
                beginning = response.indexOf(":");
                subject = response.substring(beginning );

                while(!response.equals("\n" + "." + "\n" )){
                    response = scn.nextLine();
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

               catch(Exception e){jtaLog.append("Server Exception: " + e);}//end catch
           }//end doMail

            public synchronized void doQuit() {
                try {
                    pwt.println("221 BYE");
                    pwt.flush();
                    jtaLog.append("Server: 221 BYE");
                    yield();
                }
                catch(Exception e){jtaLog.append("Server: " + e);}
            }


    }//end ClientThread

    public class FIFOHandler extends Thread{
        MailConstants newEmail;

        public FIFOHandler() {

        }

        public void run(){

            while(fifo.empty() != true) {
                process();
            }

        }
        
        private synchronized void process() {
            try {
                newEmail = fifo.dequeue();
            }

            catch(FIFOQueueException e) { jtaLog.append("Server: " + e);}

            String To = newEmail.getTo();
            int breakPoint = To.indexOf("@");
            String mailTo = To.substring(0,breakPoint);
            String ip = To.substring(breakPoint+1);
            boolean locateUser = onServer(mailTo, ip);

            if(locateUser == true){
                saveEmail(mailTo, newEmail);
            }
            else{
                /*We will relay the email here by calling a Relay Class*/
            }


        }//process

        private synchronized void saveEmail(String userName, MailConstants newMail){
            /*Add email to users mailBox vector then save vector to a file*/
        }
        /*Determines if the user is on our server or not based on user name and ip address*/
        private boolean onServer(String name, String ip) {

            for(User user: users) {
                if(name.equals(user.getUserName()) && ip.equals(user.getIP())){
                    return true;
                }
                else{
                    continue;
                }
            }
            return false;
        }
    }

}//end SMTPServer