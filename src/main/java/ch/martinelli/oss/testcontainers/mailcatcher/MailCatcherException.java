package ch.martinelli.oss.testcontainers.mailcatcher;

/**
 * Exception thrown when an error occurs while interacting with MailCatcher.
 */
public class MailCatcherException extends RuntimeException {

	public MailCatcherException(String message) {
		super(message);
	}

	public MailCatcherException(String message, Throwable cause) {
		super(message, cause);
	}

}
