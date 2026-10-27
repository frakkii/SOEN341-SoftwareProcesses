-- Removes everything seed_demo_jobs.sql added: the demo recruiters (emails ending in
-- @demo.careerconnect.test), their postings, and any applications to those postings.
-- Nothing else is touched.

BEGIN;

DELETE FROM application
WHERE post_id IN (SELECT p.post_id FROM job_postings p JOIN employer_user e ON e.employer_id = p.employer_id
                  WHERE LOWER(e.email) LIKE '%@demo.careerconnect.test');

DELETE FROM job_postings
WHERE employer_id IN (SELECT employer_id FROM employer_user WHERE LOWER(email) LIKE '%@demo.careerconnect.test');

DELETE FROM employer_user WHERE LOWER(email) LIKE '%@demo.careerconnect.test';

COMMIT;
