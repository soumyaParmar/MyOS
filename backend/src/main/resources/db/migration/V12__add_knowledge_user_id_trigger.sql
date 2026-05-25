-- V12: Add database trigger to automatically map user_id from metadata JSONB
CREATE OR REPLACE FUNCTION set_knowledge_base_user_id()
RETURNS TRIGGER AS $$
BEGIN
    -- Extract the user_id from the metadata jsonb object and set it to the user_id column
    IF NEW.user_id IS NULL AND NEW.metadata->>'user_id' IS NOT NULL THEN
        NEW.user_id := (NEW.metadata->>'user_id')::uuid;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_set_knowledge_base_user_id
BEFORE INSERT ON knowledge_base
FOR EACH ROW
EXECUTE FUNCTION set_knowledge_base_user_id();