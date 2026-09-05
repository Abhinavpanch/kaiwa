package com.kaiwa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import jakarta.validation.constraints.NotBlank;
import com.kaiwa.validation.WordCount;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
public class ChatMessage {
    @Id
    private String id;
    private String roomId;

    @NotBlank(message = "sender must not be blank")
    private String sender;

    @NotBlank(message = "content must not be blank")
    private String content;

    private Instant timestamp;

    /** For direct messages: the receiving user. Null for channel messages. */
    private String recipient;

    /** Read receipt for direct messages. Ignored for channel messages. */
    private boolean read;
}
