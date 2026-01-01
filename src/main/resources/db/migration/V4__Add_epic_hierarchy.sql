-- Add epic hierarchy support to issues
-- This migration adds parent-child relationships between issues where:
-- - Epics can have child issues (or no children)
-- - Non-epic issues MUST have a parent epic

-- Add parent_issue_id column to issues table
ALTER TABLE issues ADD COLUMN parent_issue_id BIGINT REFERENCES issues(id) ON DELETE CASCADE;

-- Create index for parent-child queries
CREATE INDEX idx_issue_parent ON issues(parent_issue_id);

-- Create index for finding root epics (issues without parent)
CREATE INDEX idx_issue_root ON issues(parent_issue_id) WHERE parent_issue_id IS NULL;

-- Add constraint to ensure only EPIC type issues can be parents (root level)
-- This will be enforced at application level initially, then we can add a database constraint later

-- Update existing issues to have proper hierarchy
-- For now, we'll leave existing issues as they are since this is a new feature
-- In production, you might want to create default epics for existing issues

-- Add check constraint to ensure epic hierarchy rules:
-- 1. If parent_issue_id IS NULL, then issue_type must be EPIC
-- 2. If parent_issue_id IS NOT NULL, then issue_type must NOT be EPIC
-- Note: We'll implement this as application logic first, then add DB constraints later for safety

COMMENT ON COLUMN issues.parent_issue_id IS 'Reference to parent epic. NULL for epic issues, required for non-epic issues';