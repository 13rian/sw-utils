package ch.wenkst.sw_utils;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.mail.EmailSender;

public class MainEmail {
	private final static Logger logger = LoggerFactory.getLogger(MainEmail.class);
	
	
	private static String emailDir = Utils.getWorkDir() + File.separator + "resource" + File.separator + "mail" + File.separator;
	
	
	/**
	 * two test for trouble shooting:
	 * - ping smtp.gmail.com
	 * - telnet smtp.gmail.com 587
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String fromEmail = "stefan.wenk@celsi.ch"; 	// email of the sender
		String password = "pP1_abcd"; 				// password of the sender
		String host = "smtp.celsi.ch"; 				// the smtp host
		String toEmail1 = "wenkst@gmail.com"; 		// email of the receiver1
		String toEmail2 = "stefan.wenk@celsi.ch"; 	// email of the receiver2
		
		// define the attachments for the email
		String attachment1 = emailDir + "attachment1.txt";
		String attachment2 = emailDir + "attachment2.txt";
		String imgAttachment1 = emailDir + "pic1.jpeg";
		String imgAttachment2 = emailDir + "pic2.jpg";
		
		EmailSender emailSender = new EmailSender();
		logger.info("start to prepare the email");
		try {
			emailSender
				.debugMessages(false) 								// true to print debug messages
				.host(host)											// add the smpt host
				.port(587) 											// port of the smpt server
				.authentication(fromEmail, password)				// add the username and password
				.createClientSession() 								// init the email client
				.createBasicMsg("Test", toEmail1, toEmail2) 		// create the basic message
				.addBody("This is the body of the email.")			// add the body of the email
				.addAttachment(attachment1)							// add an attachment
				.addAttachment(attachment2) 						// add an attachment
				.addAttachment(imgAttachment1) 						// add an image attachment
//				.addDisplayedImage(imgAttachment2) 					// add an image attachment that is displayed in the messae body
				.send(); 											// send out the email
			
			
			
		} catch (Exception e) {
			logger.error("error sending the email: ", e);
		}
		logger.info("email successfully sent");
	}

}
