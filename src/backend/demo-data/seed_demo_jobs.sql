-- Demo data for presentations: 4 recruiter accounts and 12 job postings.
-- NOT a Flyway migration (it lives outside db/migration), so it never runs on its own.
-- Safe to run more than once: rows that already exist are skipped.
-- Remove everything it adds with remove_demo_jobs.sql.
--
-- Every demo recruiter's email ends in @demo.careerconnect.test and the password is demo1234,
-- so you can sign in as one to show the recruiter side (e.g. talent@demo.careerconnect.test).
-- Deadlines are relative to the day the script runs, so the postings stay open for a while.

BEGIN;

INSERT INTO employer_user (email, recruiter_name, recruiter_surname, company_name, country, province_state,
                           city, address, postal_code_zip, phone, password)
SELECT v.email, v.first_name, v.last_name, v.company, 'Canada', v.province, v.city, v.address, v.postal, v.phone, 'demo1234'
FROM (VALUES
    ('talent@demo.careerconnect.test',  'Sofia',  'Marchetti', 'Lumen Labs',           'Quebec',  'Montreal', '1250 Rue Peel',        'H3B 4W8', '514-555-0101'),
    ('hiring@demo.careerconnect.test',  'Daniel', 'Okafor',    'Northwind Robotics',   'Quebec',  'Montreal', '400 Rue Saint-Jacques', 'H2Y 1S1', '514-555-0102'),
    ('jobs@demo.careerconnect.test',    'Amelie', 'Tremblay',  'Atelier Pixel',        'Quebec',  'Laval',    '3030 Boul. Le Carrefour','H7T 2P5', '450-555-0103'),
    ('careers@demo.careerconnect.test', 'Priya',  'Shah',      'Mont-Royal Analytics', 'Ontario', 'Toronto',  '100 King St W',        'M5X 1A9', '416-555-0104')
) AS v(email, first_name, last_name, company, province, city, address, postal, phone)
WHERE NOT EXISTS (SELECT 1 FROM employer_user e WHERE LOWER(e.email) = v.email);

INSERT INTO job_postings (employer_id, title, description, department, application_deadline, salary,
                          required_skills, status, location)
SELECT e.employer_id, v.title, v.description, v.department, CURRENT_DATE + v.days_open, v.salary, v.skills, v.status, v.location
FROM (VALUES
    ('talent@demo.careerconnect.test', 'Junior Frontend Developer', 'Technology',
     'Help us ship the interface 40,000 people use every day. You will pair with designers, own small features end to end, and get a mentor from week one.',
     'React, TypeScript, CSS, Git', '$62,000 - $74,000', 'Montreal, QC (hybrid)', 30, 'Open'),
    ('talent@demo.careerconnect.test', 'QA Automation Tester', 'Technology',
     'Write and maintain end-to-end tests for our web app and help the team release with confidence every week.',
     'Selenium, Java, JUnit, CI/CD', '$58,000 - $68,000', 'Montreal, QC', 21, 'Open'),
    ('talent@demo.careerconnect.test', 'Software Developer Co-op (Winter)', 'Technology',
     'A four-month co-op on our platform team. You will fix real bugs in week one and ship a feature by the end of your term.',
     'Java, Spring Boot, SQL', '$25/hr', 'Montreal, QC', 45, 'Open'),
    ('hiring@demo.careerconnect.test', 'Java Backend Developer', 'Engineering',
     'Build the services that coordinate fleets of warehouse robots. Spring Boot, PostgreSQL, and a team that writes tests first.',
     'Java, Spring Boot, PostgreSQL, Docker', '$78,000 - $95,000', 'Montreal, QC', 28, 'Open'),
    ('hiring@demo.careerconnect.test', 'Embedded Software Intern', 'Engineering',
     'Work on the firmware that keeps our robots moving. You will test on real hardware in our lab every week.',
     'C, C++, Linux, Git', '$27/hr', 'Montreal, QC', 35, 'Open'),
    ('hiring@demo.careerconnect.test', 'DevOps Engineer', 'Engineering',
     'Own our build pipelines and cloud infrastructure, and help developers get code to production safely.',
     'AWS, Kubernetes, Terraform, Linux', '$90,000 - $110,000', 'Remote, Canada', 20, 'Open'),
    ('jobs@demo.careerconnect.test', 'Product Designer', 'Design',
     'Redesign onboarding for a booking app used by independent studios. You will run research sessions and hand off in Figma.',
     'Figma, Prototyping, User research', '$45 - $55/hr', 'Laval, QC', 25, 'Open'),
    ('jobs@demo.careerconnect.test', 'Marketing Coordinator', 'Marketing',
     'Plan our social and email campaigns and report on what works. Great for someone who likes both words and numbers.',
     'Copywriting, Social media, Google Analytics', '$50,000 - $58,000', 'Laval, QC (hybrid)', 18, 'Open'),
    ('jobs@demo.careerconnect.test', 'UX Research Intern', 'Design',
     'Talk to our users, turn interviews into insights, and help decide what we build next.',
     'Interviewing, Survey design, Note-taking', '$22/hr', 'Remote, Canada', 40, 'Open'),
    ('careers@demo.careerconnect.test', 'Data Analyst Intern', 'Data & Analytics',
     'Turn messy retail data into dashboards the whole company reads. Four-month placement with a path to a full-time offer.',
     'SQL, Python, Tableau', '$24/hr', 'Toronto, ON', 30, 'Open'),
    ('careers@demo.careerconnect.test', 'Financial Analyst', 'Finance',
     'Build forecasts and pricing models for our analytics contracts and present them to clients.',
     'Excel, Financial modelling, SQL', '$70,000 - $82,000', 'Toronto, ON', 22, 'Open'),
    -- Closed by the recruiter: hidden from job search, shown as Closed on the recruiter's postings page
    ('careers@demo.careerconnect.test', 'Machine Learning Engineer', 'Data & Analytics',
     'Deploy and monitor the models behind our demand forecasts.',
     'Python, PyTorch, MLOps', '$105,000 - $125,000', 'Toronto, ON', 15, 'Closed')
) AS v(email, title, department, description, skills, salary, location, days_open, status)
JOIN employer_user e ON LOWER(e.email) = v.email
WHERE NOT EXISTS (SELECT 1 FROM job_postings p WHERE p.employer_id = e.employer_id AND p.title = v.title);

COMMIT;
