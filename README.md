# Testcontainers MailCatcher

A [Testcontainers](https://www.testcontainers.org/) module for [MailCatcher](https://mailcatcher.me/) - a simple SMTP
server that catches all mail sent to it and displays it in a web interface.

## Installation

Add the following dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>ch.martinelli.oss</groupId>
    <artifactId>testcontainers-mailcatcher</artifactId>
    <version>0.0.1</version>
    <scope>test</scope>
</dependency>
```

## Usage

### Basic Usage

```java

@Testcontainers
class EmailServiceTest {

    @Container
    static MailCatcherContainer mailCatcher = new MailCatcherContainer();

    @Test
    void shouldSendEmail() throws Exception {
        // Configure your mail sender
        Properties props = new Properties();
        props.put("mail.smtp.host", mailCatcher.getSmtpHost());
        props.put("mail.smtp.port", String.valueOf(mailCatcher.getSmtpPort()));

        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("sender@example.com"));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress("recipient@example.com"));
        message.setSubject("Test Subject");
        message.setText("Hello, this is a test email!");

        Transport.send(message);

        // Verify the email was caught
        MailCatcherClient client = mailCatcher.getClient();
        List<Message> messages = client.getAllMessages();

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).subject()).isEqualTo("Test Subject");
    }

}
```

### Container Configuration

The `MailCatcherContainer` exposes two ports:

| Port | Description                              |
|------|------------------------------------------|
| 1025 | SMTP port for sending emails             |
| 1080 | HTTP port for web interface and REST API |

```java
// Create container with default image (dockage/mailcatcher:0.9.0)
MailCatcherContainer mailCatcher = new MailCatcherContainer();

// Or specify a custom image version
MailCatcherContainer mailCatcher = new MailCatcherContainer("dockage/mailcatcher:0.8.2");

// Get connection details
String smtpHost = mailCatcher.getSmtpHost();
int smtpPort = mailCatcher.getSmtpPort();
int httpPort = mailCatcher.getHttpPort();
String httpUrl = mailCatcher.getHttpUrl(); // e.g., "http://localhost:32789"
```

### MailCatcherClient API

The `MailCatcherClient` provides methods to interact with caught emails:

```java
MailCatcherClient client = mailCatcher.getClient();

// Get all messages
List<Message> messages = client.getAllMessages();

// Get message count
int count = client.getMessageCount();

// Get a specific message
Message message = client.getMessage(1);

// Get message content
String html = client.getMessageHtml(1);    // HTML body
String plain = client.getMessagePlain(1);  // Plain text body
String source = client.getMessageSource(1); // Raw email source

// Delete messages
client.

deleteMessage(1);     // Delete specific message
client.

deleteAllMessages();  // Delete all messages
```

### Message Properties

The `Message` record contains the following properties:

| Property     | Type           | Description                             |
|--------------|----------------|-----------------------------------------|
| `id`         | `int`          | Unique message identifier               |
| `sender`     | `String`       | Email sender address                    |
| `recipients` | `List<String>` | List of recipient addresses             |
| `subject`    | `String`       | Email subject                           |
| `size`       | `String`       | Message size                            |
| `createdAt`  | `Instant`      | Timestamp when the message was received |

## Requirements

- Java 17 or higher
- Docker

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
