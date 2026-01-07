-- Add goal column to sprints table
-- This migration adds support for sprint goals in the sprint planning feature

ALTER TABLE sprints ADD COLUMN goal VARCHAR(500);

-- Add comment for documentation
COMMENT ON COLUMN sprints.goal IS 'Optional sprint goal description for planning purposes';