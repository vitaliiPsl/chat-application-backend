package com.example.chat.repository;

import com.example.chat.model.chat.member.Member;
import com.example.chat.model.chat.member.MemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, MemberId> {

}
