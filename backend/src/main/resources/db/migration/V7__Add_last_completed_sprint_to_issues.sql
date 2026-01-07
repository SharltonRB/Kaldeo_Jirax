-- Add field to track the last completed sprint an issue belonged to
-- This allows us to show incomplete issues from completed sprints with visual indicators

ALTER TABLE issues 
ADD COLUMN last_completed_sprint_id BIGINT REFERENCES sprints(id) ON DELETE SET NULL;

-- Create index for efficient queries
CREATE INDEX idx_issue_last_completed_sprint ON issues(last_completed_sprint_id);

-- Add comment for documentation
COMMENT ON COLUMN issues.last_completed_sprint_id IS 'References the last completed sprint this issue was part of, used to show incomplete issues from completed sprints';