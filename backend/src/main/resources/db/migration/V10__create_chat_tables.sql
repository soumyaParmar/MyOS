-- V10: Create Chat and Conversation tables for AI Agents
-- This migration sets up the relational storage for AI-user interactions.
-- It follows the consolidated PostgreSQL approach for AI memory.

-- 1. Create the conversations table to group messages by user and agent type
-- Each record represents a persistent chat session between a user and a specific AI agent.
CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    agent_type VARCHAR(50) NOT NULL, -- Examples: 'JOB_AGENT', 'FINANCE_AGENT', 'SOCIAL_AGENT'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversation_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 2. Create the chat_messages table to store individual dialogue lines
-- These are the individual lines of text exchanged within a conversation.
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'USER', 'ASSISTANT', or 'SYSTEM'
    content TEXT NOT NULL,
    metadata JSONB, -- Stores dynamic data like token usage, model ID, or tool outputs
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

-- Indexing for performance
-- We index user_id and conversation_id to ensure fast retrieval of chat history as the database grows.
CREATE INDEX idx_conversations_user_id ON conversations(user_id);
CREATE INDEX idx_chat_messages_conversation_id ON chat_messages(conversation_id);
