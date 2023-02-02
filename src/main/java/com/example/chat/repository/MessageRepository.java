package com.example.chat.repository;

import com.example.chat.model.chat.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findAllByChatId(String chatId, Pageable pageable);

    Page<Message> findAllByChatIdAndIdIsBefore(String chatId, long lastMessageId, Pageable pageable);
}
