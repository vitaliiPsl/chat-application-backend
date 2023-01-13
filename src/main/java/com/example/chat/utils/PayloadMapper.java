package com.example.chat.utils;

import com.example.chat.model.chat.Chat;
import com.example.chat.model.chat.member.Member;
import com.example.chat.model.user.User;
import com.example.chat.payload.chat.ChatDto;
import com.example.chat.payload.chat.MemberDto;
import com.example.chat.payload.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PayloadMapper {
    private final ModelMapper modelMapper;

    public UserDto mapUserToUserDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    public User mapUserDtoToUser(UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }

    public ChatDto mapChatToChatDto(Chat chat) {
        return modelMapper.map(chat, ChatDto.class);
    }

    public Chat mapChatDtoToChat(ChatDto chatDto) {
        return modelMapper.map(chatDto, Chat.class);
    }

    public MemberDto mapMemberToMemberDto(Member member) {
        return modelMapper.map(member, MemberDto.class);
    }

    public Member mapMemberDtoToMember(MemberDto memberDto) {
        return modelMapper.map(memberDto, Member.class);
    }
}
