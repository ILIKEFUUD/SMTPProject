import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class SMTPServer extends JFrame implements ActionListener{

    private JTextArea jtaLog = new JTextArea(10, 35);
    private JButton jbStart = new JButton("Start");
    private JLabel jlLog = new JLabel("Log:");
    private JPanel jpNorth = new JPanel(new FlowLayout(FlowLayout.CENTER));
    private JPanel jpCenter = new JPanel();
    private ServerThread sThread;

    private Vector<User> users = new Vector<User>();//Vector for a users on the server
    private File userFile = new File("user.obj");//file that contains users

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

                    output = new ObjectOutputStream(cSocket.getOutputStream());
                    input = new ObjectInputStream(cSocket.getInputStream());
                    /*read in username and password from the client*/
                    String userName = (String) input.readObject();
                    String passWord = (String) input.readObject();

                    tempUser = doCheck(userName, passWord);//check creds against user list
                    /*if user is not returned as null, then a new client thread is created*/
                    if(tempUser != null){
                        jtaLog.append("User Connected: " + tempUser.getUserName() + "\n");
                        ClientThread ct = new ClientThread(cSocket, tempUser, output, input);
                        output.writeObject("220 OK");
                        output.flush();
                        ct.start();

                    }
                    /*If no user exists, return error to user*/
                    else{
                        output.writeObject("421 SERVICE NOT AVAILABLE");
                        output.flush();
                        jtaLog.append("User login failed");
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
                    return null;
                }
            }
            return null;
        }
    }
    /**ClientThread - each client receives its own thread. All functions of the SMTP Server are handled in this class*/
    class ClientThread extends Thread{
        private Socket clientSocket;
        private ObjectOutputStream oos;
        private ObjectInputStream ois;
        private String name;
        private User clientUser;

        /**ClientThread - passed in socket, user, oos, and ois*/
        public ClientThread(Socket socket, User _user, ObjectOutputStream objOut, ObjectInputStream objIn){
           try {
               clientSocket = socket;
               clientUser = _user;
               oos = objOut;
               ois = objIn;
           }

           catch(Exception e){jtaLog.append(e + "\n");}

           /*get the ip address from the client*/
            name = "<" + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + "> ";//name is composed if the clients IP address and port number
        }

        public void run(){
            try{

                while(true){
                   String command = (String) ois.readObject();
                    jtaLog.append("Command Received: " + command + "\n");
                    commands(command);
               }
            }

            catch(Exception e){jtaLog.append(e + "\n"); e.printStackTrace();}
        }

        public void commands(String command){
            switch(command){

                case "HELO":
                    break;

                case "MAIL FROM":
                    break;

                case "RCPT":
                    break;

                case "DATA":
                    break;

                case "QUIT":
                    break;

                case "MAILBOX":
                    doSendMailbox();
                    break;


            }
        }
        /*doSendMailbox*/
        public void doSendMailbox() {
           try {
               Vector<MailConstants> sendBox;
               sendBox = clientUser.getEmail();
               oos.writeObject(sendBox);
               oos.flush();
               String receive = (String) ois.readObject();
               System.out.println(receive);

               if(receive.equals("INBOX RECEIVED")) {
                   jtaLog.append("Inbox was received!" + "\n");
                   return;
               }
               else{
                   jtaLog.append("Inbox was not received." + "\n");
                   }
               }

               catch(Exception e){jtaLog.append("Error sending Mailbox" + "\n");}
           }


    }//end ClientThread



}//end SMTPServer