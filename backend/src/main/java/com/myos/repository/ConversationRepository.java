package com.myos.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myos.entity.Conversation;
import com.myos.enums.AgentType;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    
    Optional<Conversation> findByUserIdAndAgentType(UUID userId, AgentType agentType);
    
}
