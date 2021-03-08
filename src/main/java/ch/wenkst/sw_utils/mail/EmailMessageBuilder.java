package ch.wenkst.sw_utils.mail;

import java.util.Date;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailMessageBuilder {
	private MimeMessage message;
	
	public EmailMessageBuilder(Session session) {
		message = new MimeMessage(session);
	}
	
	
	protected EmailMessageBuilder addDefaultHeaders() throws MessagingException {
		message.addHeader("Content-type", "text/HTML; charset=UTF-8");
		message.addHeader("format", "flowed");
		message.addHeader("Content-Transfer-Encoding", "8bit");
		return this;
	}
	
	
	protected EmailMessageBuilder addSender(String fromEmail) throws AddressException, MessagingException {
		message.setFrom(new InternetAddress(fromEmail));
		message.setReplyTo(InternetAddress.parse(fromEmail, false));
		return this;
	}
	
	
	protected EmailMessageBuilder addSubject(String subject) throws MessagingException {
		message.setSubject(subject, "UTF-8");
		return this;
	}
	
	
	protected EmailMessageBuilder addSendDate(Date date) throws MessagingException {
		message.setSentDate(date);
		return this;
	}
	
	
	protected EmailMessageBuilder addRecipients(String... toEmail) throws MessagingException {
		InternetAddress[] recipients = new InternetAddress[toEmail.length];
		for (int i=0; i<toEmail.length; i++) {
			recipients[i] = new InternetAddress(toEmail[i]);
		}
		message.setRecipients(Message.RecipientType.TO, recipients);
		return this;
	}
	
	
	protected MimeMessage toMimeMessage() {
		return message;
	}
}
