-- Insert global issue types that are available to all users
-- These types don't belong to any specific project (project_id is NULL)

INSERT INTO issue_types (project_id, name, description, is_global, created_at) VALUES
(NULL, 'BUG', 'A problem or defect in the system that needs to be fixed', TRUE, CURRENT_TIMESTAMP),
(NULL, 'STORY', 'A user story or feature request from the user perspective', TRUE, CURRENT_TIMESTAMP),
(NULL, 'TASK', 'A general task or work item that needs to be completed', TRUE, CURRENT_TIMESTAMP),
(NULL, 'EPIC', 'A large body of work that can be broken down into smaller stories', TRUE, CURRENT_TIMESTAMP);