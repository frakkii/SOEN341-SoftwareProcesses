Task 1 – Backend Login and Registration
Task ID/Title: Backend Login and Registration
Purpose of AI Use:
AI was used as a support tool while developing the Spring Boot login and registration backend. It was mainly used to explain Spring Boot concepts, suggest a simple structure for the authentication flow, and help troubleshoot setup issues.
Chat Link or Prompt/Response:
See Appendix A.
AI-Suggested Content:
- Explained how @RestController, @RequestMapping, and @PostMapping are used
- Suggested creating separate /register and /login endpoints
- Suggested using a simple in-memory user store for the Sprint 1 prototype
- Explained how duplicate email checking should work
- Explained how successful and failed login attempts should be handled
- Suggested using BCrypt instead of storing passwords in plain text
Validation:
I reviewed the controller logic manually, ran the Spring Boot application, and tested the authentication endpoints myself. I tested successful registration, successful login, duplicate-user handling, and login with an incorrect password.
Decision:
Modified before use. I used the AI suggestions as guidance and adapted them to the structure of the project. I manually tested the final implementation before pushing it to GitHub.
Reflection:
AI was helpful for understanding the basic Spring Boot authentication flow and troubleshooting setup problems. Testing the backend myself helped me better understand how the registration and login process works.