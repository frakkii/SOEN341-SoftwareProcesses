-- One account per email address in each table
ALTER TABLE person_user ADD CONSTRAINT person_user_email_key UNIQUE (email);
ALTER TABLE employer_user ADD CONSTRAINT employer_user_email_key UNIQUE (email);

-- INTEGER tops out at 2147483647, too small for a 10-digit phone number, and
-- can't hold a leading zero, "+" or an extension
ALTER TABLE person_user ALTER COLUMN phone SET DATA TYPE VARCHAR(20);
ALTER TABLE employer_user ALTER COLUMN phone SET DATA TYPE VARCHAR(20);
