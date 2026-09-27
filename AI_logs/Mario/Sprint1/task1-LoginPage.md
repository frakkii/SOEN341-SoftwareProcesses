**Task ID/Title:** Basic login page front-end demo

**Purpose of AI Use:** Code generation and UI/UX guidance  creating a first working front-end demo (login/registration form + mock dashboard) for the Sprint 1 Code Demonstration requirement, and understanding how a static HTML front end connects to a Java backend in IntelliJ.

**Chat Link or Prompt/Response:** *https://share.google/aimode/U5YfeWd48Da8F5htG*. Key prompts included: "could u write me a simple front end code demo for the website... for connectcareer website"; a follow-up asking how Java, Python, and HTML fit together ("i got only python and java code how will it work"); and setup questions about where to create files in IntelliJ.

**AI-Suggested Content:** Gemini generated a single-page `login.html` (HTML/CSS/JS) with a hardcoded sign-in/register form that toggles to a mock dashboard view on submit, plus a matching `MainServer.java` using Java's built-in `HttpServer` to serve that one file on port 8080. It also explained the browser/Java/Python architecture and walked through IntelliJ project setup (creating a `webapp` folder, marking `src` as sources root, creating the Java class).

**Validation:** Ran the generated files locally via IntelliJ's Run button and loaded `http://localhost:8080` in a browser to confirm the login form displayed and the dashboard view swapped in correctly on submit.

**Decision:** Modified before use. Kept the overall structure (form fields, role selector, show/hide dashboard pattern) as a starting skeleton, but the visual styling was plain and a leftover placeholder rule (rejecting the username "Jack", copied from an unrelated ELEC 366 lab concept) was removed in Task 2.

**Reflection:** Useful for quickly getting something clickable to test the IntelliJ/Java server setup, and for understanding how the front end and backend talk to each other. The generated code itself was very basic  flat single-color styling, no input validation feedback, and a stray piece of unrelated logic that needed to be caught and removed rather than accepted as is.

**Person:** Mario
 
