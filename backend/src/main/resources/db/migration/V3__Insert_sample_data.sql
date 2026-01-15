-- Sample data for development environment
-- This migration is only applied in development profile

-- Insert sample users
-- Password for ALL users: password123
-- Hash generated with BCrypt (10 rounds) matching Spring Security configuration
-- NOTE: These are DEVELOPMENT-ONLY test users with weak passwords
-- NEVER use these credentials in production
INSERT INTO users (email, password_hash, name, created_at, updated_at) VALUES
('john.doe@example.com', '$2b$10$xOH3XVhVbYSyVrTP9SleZe/NDT/0OSEnwcWyekoBlUDndYTQjSipW', 'John Doe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('jane.smith@example.com', '$2b$10$GeXMs6fsY2MGk3AtHqmgxOiUQgee.fFX/Gyt7IquO0bclrhfX7KuO', 'Jane Smith', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('admin@example.com', '$2b$10$.s7B5t4D5zj9VJojA/sLcuV3UpzYkxgpVpenfUcMjyfMPj.pHQvwy', 'Admin User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample projects for John Doe (user_id = 1)
INSERT INTO projects (user_id, name, project_key, description, created_at, updated_at) VALUES
(1, 'E-commerce Platform', 'ECOM', 'Online shopping platform with payment integration', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Mobile App', 'MOBILE', 'Cross-platform mobile application', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample projects for Jane Smith (user_id = 2)
INSERT INTO projects (user_id, name, project_key, description, created_at, updated_at) VALUES
(2, 'Data Analytics Dashboard', 'DASH', 'Real-time analytics and reporting dashboard', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'API Gateway', 'API', 'Microservices API gateway and management', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample sprints for John Doe
INSERT INTO sprints (user_id, name, start_date, end_date, status, created_at, updated_at) VALUES
(1, 'Sprint 1 - Foundation', '2024-01-01', '2024-01-14', 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Sprint 2 - Core Features', '2024-01-15', '2024-01-28', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Sprint 3 - Polish', '2024-01-29', '2024-02-11', 'PLANNED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample sprints for Jane Smith
INSERT INTO sprints (user_id, name, start_date, end_date, status, created_at, updated_at) VALUES
(2, 'Analytics Sprint 1', '2024-01-08', '2024-01-21', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Analytics Sprint 2', '2024-01-22', '2024-02-04', 'PLANNED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample labels for John Doe
INSERT INTO labels (user_id, name, color, created_at) VALUES
(1, 'frontend', '#FF5733', CURRENT_TIMESTAMP),
(1, 'backend', '#33FF57', CURRENT_TIMESTAMP),
(1, 'urgent', '#FF3333', CURRENT_TIMESTAMP),
(1, 'enhancement', '#3366FF', CURRENT_TIMESTAMP);

-- Insert sample labels for Jane Smith
INSERT INTO labels (user_id, name, color, created_at) VALUES
(2, 'analytics', '#FF8C00', CURRENT_TIMESTAMP),
(2, 'performance', '#9932CC', CURRENT_TIMESTAMP),
(2, 'security', '#DC143C', CURRENT_TIMESTAMP);

-- Insert sample issues for John Doe's E-commerce Platform (project_id = 1)
INSERT INTO issues (user_id, project_id, sprint_id, issue_type_id, title, description, status, priority, story_points, created_at, updated_at) VALUES
(1, 1, 1, 2, 'User Registration System', 'Implement user registration with email verification', 'DONE', 'HIGH', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 1, 1, 2, 'Product Catalog', 'Create product listing and search functionality', 'DONE', 'HIGH', 13, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 1, 2, 2, 'Shopping Cart', 'Implement shopping cart with add/remove items', 'IN_PROGRESS', 'HIGH', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 1, 2, 1, 'Cart Persistence Bug', 'Shopping cart items disappear after page refresh', 'IN_REVIEW', 'MEDIUM', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 1, NULL, 2, 'Payment Integration', 'Integrate with Stripe payment gateway', 'BACKLOG', 'HIGH', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample issues for John Doe's Mobile App (project_id = 2)
INSERT INTO issues (user_id, project_id, sprint_id, issue_type_id, title, description, status, priority, story_points, created_at, updated_at) VALUES
(1, 2, 2, 2, 'Login Screen', 'Create mobile login interface', 'IN_PROGRESS', 'MEDIUM', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 2, NULL, 3, 'Setup CI/CD Pipeline', 'Configure automated build and deployment', 'BACKLOG', 'LOW', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample issues for Jane Smith's Dashboard (project_id = 3)
INSERT INTO issues (user_id, project_id, sprint_id, issue_type_id, title, description, status, priority, story_points, created_at, updated_at) VALUES
(2, 3, 4, 2, 'Real-time Charts', 'Implement live updating charts and graphs', 'IN_PROGRESS', 'HIGH', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 3, 4, 1, 'Chart Performance Issue', 'Charts are slow to load with large datasets', 'SELECTED_FOR_DEVELOPMENT', 'HIGH', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 3, NULL, 2, 'Export Functionality', 'Allow users to export reports as PDF/Excel', 'BACKLOG', 'MEDIUM', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample issue-label associations
INSERT INTO issue_labels (issue_id, label_id) VALUES
(1, 1), -- User Registration -> frontend
(1, 2), -- User Registration -> backend
(2, 1), -- Product Catalog -> frontend
(2, 2), -- Product Catalog -> backend
(3, 1), -- Shopping Cart -> frontend
(4, 2), -- Cart Bug -> backend
(4, 3), -- Cart Bug -> urgent
(5, 2), -- Payment Integration -> backend
(6, 1), -- Login Screen -> frontend
(8, 5), -- Real-time Charts -> analytics
(9, 5), -- Chart Performance -> analytics
(9, 6), -- Chart Performance -> performance
(10, 5); -- Export Functionality -> analytics

-- Insert sample comments
INSERT INTO comments (user_id, issue_id, content, created_at, updated_at) VALUES
(1, 1, 'Email verification is working correctly. Ready for testing.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 3, 'Added basic add/remove functionality. Working on quantity updates.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 4, 'Found the issue - localStorage is not being properly initialized. Fix in progress.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 8, 'Using Chart.js for the implementation. Performance looks good so far.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 9, 'Need to implement data pagination to handle large datasets efficiently.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample audit logs
INSERT INTO audit_logs (user_id, issue_id, action, details, created_at) VALUES
(1, 1, 'STATUS_CHANGE', 'Changed status from IN_PROGRESS to DONE', CURRENT_TIMESTAMP - INTERVAL '2 days'),
(1, 2, 'STATUS_CHANGE', 'Changed status from IN_PROGRESS to DONE', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(1, 3, 'STATUS_CHANGE', 'Changed status from SELECTED_FOR_DEVELOPMENT to IN_PROGRESS', CURRENT_TIMESTAMP - INTERVAL '3 hours'),
(1, 4, 'STATUS_CHANGE', 'Changed status from IN_PROGRESS to IN_REVIEW', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
(2, 8, 'CREATED', 'Issue created', CURRENT_TIMESTAMP - INTERVAL '5 days'),
(2, 9, 'STATUS_CHANGE', 'Changed status from BACKLOG to SELECTED_FOR_DEVELOPMENT', CURRENT_TIMESTAMP - INTERVAL '2 hours');