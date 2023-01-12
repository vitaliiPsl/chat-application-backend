package com.example.chat.repository;

import com.example.chat.model.chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, String> {

    @Query("select c from Chat c join Member m on c.id = m.chat.id where m.user.id = :userId")
    List<Chat> findByUserId(String userId);
}
