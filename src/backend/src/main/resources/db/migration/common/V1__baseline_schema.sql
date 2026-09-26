-- The schema as it was first created in the Supabase dashboard. On Supabase this
-- version is marked as already applied (spring.flyway.baseline-on-migrate), so it
-- only runs on an empty database such as the H2 one used by the tests.

CREATE TABLE person_user (
    person_id             INTEGER      NOT NULL,
    email                 VARCHAR(255) NOT NULL,
    name                  VARCHAR(255) NOT NULL,
    surname               VARCHAR(255) NOT NULL,
    country               VARCHAR(50)  NOT NULL,
    province_state        VARCHAR(50)  NOT NULL,
    city                  VARCHAR(50)  NOT NULL,
    address               VARCHAR(50)  NOT NULL,
    postal_code_zip       VARCHAR(15)  NOT NULL,
    phone                 INTEGER      NOT NULL,
    total_applications    SMALLINT,
    pending_applications  SMALLINT,
    refused_applications  SMALLINT,
    accepted_applications SMALLINT,
    fav_postings          SMALLINT,
    password              VARCHAR(255) NOT NULL,
    CONSTRAINT person_user_pkey PRIMARY KEY (person_id)
);

CREATE TABLE employer_user (
    employer_id         INTEGER      NOT NULL,
    email               VARCHAR(255) NOT NULL,
    recruiter_name      VARCHAR(255) NOT NULL,
    company_name        VARCHAR(255) NOT NULL,
    country             VARCHAR(50)  NOT NULL,
    province_state      VARCHAR(50)  NOT NULL,
    city                VARCHAR(50)  NOT NULL,
    address             VARCHAR(50)  NOT NULL,
    postal_code_zip     VARCHAR(15)  NOT NULL,
    phone               INTEGER      NOT NULL,
    total_received_appl INTEGER,
    pending_appl        INTEGER,
    refused_appl        INTEGER,
    accepted_appl       INTEGER,
    password            VARCHAR(255) NOT NULL,
    active_postings     SMALLINT,
    recruiter_surname   VARCHAR(255) NOT NULL,
    CONSTRAINT employer_user_pkey PRIMARY KEY (employer_id)
);

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

CREATE TABLE fav_postings (
    person_id INTEGER NOT NULL,
    post_id   INTEGER NOT NULL,
    status    BOOLEAN NOT NULL,
    CONSTRAINT fav_postings_person_id_fkey FOREIGN KEY (person_id) REFERENCES person_user (person_id),
    CONSTRAINT fav_postings_post_id_fkey FOREIGN KEY (post_id) REFERENCES job_postings (post_id)
);
