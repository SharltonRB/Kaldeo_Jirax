-- Add status column to projects table for project completion tracking
-- This enables automatic project status updates based on epic completion

-- Add status column with default value
ALTER TABLE projects 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS';

-- Add check constraint to ensure valid status values
ALTER TABLE projects 
ADD CONSTRAINT chk_project_status 
CHECK (status IN ('IN_PROGRESS', 'DONE'));

-- Create index for efficient status-based queries
CREATE INDEX idx_project_status ON projects(status);

-- Update existing projects to have IN_PROGRESS status (already set by default)
-- This is just for clarity and documentation
UPDATE projects SET status = 'IN_PROGRESS' WHERE status = 'IN_PROGRESS';