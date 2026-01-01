-- Initialize development database
-- This script runs when the PostgreSQL container starts for the first time

-- Create additional databases if needed
-- CREATE DATABASE issue_tracker_test;

-- Set up any initial configuration
-- You can add initial data or configuration here if needed

-- Enable extensions that might be useful
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Log initialization
SELECT 'Database initialized successfully' as status;