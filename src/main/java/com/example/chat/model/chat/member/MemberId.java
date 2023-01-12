package com.example.chat.model.chat.member;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MemberId implements Serializable {
    private String userId;
    private String chatId;
}
