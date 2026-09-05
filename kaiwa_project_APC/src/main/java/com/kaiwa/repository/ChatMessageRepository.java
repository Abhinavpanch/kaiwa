package com.kaiwa.repository;

import com.kaiwa.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByRoomIdOrderByTimestampAsc(String roomId);

    // All messages that involve the given user (sent by or addressed to them),
    // newest first — used to build the direct-message conversation list.
    List<ChatMessage> findBySenderOrRecipientOrderByTimestampDesc(String sender, String recipient);

    // Unread direct messages for a user inside one conversation.
    List<ChatMessage> findByRoomIdAndRecipientAndReadFalse(String roomId, String recipient);
}
