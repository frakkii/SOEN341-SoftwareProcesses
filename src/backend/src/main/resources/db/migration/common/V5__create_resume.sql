-- One resume per job seeker: person_id is the primary key, so uploading a new
-- resume replaces the old row. Deleting a job seeker also deletes their resume.
CREATE TABLE resume (
    person_id    INTEGER                  NOT NULL,
    file_name    VARCHAR(255)             NOT NULL,
    content_type VARCHAR(100)             NOT NULL,
    file_size    INTEGER                  NOT NULL,
    data         BYTEA                    NOT NULL,
    uploaded_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT resume_pkey PRIMARY KEY (person_id),
    CONSTRAINT resume_person_id_fkey FOREIGN KEY (person_id) REFERENCES person_user (person_id) ON DELETE CASCADE
);
