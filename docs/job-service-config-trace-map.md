# Job Service Configuration Trace Map

> This map traces checked-in configuration sources for `job-service`; it does not represent effective live configuration. Secret values are omitted.

## Configuration trace map

| `application.yml` | `Default` | `application-local.yml` | `census31-fwmt-ci-terraform/k8s/job-service/job-service-deployment.yaml` | `census31-fwmt-terraform/secrets.tf` | `census31-fwmt-terraform/secret-manager.tf` |
|---|---|---|---|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/postgres?currentSchema=fwmtg` | — | `DB_NAME <- db-config[db-name]`; `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/$(DB_NAME)?currentSchema=fwmtg` | `db-config[db-name]=var.db_name` | — |
| `spring.datasource.username` | `postgres` | — | `SPRING_DATASOURCE_USERNAME <- db-credentials[username]` | `db-credentials[username]=var.db_user_name` | — |
| `spring.datasource.password` | `[MASKED]` | — | `SPRING_DATASOURCE_PASSWORD <- db-credentials[password]` | `db-credentials[password]=random_password.cloudsqlUser.result` | — |
| `spring.liquibase.url` | `jdbc:postgresql://localhost:5432/postgres?currentSchema=fwmtg` | — | `DB_NAME <- db-config[db-name]`; `SPRING_LIQUIBASE_URL=jdbc:postgresql://localhost:5432/$(DB_NAME)?currentSchema=fwmtg` | `db-config[db-name]=var.db_name` | — |
| `spring.liquibase.user` | `postgres` | — | `SPRING_LIQUIBASE_USER=$(SPRING_DATASOURCE_USERNAME)` | `db-credentials[username]=var.db_user_name` | — |
| `spring.liquibase.password` | `[MASKED]` | — | `SPRING_LIQUIBASE_PASSWORD=$(SPRING_DATASOURCE_PASSWORD)` | `db-credentials[password]=random_password.cloudsqlUser.result` | — |
| `spring.cloud.gcp.project-id` | `fwmt-local` | — | `SPRING_CLOUD_GCP_PROJECT_ID <- project-config[project-id]` | `project-config[project-id]=var.project_id` | — |
| `spring.cloud.gcp.credentials.enabled` | `false` | — | `SPRING_CLOUD_GCP_CREDENTIALS_ENABLED=true` | — | — |
| `spring.cloud.gcp.pubsub.emulator-host` | — | `spring.cloud.gcp.pubsub.emulator-host=localhost:8085` | — | — | — |
| `management.endpoint.env.enabled` | `${APP_TESTING:false}` | `management.endpoint.env.enabled=true` | `APP_TESTING=true` | — | — |
| `management.endpoint.env.post.enabled` | `${APP_TESTING:false}` | `management.endpoint.env.post.enabled=true` | `APP_TESTING=true` | — | — |
| `management.endpoint.refresh.enabled` | `${APP_TESTING:false}` | `management.endpoint.refresh.enabled=true` | `APP_TESTING=true` | — | — |
| `totalmobile.username` | `user` | — | `TOTALMOBILE_USERNAME <- tm-config[username]` | `tm-config[username]=lookup(local.tm_config_secret_payload, "tm_username", "")` | `secret-manager[tm-config]` |
| `totalmobile.password` | `[MASKED]` | — | `TOTALMOBILE_PASSWORD <- tm-config[password]` | `tm-config[password]=lookup(local.tm_config_secret_payload, "tm_password", "")` | `secret-manager[tm-config]` |
| `totalmobile.baseUrl` | `http://localhost:8000/` | — | `TOTALMOBILE_BASEURL <- tm-config[url]` | `tm-config[url]=lookup(local.tm_config_secret_payload, "tm_url", "")` | `secret-manager[tm-config]` |
| `totalmobile.comet.clientID` | `""` | — | `TOTALMOBILE_COMET_CLIENTID <- tm-config[clientid]` | `tm-config[clientid]=lookup(local.tm_config_secret_payload, "tm_clientid", "")` | `secret-manager[tm-config]` |
| `totalmobile.comet.clientSecret` | `[MASKED]` | — | `TOTALMOBILE_COMET_CLIENTSECRET <- tm-config[clientsecret]` | `tm-config[clientsecret]=lookup(local.tm_config_secret_payload, "tm_clientsecret", "")` | `secret-manager[tm-config]` |
| `decryption.pgp` | `[MASKED]` | — | `DECRYPTION_PGP <- hardrefusal-config[pgp-fwmt-private-key-location]` | `hardrefusal-config[pgp-fwmt-private-key-location]=var.pgp_fwmt_private_key_location` | — |
| `decryption.password` | `${DECRYPTION_PASSWORD:}` | — | `DECRYPTION_PASSWORD <- hardrefusal-config[pgp-key-passphrase]` | `hardrefusal-config[pgp-key-passphrase]=lookup(local.hard_refusal_secret_payload, "pgp_key_passphrase", "")` | `secret-manager[hard-refusal-config]` |
| `app.messaging.pubsub.rm-project-id` | `${RM_PUBSUB_PROJECT:}` | — | `RM_PUBSUB_PROJECT <- project-config[rm-pubsub-project-id]` | `project-config[rm-pubsub-project-id]=var.rm_pubsub_project_id` | — |

## Verification items

- The binding of `TOTALMOBILE_COMET_CLIENTID` and `TOTALMOBILE_COMET_CLIENTSECRET` to mixed-case `totalmobile.comet.clientID` and `totalmobile.comet.clientSecret` has not been verified; compare the application property binding with `census31-fwmt-ci-terraform/k8s/job-service/job-service-deployment.yaml`.
- `decryption.pgp` has a packaged private-key location fallback, masked here. The source of the separate private-key Secret mounted by `census31-fwmt-ci-terraform/k8s/job-service/job-service-deployment.yaml` is not identified in `census31-fwmt-terraform/secrets.tf`.

## Omitted non-property configuration

- Probes, resource requests/limits, proxy sidecar, and service account: `census31-fwmt-ci-terraform/k8s/job-service/job-service-deployment.yaml`.