-- Performance Optimization Indexes (Essential only)
-- This migration adds essential indexes for frequently queried fields

-- Basic indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_issues_user_status 
ON issues(user_id, status);

CREATE INDEX IF NOT EXISTS idx_issues_user_project 
ON issues(user_id, project_id);

CREATE INDEX IF NOT EXISTS idx_issues_created_at 
ON issues(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_projects_user 
ON projects(user_id);

-- Statistics will be updated automatically by PostgreSQL
-- or can be run manually with ANALYZE command after migration