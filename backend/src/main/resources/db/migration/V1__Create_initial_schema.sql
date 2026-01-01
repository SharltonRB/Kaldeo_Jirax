-- Personal Issue Tracker - Initial Database Schema
-- This migration creates all the core tables with proper indexes and constraints

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on email for fast lookups
CREATE UNIQUE INDEX idx_user_email ON users(email);

-- Projects table
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    project_key VARCHAR(10) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for projects
CREATE INDEX idx_project_user ON projects(user_id);
CREATE UNIQUE INDEX idx_project_key ON projects(user_id, project_key);

-- Issue types table (supports both global and project-specific types)
CREATE TABLE issue_types (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    is_global BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for issue types
CREATE INDEX idx_issue_type_project ON issue_types(project_id);
CREATE UNIQUE INDEX idx_issue_type_name ON issue_types(project_id, name);

-- Sprints table
CREATE TABLE sprints (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED' CHECK (status IN ('PLANNED', 'ACTIVE', 'COMPLETED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for sprints
CREATE INDEX idx_sprint_user ON sprints(user_id);
CREATE INDEX idx_sprint_status ON sprints(user_id, status);
CREATE INDEX idx_sprint_dates ON sprints(start_date, end_date);

-- Labels table
CREATE TABLE labels (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7) CHECK (color ~ '^#[0-9A-Fa-f]{6}$'),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for labels
CREATE INDEX idx_label_user ON labels(user_id);
CREATE UNIQUE INDEX idx_label_name ON labels(user_id, name);

-- Issues table
CREATE TABLE issues (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    sprint_id BIGINT REFERENCES sprints(id) ON DELETE SET NULL,
    issue_type_id BIGINT NOT NULL REFERENCES issue_types(id) ON DELETE RESTRICT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'BACKLOG' CHECK (status IN ('BACKLOG', 'SELECTED_FOR_DEVELOPMENT', 'IN_PROGRESS', 'IN_REVIEW', 'DONE')),
    priority VARCHAR(10) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    story_points INTEGER CHECK (story_points >= 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for issues (optimized for common queries)
CREATE INDEX idx_issue_user_project ON issues(user_id, project_id);
CREATE INDEX idx_issue_status ON issues(status);
CREATE INDEX idx_issue_priority ON issues(priority);
CREATE INDEX idx_issue_sprint ON issues(sprint_id);
CREATE INDEX idx_issue_type ON issues(issue_type_id);

-- Issue labels junction table (many-to-many relationship)
CREATE TABLE issue_labels (
    issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    label_id BIGINT NOT NULL REFERENCES labels(id) ON DELETE CASCADE,
    PRIMARY KEY (issue_id, label_id)
);

-- Create indexes for issue labels
CREATE INDEX idx_issue_labels_issue ON issue_labels(issue_id);
CREATE INDEX idx_issue_labels_label ON issue_labels(label_id);

-- Comments table
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for comments
CREATE INDEX idx_comment_user ON comments(user_id);
CREATE INDEX idx_comment_issue ON comments(issue_id);
CREATE INDEX idx_comment_created ON comments(issue_id, created_at);

-- Audit logs table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for audit logs
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_issue ON audit_logs(issue_id);
CREATE INDEX idx_audit_created ON audit_logs(issue_id, created_at);
CREATE INDEX idx_audit_action ON audit_logs(action);

-- Add constraints to ensure data integrity
ALTER TABLE sprints ADD CONSTRAINT chk_sprint_dates CHECK (end_date >= start_date);

-- Add constraint to ensure only one active sprint per user
CREATE UNIQUE INDEX idx_sprint_user_active ON sprints(user_id) WHERE status = 'ACTIVE';