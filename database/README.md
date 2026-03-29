# Database

This directory contains the local MariaDB setup and Flyway-based database migrations.

## How It Works

- `mariadb` provides the database service.
- `flyway` runs against the same database and converts every file in `init/*.sql` into a Flyway repeatable migration named `R__<original-file-name>.sql`.
- When the content of a SQL file changes, Flyway detects the checksum change and re-executes that repeatable migration on the next `migrate`.

Because these are repeatable migrations, SQL files in `init/` should stay idempotent.

## Common Commands

Run all commands from the `database/` directory.

Start database and run migrations:

```bash
docker compose up
```

Start only MariaDB in background:

```bash
docker compose up -d mariadb
```

Run Flyway migrations once:

```bash
docker compose run --rm flyway migrate
```

Show Flyway migration status:

```bash
docker compose run --rm flyway info
```

Validate migration files against history:

```bash
docker compose run --rm flyway validate
```

Repair failed migration history entries:

```bash
docker compose run --rm flyway repair
```

Stop services:

```bash
docker compose down
```

Stop services and remove database volume:

```bash
docker compose down -v
```

## Failed Migration Recovery

If `flyway migrate` reports a failed migration, fix the SQL first, then run:

```bash
docker compose run --rm flyway repair
docker compose run --rm flyway migrate
```

This is required even if you already executed the SQL manually, because Flyway still keeps the failed state in `flyway_schema_history`.

## Repeatable Migration Notes

- Adding a new file in `init/` creates a new repeatable migration.
- Editing an existing file in `init/` changes its checksum and causes Flyway to re-run it on the next `migrate`.
- Flyway does not blindly re-run all repeatable migrations every time. It only re-runs new or changed ones.

## Check Migration History

Inspect the Flyway history table:

```sql
SELECT installed_rank, version, description, type, script, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

## Force a Specific Repeatable Migration to Run Again

Option 1: modify the target SQL file so its checksum changes, then run:

```bash
docker compose run --rm flyway migrate
```

Option 2: remove the target repeatable migration record from `flyway_schema_history`, then run `migrate` again.

## Full Reset

If you want a completely fresh local database:

```bash
docker compose down -v
docker compose up
```

Warning: this deletes all local MariaDB data stored in the Docker volume.
