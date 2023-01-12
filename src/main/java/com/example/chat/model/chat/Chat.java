package com.example.chat.model.chat;

import com.example.chat.model.chat.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="chats")
public class Chat {
    @Id
    @GeneratedValue(generator="uuid")
    @GenericGenerator(name="uuid", strategy="uuid2")
    private String id;

    private String name;

    @Column(length = 512)
    private String description;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "chat")
    private Set<Member> members = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void addMember(Member member) {
        this.members.add(member);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return Objects.equals(id, chat.id) && Objects.equals(name, chat.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
