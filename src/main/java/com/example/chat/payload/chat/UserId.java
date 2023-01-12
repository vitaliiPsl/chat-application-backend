package com.example.chat.payload.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserId {
    @NotBlank(message = "Id of the user is required")
    private String id;
}