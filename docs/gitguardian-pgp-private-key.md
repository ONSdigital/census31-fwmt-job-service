# GitGuardian: PGP private key alert

## Alert

GitGuardian reported **PGP Private Key Exposed** on this repository. The scanner matched
`-----BEGIN PGP PRIVATE KEY BLOCK-----` in committed files (typically
`testPrivateKey.private` under `src/main/resources` and/or `src/test/resources`).

GitGuardian does not distinguish “test” keys from production keys: any valid OpenPGP secret
key in git history is treated as a secret leak.

## What the keys were used for

The removed files were a **test keypair** used for local development and unit tests around
**NC (non-compliance) hard-refusal contact decryption**:

- **`GpgConfig`** loads `${decryption.pgp}` into a `privateKeyByteArray` Spring bean.
- **`NamedHouseholderRetrieval`** uses that key (via **`DecryptNames`** / Bouncy Castle) to
  decrypt PGP-encrypted `title`, `forename`, and `surname` on RM hard-refusal events before
  building the “named householder” contact string for field workflows.

The matching public key (`testPublicKey.public`) remains in `src/main/resources` for
reference and for encrypt-side tests elsewhere in the FWMT stack. Production deployments
must supply the real decryption key via configuration (URI to secure storage), not from the
repo.

The former default passphrase in `application.yml` was `testJobService` (test-only).

## Action taken

| Item | Change |
|------|--------|
| `src/main/resources/testPrivateKey.private` | **Removed** |
| `src/test/resources/testPrivateKey.private` | **Removed** |
| `application.yml` | Default `decryption.pgp` no longer points at a classpath private key; use env/config (see below) |
| `NcNamedHouseholderRetrievalTest` | No longer loads a key file (test uses empty contact fields, so decryption is not invoked) |
| `testPublicKey.public` | **Kept** (public material only) |

## How to fix / restore local behaviour

### Run the service locally

1. Obtain the correct **decryption private key** for your environment (from your team’s
   secret store — not from git).
2. Place it on disk, e.g. `~/.fwmt/keys/decryption.private`, or set an explicit URI.
3. Configure Spring (env vars or `application-local.yml`):

   ```yaml
   decryption:
     pgp: "file://${user.home}/.fwmt/keys/decryption.private"
     password: "<passphrase>"
   ```

   Or:

   ```bash
   export DECRYPTION_PGP="file:///path/to/decryption.private"
   export DECRYPTION_PASSWORD="<passphrase>"
   ```

   `GpgConfig` also supports non-classpath URIs via `StorageUtils` (e.g. `gs://`, `s3://`).

### Unit / integration tests that need decryption

1. Generate a **new** test keypair (do not reuse the removed commit keys):

   ```bash
   gpg --full-generate-key
   gpg --armor --export-secret-keys -o decryption.private <key-id>
   gpg --armor --export -o testPublicKey.public <key-id>
   ```

2. Keep the private key **outside the repo** (local path or CI secret).
3. Point tests at the file with `ReflectionTestUtils` or a test `@TestPropertySource`, or
   use encrypted fixtures produced with the matching public key.

### Clear the GitGuardian alert on GitHub

1. Merge the commit that removes the keys from the default branch.
2. **Resolve** the finding in GitGuardian.
3. If your security policy requires it, purge the keys from **git history** (e.g.
   `git filter-repo`) — removing files from HEAD does not remove them from old commits.

## Related services

- **census31-fwmt-outcome-service** encrypts refusal contact names with public keys; see its
  `docs/gitguardian-pgp-private-key.md` for the paired cleanup.
