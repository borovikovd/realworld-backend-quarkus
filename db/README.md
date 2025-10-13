# Database Migrations with Atlas

This project uses [Atlas](https://atlasgo.io/) for database schema management and migrations.

## Overview

- **Source of Truth**: `db/schema.hcl` (HCL schema definition)
- **Migrations**: `db/migrations/` (versioned SQL files)
- **Configuration**: `atlas.hcl` (Atlas environments)

## Prerequisites

Install Atlas CLI:
```bash
# macOS
brew install ariga/tap/atlas

# Linux
curl -sSf https://atlasgo.sh | sh

# Or visit: https://atlasgo.io/getting-started#installation
```

## Quick Start

### 1. Start PostgreSQL
```bash
docker-compose up -d postgres
```

### 2. Apply Migrations
```bash
# Apply all pending migrations
atlas migrate apply --env local

# Apply specific number of migrations
atlas migrate apply --env local --amount 1
```

### 3. Check Migration Status
```bash
atlas migrate status --env local
```

## Development Workflow

### Making Schema Changes

1. **Edit the HCL schema** (`db/schema.hcl`)
   ```hcl
   table "users" {
     column "new_field" {
       type = varchar(255)
       null = true
     }
   }
   ```

2. **Generate migration**
   ```bash
   atlas migrate diff add_new_field --env local
   ```

3. **Review the generated SQL** in `db/migrations/`

4. **Apply the migration**
   ```bash
   atlas migrate apply --env local
   ```

5. **Regenerate jOOQ code**
   ```bash
   ./gradlew generateJooq
   ```

### Common Commands

```bash
# Generate new migration from schema changes
atlas migrate diff <migration_name> --env local

# Apply all pending migrations
atlas migrate apply --env local

# Check current migration status
atlas migrate status --env local

# Validate migration integrity
atlas migrate validate --env local

# Lint migrations for best practices
atlas migrate lint --env local

# View migration SQL without applying
atlas migrate apply --env local --dry-run
```

## Environments

### Local Development
```bash
atlas migrate apply --env local
```
- Database: `localhost:5432/realworld`
- User: `realworld`
- Password: `realworld`

### Test Environment
```bash
atlas migrate apply --env test
```
- Database: `localhost:5433/realworld_test`
- User: `realworld`
- Password: `realworld`

## Migration Files

### File Naming
Atlas generates migrations with timestamp prefixes:
```
20251013175824_initial.sql
20251013180000_add_user_fields.sql
```

### Checksum File
`atlas.sum` - Contains checksums for all migrations to ensure integrity

### Migration Structure
Each migration contains:
- SQL statements with comments
- Proper formatting and indentation
- All schema changes in correct order

## Best Practices

1. **Never edit applied migrations** - Create new ones instead
2. **Always review generated SQL** before applying
3. **Test migrations on dev environment** before production
4. **Keep schema.hcl as source of truth** - Don't manually edit SQL files
5. **Regenerate jOOQ** after schema changes
6. **Commit migrations to git** along with schema.hcl changes

## Troubleshooting

### Migration Checksum Mismatch
```bash
# Regenerate atlas.sum
atlas migrate hash --env local
```

### Reset Development Database
```bash
# Drop and recreate
docker-compose down -v
docker-compose up -d postgres

# Reapply all migrations
atlas migrate apply --env local
```

### Inspect Current Database Schema
```bash
atlas schema inspect --env local
```

### Compare Schema with Database
```bash
atlas schema diff \
  --from "file://db/schema.hcl" \
  --to "postgres://realworld:realworld@localhost:5432/realworld?sslmode=disable"
```

## Integration with Gradle

After applying migrations, regenerate jOOQ code:
```bash
# Regenerate jOOQ from current database schema
./gradlew generateJooq

# Full regeneration workflow
atlas migrate apply --env local && ./gradlew generateJooq
```

## CI/CD Integration

### GitHub Actions Example
```yaml
- name: Apply Migrations
  run: |
    atlas migrate apply \
      --url "postgres://user:pass@host:5432/db?sslmode=disable" \
      --dir "file://db/migrations"
```

## Additional Resources

- [Atlas Documentation](https://atlasgo.io/docs)
- [Atlas CLI Reference](https://atlasgo.io/cli-reference)
- [Atlas HCL Schema](https://atlasgo.io/atlas-schema/hcl)
- [Migration Best Practices](https://atlasgo.io/guides/migration-best-practices)
