CREATE TABLE job_postings (
    employer_id INTEGER      NOT NULL,
    post_id     INTEGER      NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    posted_date TIMESTAMP    NOT NULL,
    deadline    DATE         NOT NULL,
    status      BOOLEAN      NOT NULL,
    CONSTRAINT job_postings_pkey PRIMARY KEY (post_id),
    CONSTRAINT job_postings_employer_id_fkey FOREIGN KEY (employer_id) REFERENCES employer_user (employer_id)
);