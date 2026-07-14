---
name: NewJavaFile
description: Create a new Java file (class or interface) in this project with the correct package declaration
invocation: user
---

Create a new Java source file under `src/main/java`.

Rules:
- The file must always start with a `package` declaration. Default to the project's base package,
  `com.example.demo_chat`, unless the user names a specific subpackage (e.g. "in the service package"
  means `com.example.demo_chat.service`).
- Save the file at the path matching its package, e.g. `com.example.demo_chat.service` →
  `src/main/java/com/example/demo_chat/service/<Name>.java`. Create intermediate directories if they
  don't exist yet.
- If the user asks to create a **class**, generate a `public class <Name>` with the name they provided.
- If the user asks to create an **interface**, generate a `public interface <Name>` with the name they
  provided.
- The file name must match the type name exactly (Java requirement for public top-level types).
- Leave the body empty (just the package declaration and the empty type declaration) unless the user
  specifies fields, methods, or other members.
