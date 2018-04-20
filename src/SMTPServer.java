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

    private Vector<User> users = new Vector<User>();
    private File userFile = new File("user.obj");

    public static int SERVER_PORT = 32001;
    private ServerSocket sSocket;

    public static void main(String[] args){
        new SMTPServer();
    }

    public SMTPServer(){
        doLoadUsers();
        setupWindow();

        jpNorth.add(jbStart);
        this.add(jpNorth, BorderLayout.NORTH);


        jpCenter.add(jlLog);
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

    public void doStart(){
        sThread = new ServerThread();
        sThread.start();
        jbStart.setText("Stop");
    }

    public void doStop(){
        sThread.kill();
        jbStart.setText("Start");

    }

    public void doLoadUsers(){
        try{
            /*File IO to look for file*/
            FileInputStream userFis;
            ObjectInputStream userIn;
            /*get current directory*/
            String currentDir = System.getProperty("user.dir");
            String fileName = "user.obj";//set file name

            File tempFile = new File(currentDir + "\\" + fileName);//set file to orders.obj in current directory
            /*Check to see if user.obj exists and load objects into an array list.
             */
            if(tempFile.exists()){
                userFis = new FileInputStream(tempFile);
                userIn = new ObjectInputStream(userFis);
                for(int i = 0; i < tempFile.length(); i++){
                    users.add((User)userIn.readObject());
                }
                userFis.close();//close input stream

            }
            else{
                JOptionPane.showMessageDialog(this, "Error: No User File Found");
            }
        }
        /**@throws Exception - this catch is necessary for the try above*/
        catch(Exception e){System.out.println("Exception :" + e);}

    }


    class ServerThread extends Thread{
        private boolean running = true;
        private boolean pass = false;
        private ObjectOutputStream oos;
        private ObjectInputStream ois;
        private User tempUser;


        public ServerThread(){

        }

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

                    oos = new ObjectOutputStream(cSocket.getOutputStream());
                    ois = new ObjectInputStream(cSocket.getInputStream());

                    String userName = (String) ois.readObject();
                    System.out.println(userName);

                    String passWord = (String) ois.readObject();
                    System.out.println(passWord);

                    tempUser = doCheck(userName, passWord);

                    if(tempUser != null){
                        jtaLog.append("User Connected: " + tempUser.getUserName() + "\n");
                        ClientThread ct = new ClientThread(cSocket, tempUser);
                        ct.start();
                        oos.writeObject("220 OK");
                    }
                    else{
                        oos.writeObject("421: SERVICE NOT AVAILABLE");
                        jtaLog.append("User login failed");
                    }


                }
                catch(Exception e){System.out.println(e);}
            }

            pass = false;
        }

        public void kill(){
            try{
                running = false;
                sSocket.close();
            }
            catch(Exception e){}
        }


        public User doCheck(String user, String pass){
            for(User userObj : users){
                System.out.println(userObj.getUserName());
                System.out.println(userObj.getPassWord());
                if(user == userObj.getUserName() && pass == userObj.getPassWord()){

                    return userObj;
                }

                else{
                    return null;
                }
            }
            return null;
        }
    }

    class ClientThread extends Thread{
        private Socket cSocket;
        private ObjectOutputStream oos;
        private ObjectInputStream ois;
        private String name;
        private User clientUser;


        public ClientThread(Socket socket, User _user){
            cSocket = socket;
            clientUser = _user;

            name = "<" + cSocket.getInetAddress().getHostAddress() + ":" + cSocket.getPort() + "> ";//name is composed if the clients IP address and port number

        }

        public void run(){
            try{
                while(true){
                    oos = new ObjectOutputStream(cSocket.getOutputStream());
                    ois = new ObjectInputStream(cSocket.getInputStream());

                    String command = (String) ois.readObject();
                    commands(command);
                }
            }
            catch(Exception e){}
        }

        public void commands(String command){
            switch(command){

                case "HELO":
                    break;

                case "MAIL FROM":
                    break;

                case "RCPT TO":
                    break;

                case "DATA":
                    break;

                case "QUIT":
                    break;


            }
        }
    }



}//end SMTPServer