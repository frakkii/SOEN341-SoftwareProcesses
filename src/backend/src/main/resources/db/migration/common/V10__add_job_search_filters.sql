-- Search metadata is nullable so existing recruiter postings remain valid.
ALTER TABLE job_postings ADD COLUMN IF NOT EXISTS location VARCHAR(255);
ALTER TABLE job_postings ADD COLUMN IF NOT EXISTS industry VARCHAR(255);
ALTER TABLE job_postings ADD COLUMN IF NOT EXISTS job_type VARCHAR(100);
