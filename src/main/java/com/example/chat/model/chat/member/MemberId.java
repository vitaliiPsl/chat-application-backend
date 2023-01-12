package com.example.chat.model.chat.member;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MemberId implements Serializable {
    private String userId;
    private String chatId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberId memberId = (MemberId) o;
        return Objects.equals(userId, memberId.userId) && Objects.equals(chatId, memberId.chatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, chatId);
    }
}
