# Security Policy

## Supported versions

Security fixes are applied to the latest maintained version of GetMP3.

| Version | Supported |
|---|---|
| 1.x | Yes |
| Older versions | No |

## Reporting a vulnerability

Do not report security vulnerabilities through a public issue.

Use GitHub private vulnerability reporting from the repository Security page.

Include:

- A clear description of the issue.
- Affected version.
- Android version.
- Device model.
- Reproduction steps.
- Security impact.
- Relevant logs with private information removed.
- A proposed fix when available.

## Security issues include

- Access to files outside user-selected folders.
- Path traversal.
- Unsafe URI handling.
- Arbitrary command execution.
- Malicious intent handling.
- Unauthorized file deletion.
- Private data exposure.
- Download URL leakage.
- Dependency compromise.
- Unsafe native binary behavior.
- Permission abuse.

## Response process

The maintainer will:

1. Confirm receipt.
2. Attempt to reproduce the issue.
3. Evaluate severity.
4. Prepare a fix.
5. Coordinate disclosure when appropriate.
6. Publish a security update.

## Out of scope

The following are generally not security vulnerabilities:

- Unsupported source URLs.
- Source-platform behavior changes.
- Incorrect search results.
- Metadata parsing mistakes without security impact.
- Problems caused by modified unofficial builds.