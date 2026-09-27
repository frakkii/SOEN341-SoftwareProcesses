Task 2 – Authentication Validation and Password Handling
Task ID/Title: Authentication Validation and Password Handling
Purpose of AI Use:
AI was used to review the authentication logic and explain how validation, password hashing, and failed login attempts should be handled.
Chat Link or Prompt/Response:
See Appendix A.
AI-Suggested Content:
- Suggested checking that required fields are provided during registration
- Explained how to prevent duplicate email registration
- Explained why incorrect credentials should return 401 Unauthorized
- Suggested returning the same error message for an incorrect email or password
- Suggested using BCrypt to hash passwords before storing them
- Explained how BCrypt compares the entered password with the stored password hash
Validation:
I tested the backend after adding the validation and password-handling changes. I registered a user, logged in with the correct credentials, and tested an incorrect password to confirm that the backend returned a 401 Unauthorized response. I also restarted the application and repeated the tests after BCrypt was added.
Decision:
Modified before use. I kept the validation and password-handling suggestions that were useful for the Sprint 1 backend and tested the final behavior manually.
Reflection:
AI helped explain some basic authentication edge cases and the reason for hashing passwords. Manually testing both successful and unsuccessful login attempts helped confirm that the backend worked correctly.