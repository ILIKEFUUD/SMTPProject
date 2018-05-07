import java.io.Serializable;

/*
 * @author - Brennan Jackson
 * MailConstants - a getter and setter class for the constants used accross all client and servers
 */

public class MailConstants implements Serializable{

   public static final long serialVersionUID = 1L;

   public boolean isEncrypted;
   public String mailTo;
   public String mailFrom;
   public String ccAddress;
   public String date;
   public String subject;
   public String message;

   /*
    * MailConstanst
    * @parem _isEncrypted - sets boolean of setEncrypted
    * @parem _mailTo - sets string for setTo
    * @parem _mailFrom - sets string for setFrom
    * @parem _ccAddress - sets string for setCC
    * @parem _date - sets string for setDate
    * @parem _subject - sets string for setSubject
    * @parem _message - sets string for setMessage
    */
   public MailConstants(boolean _isEncrypted,
                        String _mailTo,
                        String _mailFrom,
                        String _ccAddress,
                        String _date,
                        String _subject,
                        String _message) {
      setEncrypted(_isEncrypted);
      setTo(_mailTo);
      setFrom(_mailFrom);
      setCC(_ccAddress);
      setDate(_date);
      setSubject(_subject);
      setMessage(_message);
   }
   
   /* gets whether the email is or is not encrypted */
   public boolean getEncrypted(){
      return isEncrypted;
   }

   /* gets who the mail is to */
   public String getTo(){
      return mailTo;
   }
   
   /* gets who the mail is coming from */
   public String getFrom(){
      return mailFrom;
   }
   
   /* gets the CC address of the mail */
   public String getCC(){
      return ccAddress;
   }
   
   /* gets the date and time the email was sent */
   public String getDate(){
      return date;
   }
   
   /* gets the email subject */
   public String getSubject(){
      return subject;
   }
   
   /* gets the body of the email */
   public String getMessage(){
      return message;
   }

   /* sets whether the email is or is not encrypted */
   public void setEncrypted(boolean setEncr){
      isEncrypted = setEncr;
   }
   
   /* sets who the mail is to */
   public void setTo(String receiver){
      mailTo = receiver;
   }

   /* sets who the mail is coming from */
   public void setFrom(String sender){
      mailFrom = sender;
   }
   
   /* sets the CC address of the mail */
   public void setCC(String cc){
      ccAddress = cc;
   }
   
   /* sets the date and time the email was sent */
   public void setDate(String sendDate){
      date = sendDate;
   }
   
   /* sets the email subject */
   public void setSubject(String sendSub){
      subject = sendSub;
   }

   /* sets the body of the email */
   public void setMessage(String sentMessage){
      message = sentMessage;
   }

}