package ch.martinelli.oss.testcontainers.mailcatcher;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers implementation for MailCatcher.
 * <p>
 * MailCatcher runs a simple SMTP server which catches any message sent to it and displays
 * it in a web interface.
 * <p>
 * Exposed ports:
 * <ul>
 * <li>1025 - SMTP port for sending emails</li>
 * <li>1080 - HTTP port for the web interface and REST API</li>
 * </ul>
 *
 * @see <a href="https://hub.docker.com/r/dockage/mailcatcher">MailCatcher Docker
 * Image</a>
 */
public class MailCatcherContainer extends GenericContainer<MailCatcherContainer> {

	private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("dockage/mailcatcher");

	private static final String DEFAULT_TAG = "0.9.0";

	public static final int SMTP_PORT = 1025;

	public static final int HTTP_PORT = 1080;

	public MailCatcherContainer() {
		this(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
	}

	public MailCatcherContainer(String dockerImageName) {
		this(DockerImageName.parse(dockerImageName));
	}

	public MailCatcherContainer(DockerImageName dockerImageName) {
		super(dockerImageName);
		dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);

		withExposedPorts(SMTP_PORT, HTTP_PORT);
		waitingFor(Wait.forHttp("/").forPort(HTTP_PORT));
	}

	/**
	 * Returns the mapped SMTP port for sending emails.
	 * @return the mapped SMTP port
	 */
	public int getSmtpPort() {
		return getMappedPort(SMTP_PORT);
	}

	/**
	 * Returns the mapped HTTP port for the web interface and REST API.
	 * @return the mapped HTTP port
	 */
	public int getHttpPort() {
		return getMappedPort(HTTP_PORT);
	}

	/**
	 * Returns the SMTP host address.
	 * @return the SMTP host
	 */
	public String getSmtpHost() {
		return getHost();
	}

	/**
	 * Returns the base URL for the MailCatcher web interface and REST API.
	 * @return the base URL (e.g., "http://localhost:32789")
	 */
	public String getHttpUrl() {
		return String.format("http://%s:%d", getHost(), getHttpPort());
	}

	/**
	 * Returns a {@link MailCatcherClient} for interacting with the MailCatcher REST API.
	 * @return a new MailCatcherClient instance
	 */
	public MailCatcherClient getClient() {
		return new MailCatcherClient(getHttpUrl());
	}

}
