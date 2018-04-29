public class Relay extends SMTP_Client{

    public Relay(String userName, String ip, MailConstants newEmail){

        Draft draft = new Draft();
        draft.SMTPSend(newEmail);
    }
}
