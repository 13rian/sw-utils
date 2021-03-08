package ch.wenkst.sw_utils.mail;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class EmailSender {
	private Properties props;
	private Authenticator authenticator;
	private Session session;
	private MimeMessage message;
	private MimeMultipart multipart;
	private String fromEmail;
	private boolean isDebug;
	
	
	/**
	 * can send emails to one or more recipients using the smtp protocol. The communication with the mail server
	 * will be tls encryped 
	 */
	public EmailSender() {			
		multipart = new MimeMultipart();
		createDefaultProps();
	}
	
	
	private void createDefaultProps() {
		props = new Properties();
		props.put("mail.smtp.port", "587"); 				// TLS Port
		props.put("mail.smtp.auth", "true"); 				// enable authentication
		props.put("mail.smtp.starttls.enable", "true"); 	// enable STARTTLS
		props.put("mail.smtp.connectiontimeout", "10000");  // connection timeout
		props.put("mail.smtp.timeout", "30000"); 			// read timeout for the socket
	}
	
	
	/**
	 * configures the smtp host, e.g. smtp.gmail.com, smtp.celsi.ch
	 * @param host
	 * @return
	 */
	public EmailSender host(String host) {
		props.put("mail.smtp.host", host);
		return this;
	}
	
	
	/**
	 * configures the port of the smtp server
	 * @param port
	 * @return
	 */
	public EmailSender port(int port) {
		props.put("mail.smtp.port", port + "");
		return this;
	}
	
	
	/**
	 * configures the authentication to connect to the smpt server
	 * @param senderEmail		the sender's email address 
	 * @param password			the password of the smpt server
	 * @return
	 */
	public EmailSender authentication(String senderEmail, String password) {
		this.fromEmail = senderEmail;
		
		if (password != null) {
			authenticator = new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(fromEmail, password);
				}
			};
		}
		
		return this;
	}
	
	
	/**
	 * true if the email client should log debug messages
	 * @param isDebug	true if the email client should log debug messages
	 * @return
	 */
	public EmailSender debugMessages(boolean isDebug) {
		this.isDebug = isDebug;
		return this;
	}
	
	
	/**
	 * initializes the client session to connect to the smpt server
	 * @return
	 */
	public EmailSender createClientSession() {
		if (authenticator == null) {
			session = Session.getInstance(props);
		} else {
			session = Session.getInstance(props, authenticator);
		}
		
		session.setDebug(isDebug);
		return this;
	}
	
	
	/**
	 * creates a basic mime message with all the properties that need to be present in every email
	 * @param subject 		the subject
	 * @param toEmail 		the email addresses of the receivers
	 * @return 
	 * @throws MessagingException
	 */
	public EmailSender createBasicMsg(String subject, ArrayList<String> toEmail) throws MessagingException {
		String[] toEmailArr = toEmail.toArray(new String[toEmail.size()]);
		return createBasicMsg(subject, toEmailArr);
	}
	
	
	/**
	 * creates a basic mime message with all the properties that need to be present in every email
	 * @param subject 		the subject
	 * @param toEmail 		the email addresses of the receivers
	 * @return 
	 * @throws MessagingException
	 */
	public EmailSender createBasicMsg(String subject, String... toEmail) throws MessagingException {
		message = new EmailMessageBuilder(session)
				.addDefaultHeaders()
				.addSender(fromEmail)
				.addSubject(subject)
				.addSendDate(new Date())
				.addRecipients(toEmail)
				.toMimeMessage();
		
		return this;
	}


	/**
	 * adds the body part to the email message
	 * @param body 					text body of the email
	 * @return
	 * @throws MessagingException
	 */
	public EmailSender addBody(String body) throws MessagingException {
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(body);
		multipart.addBodyPart(messageBodyPart);
		
		return this;
	}
	
	
	/**
	 * adds an attachment to the body, can be any file
	 * @param filename 				the path to any file to attach
	 * @return
	 * @throws MessagingException
	 */
	public EmailSender addAttachment(String filename) throws MessagingException {
		BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);
        multipart.addBodyPart(messageBodyPart);
        
        return this;
	}
	
//	does not work like this for some reason	
//	/**
//	 * attaches an image and displays it in the message body
//	 * @param filename 				path of the image to attach
//	 * @return
//	 * @throws MessagingException
//	 */
//	public EmailSender addDisplayedImage(String filename) throws MessagingException {
//		BodyPart messageBodyPart = new MimeBodyPart();
//        DataSource source = new FileDataSource(filename);
//        messageBodyPart.setDataHandler(new DataHandler(source));
//        messageBodyPart.setFileName(filename);
//		
//        // add the image as attachment
//        String id = UUID.randomUUID().toString();
//        id = id.replaceAll("-", "");
//        messageBodyPart.setHeader("Content-ID", "test");
//        messageBodyPart.setDisposition(MimeBodyPart.INLINE);
//        multipart.addBodyPart(messageBodyPart);
//
//        // display the image in the email body
//        messageBodyPart = new MimeBodyPart();
//        String htmlContent = "<html> <h1>Attached Image</h1>" + "<img src=\"cid:" + "test" + "\" /> </html>";
//        messageBodyPart.setContent(htmlContent, "text/html");
//        multipart.addBodyPart(messageBodyPart);
//		
//		return this;
//	}
	

	
	/**
	 * sends out the email
	 * @throws MessagingException
	 */
	public void send() throws MessagingException {
		message.setContent(multipart); 	 	// set the content of the message
		Transport.send(message);
	}


	public Properties getProps() {
		return props;
	}


	public void setProps(Properties props) {
		this.props = props;
	}


	public Authenticator getAuthenticator() {
		return authenticator;
	}


	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}
}
