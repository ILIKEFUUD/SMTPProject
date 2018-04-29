public class Relay extends SMTP_Client{

    public Relay(String userName, String ip, MailConstants newEmail){
      SMTP_Client relay = new SMTP_Client();

      relay.SMTPSend(newEmail);
    }
}