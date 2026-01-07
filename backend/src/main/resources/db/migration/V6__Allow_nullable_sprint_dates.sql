-- Migration V6: Allow nullable sprint dates for conflict resolution
-- This allows sprints to have their dates cleared when conflicts occur during early activation

-- Remove NOT NULL constraints from sprint dates
ALTER TABLE sprints ALTER COLUMN start_date DROP NOT NULL;
ALTER TABLE sprints ALTER COLUMN end_date DROP NOT NULL;