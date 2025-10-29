SecureVault — Military-Grade Multi-User File Storage (Android)

SecureVault is an Android app that provides private, multi-user file storage with military-grade AES encryption and biometric authentication. Built for professionals (lawyers, doctors, consultants), students, and anyone who needs a simple — yet highly secure — way to store sensitive documents locally on their device.

Key features

AES file encryption: All files are encrypted on disk using AES before being saved — files remain encrypted at rest.

Multi-user isolation: Each user has an isolated environment (separate encrypted storage and metadata).

Username & password auth: Secure login backed by a Room local database. Passwords are hashed (using HashUtils) — never stored or compared in plaintext.

Biometric unlock: Uses androidx.biometric.BiometricPrompt so access requires fingerprint/face (device-managed secure hardware).

Session management: SessionManager handles secure sessions and scoped access.

File management UI: Intuitive interface for creating folders, uploading files, renaming, deleting and organizing.

Previews: Support for multiple file types with in-app preview where feasible (PDF, images, text, common office docs).

Local-only architecture: No cloud backend by default — everything stays on the device to maximize privacy.

Extensible: Modular architecture (Room DAOs, Services, Repository pattern) — easy to extend for backup or sync later.

Why this project

Designed with privacy-first principles: local-only storage, strong encryption, biometric gating.

Practical for legal, medical, consulting workflows and for students protecting academic work.

Minimal permissions and explicit user consent for file access.

Repo contents (high level)

app/ — Android Studio project

MainActivity.java — main entry, biometric flow integration

LoginActivity.java / RegistrationActivity.java — login & registration logic (username/password + hashed verification)

SessionManager — session lifecycle & secure storage hooks

HashUtils — secure password hashing utilities

Room entities/DAOs — User, FileEntry, etc.

encryption/ — AES encryption + key management helpers

ui/ — file browser, previews, upload flows

services/ — file I/O, encryption/decryption, import/export

docs/ — design notes and threat model

LICENSE — license of choice (MIT/Apache recommended)

Quick start (build & run)

Clone the repo.

Open in Android Studio (Arctic Fox or later recommended).

Build and run on a physical device (biometric hardware recommended).

Register a user, log in with username & password, then follow the biometric prompt to access files.

Note: AES keys are stored and protected using Android keystore where possible. Because this is a local app, keep device lock active and secure.

Security notes & best practices

Passwords: hashed with a secure algorithm (salted). Do not store plaintext passwords.

Encryption keys: generated and protected in Android KeyStore / secure hardware when available.

Biometric + password: both are used to strengthen access control — biometric protects the active session.

Data isolation: user files saved to user-scoped encrypted directories; cross-user access is prevented by the app logic.

No remote backups by default: reduces data exposure; add encrypted export/backup only with user consent and strong encryption.

For developers

Project uses MVVM / Repository patterns and Room DB.

Add tests for encryption/decryption and DAO operations.

Consider enabling optional encrypted cloud backups only after threat model review.

Use cases

Lawyers: store contracts, evidence, client documents.

Doctors: keep medical records, prescriptions, images.

Consultants & businesses: sensitive reports and agreements.

Students & general users: personal projects and private documents.

Contributing

Contributions welcome — open issues for bugs, feature requests, or security improvements. Please follow secure coding practices and document any changes to the encryption or authentication flow.
