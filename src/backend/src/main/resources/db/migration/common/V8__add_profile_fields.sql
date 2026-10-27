-- The parts of a job seeker's profile they write themselves (see ProfileController).
-- Skills are stored comma-separated. All nullable so existing accounts stay valid.
ALTER TABLE person_user ADD COLUMN IF NOT EXISTS headline VARCHAR(120);
ALTER TABLE person_user ADD COLUMN IF NOT EXISTS about VARCHAR(1200);
ALTER TABLE person_user ADD COLUMN IF NOT EXISTS skills VARCHAR(500);
