Task: Build the job seeker homepage and connect it to the login page

Date: 2026-09-26

AI Tool: Claude (claude.ai chat)

Purpose of AI Use: AI was used to learn the basics of frontend development (no prior experience), understand how the frontend connects to the Spring Boot backend, get starter code for the homepage, and debug running the app locally.

Chat Link or Prompt/Response: check Appendix A

AI-Suggested Content:

Explanations of the frontend/backend split, what HTML, CSS and JavaScript each do, and how Spring Boot serves static pages
A walkthrough of the teammate’s backend pull request (the register and login endpoints) and how a page sends data to them
An explanation of Git branches and pull requests for working alongside the other frontend teammate’s branch
Starter code with inline comments for home.html: a header, a welcome message showing the user’s name, a flexbox navigation bar with styled link buttons, and a “My Tools” hover dropdown
A small change to index.html that saves the user’s name with localStorage after a successful login and redirects to home.html
Help running the backend locally (port 8081, setting SUPABASE_DB_PASSWORD before .\run.cmd) and placing the pages in src/webapp/ instead of the static folder

Validation:

Manual: read through the code line by line to understand it before using it
Live run: started the backend locally and tested the full flow in the browser (register, log in, redirect to the homepage, name displayed correctly)
Checked that the changes to index.html didn’t break the login/registration page the other frontend teammate built

Decision:

Modified: I first planned to build the pages without AI using MDN, but switched to reading and adjusting AI-provided code to meet the deadline
Accepted: using localStorage to carry the user’s name from login to the homepage (simple enough for Sprint 1)
Accepted: keeping the homepage in src/webapp/ to match the backend configuration
Flagged to the team: the backend User model has fewer fields than the Supabase tables
Deferred: the recruiter version of the homepage

Reflection: AI was useful for getting up to speed quickly on things I had never touched, especially how the pages, backend and database fit together. I learned more by reading and changing working code than by starting from a blank file. Next sprint I want to write more of the frontend code myself now that I understand the basics.

Responsible Person: Maria-Francesca Staicu (40324837)

Appendix A: Prompt / Response history (Prompts are paraphrased.)

Prompt 1: “I need to build the homepage and login page but have never done frontend and have about 3 days. Where do I start?”

Response 1 (summary): The AI explained the difference between frontend and backend, said browsers only run HTML, CSS and JavaScript, and suggested a 3-day plan using MDN resources.

Prompt 2: “This is the backend pull request another teammate made. What does it do, and should I merge it?”

Response 2 (summary): The AI confirmed the backend uses Spring Boot and explained its register and login endpoints. It described how the login page would send data to them and walked through how pull requests and branches work.

Prompt 3: “Help me build the job seeker homepage. Give me code with comments so I can learn from it.”

Response 3 (summary): The AI provided home.html with a header, a welcome message, a navigation bar and a “My Tools” dropdown, with comments explaining each part. Acceptance: Accepted with adjustments.

Prompt 4: “How do I go from the login page to the homepage and show the user’s name?”

Response 4 (summary): The AI explained localStorage and suggested saving the name after a successful login, then redirecting to home.html. Acceptance: Accepted.

Prompt 5: “The app won’t run / the page isn’t showing up. What’s wrong?”

Response 5 (summary): The AI helped set the database password environment variable and found that the backend runs on port 8081. It also explained that the pages are served from src/webapp/. The full login-to-homepage flow then worked. Acceptance: Accepted.

Prompt 6: “Help me open a pull request for my branch.”

Response 6 (summary): The AI walked through committing, pushing the francesca_homepage_frontend branch and opening the pull request.
