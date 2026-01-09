package ch.martinelli.oss.testcontainers.mailcatcher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Represents an email message caught by MailCatcher.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Message(@JsonProperty("id") int id, @JsonProperty("sender") String sender,
		@JsonProperty("recipients") List<String> recipients, @JsonProperty("subject") String subject,
		@JsonProperty("size") String size, @JsonProperty("created_at") Instant createdAt) {
}
