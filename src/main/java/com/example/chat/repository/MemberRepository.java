package com.example.chat.repository;

import com.example.chat.model.chat.member.Member;
import com.example.chat.model.chat.member.MemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, MemberId> {

    List<Member> findByChat_Id(String chatId);
}
