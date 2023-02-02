package com.example.chat.model.chat.member;

import com.example.chat.model.chat.Chat;
import com.example.chat.model.user.User;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "members")
public class Member {
    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "userId", column = @Column(name = "user_id")),
            @AttributeOverride(name = "chatId", column = @Column(name = "chat_id"))
    })
    private MemberId id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private User user;

    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "chat_id", nullable = false, insertable = false, updatable = false)
    private Chat chat;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    private LocalDateTime joinedAt;
    private LocalDateTime updatedAt;

    public Member(User user, Chat chat) {
        this.id = new MemberId(user.getId(), chat.getId());
        this.user = user;
        this.chat = chat;
        this.joinedAt = LocalDateTime.now();
    }

    public Member(User user, Chat chat, MemberRole role) {
        this(user, chat);
        this.role = role;
    }

    public Member(User user, Chat chat, MemberRole role, LocalDateTime joinedAt) {
        this(user, chat, role);
        this.joinedAt = joinedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member that = (Member) o;
        return Objects.equals(user, that.user) && Objects.equals(chat, that.chat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, chat);
    }
}
