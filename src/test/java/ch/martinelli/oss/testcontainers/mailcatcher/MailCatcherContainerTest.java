package ch.martinelli.oss.testcontainers.mailcatcher;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class MailCatcherContainerTest {

	@Container
	static MailCatcherContainer mailCatcher = new MailCatcherContainer();

	@Test
	void shouldStartAndExposeCorrectPorts() {
		assertThat(mailCatcher.isRunning()).isTrue();
		assertThat(mailCatcher.getSmtpPort()).isPositive();
		assertThat(mailCatcher.getHttpPort()).isPositive();
		assertThat(mailCatcher.getHttpUrl()).startsWith("http://");
	}

	@Test
	void shouldCatchEmailSentViaSMTP() throws Exception {
		// Clear any existing messages
		MailCatcherClient client = mailCatcher.getClient();
		client.deleteAllMessages();

		// Send an email
		sendEmail("sender@example.com", "recipient@example.com", "Test Subject", "Hello, this is a test email!");

		// Verify the email was caught
		List<Message> messages = client.getAllMessages();
		assertThat(messages).hasSize(1);

		Message message = messages.get(0);
		assertThat(message.sender()).isEqualTo("<sender@example.com>");
		assertThat(message.recipients()).containsExactly("<recipient@example.com>");
		assertThat(message.subject()).isEqualTo("Test Subject");

		// Verify message content
		String plainBody = client.getMessagePlain(message.id());
		assertThat(plainBody).contains("Hello, this is a test email!");
	}

	@Test
	void shouldSupportMultipleRecipients() throws Exception {
		MailCatcherClient client = mailCatcher.getClient();
		client.deleteAllMessages();

		// Send email to multiple recipients
		sendEmailToMultipleRecipients("sender@example.com", List.of("alice@example.com", "bob@example.com"),
				"Group Email", "This is a group email.");

		List<Message> messages = client.getAllMessages();
		assertThat(messages).hasSize(1);

		Message message = messages.get(0);
		assertThat(message.recipients()).containsExactlyInAnyOrder("<alice@example.com>", "<bob@example.com>");
	}

	@Test
	void shouldDeleteMessages() throws Exception {
		MailCatcherClient client = mailCatcher.getClient();
		client.deleteAllMessages();

		// Send two emails
		sendEmail("sender@example.com", "recipient@example.com", "Email 1", "Body 1");
		sendEmail("sender@example.com", "recipient@example.com", "Email 2", "Body 2");

		assertThat(client.getMessageCount()).isEqualTo(2);

		// Delete all messages
		client.deleteAllMessages();

		assertThat(client.getMessageCount()).isZero();
	}

	@Test
	void shouldRetrieveMessageSource() throws Exception {
		MailCatcherClient client = mailCatcher.getClient();
		client.deleteAllMessages();

		sendEmail("sender@example.com", "recipient@example.com", "Source Test", "Test body");

		List<Message> messages = client.getAllMessages();
		String source = client.getMessageSource(messages.get(0).id());

		assertThat(source).contains("From: sender@example.com")
			.contains("To: recipient@example.com")
			.contains("Subject: Source Test");
	}

	private void sendEmail(String from, String to, String subject, String body) throws Exception {
		Properties props = new Properties();
		props.put("mail.smtp.host", mailCatcher.getSmtpHost());
		props.put("mail.smtp.port", String.valueOf(mailCatcher.getSmtpPort()));

		Session session = Session.getInstance(props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.setRecipient(RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);
		message.setText(body);

		Transport.send(message);
	}

	private void sendEmailToMultipleRecipients(String from, List<String> recipients, String subject, String body)
			throws Exception {
		Properties props = new Properties();
		props.put("mail.smtp.host", mailCatcher.getSmtpHost());
		props.put("mail.smtp.port", String.valueOf(mailCatcher.getSmtpPort()));

		Session session = Session.getInstance(props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));

		for (String recipient : recipients) {
			message.addRecipient(RecipientType.TO, new InternetAddress(recipient));
		}

		message.setSubject(subject);
		message.setText(body);

		Transport.send(message);
	}

}
