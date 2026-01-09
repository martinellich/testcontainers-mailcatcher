package ch.martinelli.oss.testcontainers.mailcatcher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Client for interacting with the MailCatcher REST API.
 * <p>
 * Provides methods to retrieve, inspect, and delete caught email messages.
 */
public class MailCatcherClient {

	private final String baseUrl;

	private final HttpClient httpClient;

	private final ObjectMapper objectMapper;

	public MailCatcherClient(String baseUrl) {
		this.baseUrl = baseUrl;
		this.httpClient = HttpClient.newHttpClient();
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());
	}

	/**
	 * Retrieves all messages from MailCatcher.
	 * @return a list of all caught messages
	 * @throws MailCatcherException if an error occurs while fetching messages
	 */
	public List<Message> getAllMessages() {
		try {
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + "/messages"))
				.header("Accept", "application/json")
				.GET()
				.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				throw new MailCatcherException("Failed to fetch messages: HTTP " + response.statusCode());
			}

			return objectMapper.readValue(response.body(), new TypeReference<>() {
			});
		}
		catch (IOException | InterruptedException e) {
			throw new MailCatcherException("Failed to fetch messages", e);
		}
	}

	/**
	 * Returns the number of messages in MailCatcher.
	 * @return the message count
	 * @throws MailCatcherException if an error occurs
	 */
	public int getMessageCount() {
		return getAllMessages().size();
	}

	/**
	 * Retrieves a specific message by ID.
	 * @param id the message ID
	 * @return the message details
	 * @throws MailCatcherException if an error occurs or the message is not found
	 */
	public Message getMessage(int id) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + "/messages/" + id + ".json"))
				.header("Accept", "application/json")
				.GET()
				.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 404) {
				throw new MailCatcherException("Message not found: " + id);
			}
			if (response.statusCode() != 200) {
				throw new MailCatcherException("Failed to fetch message: HTTP " + response.statusCode());
			}

			return objectMapper.readValue(response.body(), Message.class);
		}
		catch (IOException | InterruptedException e) {
			throw new MailCatcherException("Failed to fetch message", e);
		}
	}

	/**
	 * Retrieves the HTML body of a message.
	 * @param id the message ID
	 * @return the HTML body, or null if not available
	 * @throws MailCatcherException if an error occurs
	 */
	public String getMessageHtml(int id) {
		return fetchMessagePart(id, "html");
	}

	/**
	 * Retrieves the plain text body of a message.
	 * @param id the message ID
	 * @return the plain text body, or null if not available
	 * @throws MailCatcherException if an error occurs
	 */
	public String getMessagePlain(int id) {
		return fetchMessagePart(id, "plain");
	}

	/**
	 * Retrieves the raw source of a message.
	 * @param id the message ID
	 * @return the raw message source
	 * @throws MailCatcherException if an error occurs
	 */
	public String getMessageSource(int id) {
		return fetchMessagePart(id, "source");
	}

	private String fetchMessagePart(int id, String part) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + "/messages/" + id + "." + part))
				.GET()
				.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 404) {
				return null;
			}
			if (response.statusCode() != 200) {
				throw new MailCatcherException("Failed to fetch message part: HTTP " + response.statusCode());
			}

			return response.body();
		}
		catch (IOException | InterruptedException e) {
			throw new MailCatcherException("Failed to fetch message part", e);
		}
	}

	/**
	 * Deletes all messages from MailCatcher.
	 * @throws MailCatcherException if an error occurs
	 */
	public void deleteAllMessages() {
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/messages")).DELETE().build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200 && response.statusCode() != 204) {
				throw new MailCatcherException("Failed to delete messages: HTTP " + response.statusCode());
			}
		}
		catch (IOException | InterruptedException e) {
			throw new MailCatcherException("Failed to delete messages", e);
		}
	}

	/**
	 * Deletes a specific message.
	 * @param id the message ID to delete
	 * @throws MailCatcherException if an error occurs
	 */
	public void deleteMessage(int id) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + "/messages/" + id))
				.DELETE()
				.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200 && response.statusCode() != 204) {
				throw new MailCatcherException("Failed to delete message: HTTP " + response.statusCode());
			}
		}
		catch (IOException | InterruptedException e) {
			throw new MailCatcherException("Failed to delete message", e);
		}
	}

}
