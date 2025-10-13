# RealWorld API - Technical Design

## System Overview

A [RealWorld API](https://realworld-docs.netlify.app/) backend implementing a Medium.com clone with articles, comments, user profiles, and social features.

**Architecture Pattern:** DDD-lite with CQRS
**Primary Goal:** Type-safe, performant, maintainable API
**Deployment Target:** GraalVM native image for production

## Technology Stack

### Core Framework
- **Quarkus 3.28.x** - Supersonic Java framework
  - GraalVM native image support (primary target)
  - Fast startup (<100ms), low memory (<50MB)
  - Built-in DI, REST, security
  - JVM mode for development, native for production

### Language & Build
- **Kotlin 2.2.x** - Concise, null-safe JVM language
- **Gradle (Kotlin DSL)** - Build automation
- **Java 21** - LTS runtime
- **GraalVM 21** - Native compilation target

### Database Layer
- **PostgreSQL 15** - Production database
- **jOOQ 3.19.x** - Type-safe SQL DSL (NOT JPA/Hibernate)
  - Compile-time query validation
  - Full SQL feature access
  - Kotlin code generation
- **Atlas CLI** - Schema migrations
  - HCL-based declarative schema
  - Automatic migration generation
  - Version control for schema

### API & Security
- **OpenAPI 3.0** - Contract-first API design
  - OpenAPI Generator 7.x (jaxrs-spec)
  - Generate JAX-RS interfaces
- **SmallRye JWT** - JWT authentication
  - RS256 algorithm (RSA with SHA-256)
  - 60-day token expiry
  - Private key for signing, public key for verification
- **Argon2** - Password hashing (Argon2id variant)

### Development
- **Docker Compose** - Local PostgreSQL
- **Swagger UI** - Interactive API docs
- **ktlint** - Code formatting
- **Testcontainers** - Integration tests

## Architecture

### DDD-Lite Pattern

**Feature-Based Packages** (one per aggregate):
```
com.example/
├── user/           # User aggregate
├── profile/        # Follow aggregate
├── article/        # Article aggregate (tags, favorites)
├── comment/        # Comment aggregate
├── query/          # Read model (CQRS queries)
└── shared/         # Cross-cutting (security, utils)
```

### CQRS-Lite Pattern

**Write Side (Commands)**
- Commands defined in `*Service.kt`
- Domain logic enforced
- Transactional operations

**Read Side (Queries)**
- Centralized in `QueryService.kt`
- Optimized for reads
- Direct jOOQ queries
- NO N+1 queries (use multiset)

### Layer Responsibilities

```
Resource (API)
    ↓ calls
Service (Commands)
    ↓ uses
Repository (Persistence)
    ↓ uses
jOOQ DSL (SQL)

QueryService (Queries)
    ↓ uses
jOOQ DSL (SQL)
```

## Architectural Patterns

### Aggregate Design

**Concept:**
- Aggregate = transactional consistency boundary
- Cluster of domain objects treated as single unit
- One repository per aggregate root
- Changes to aggregate are atomic

**RealWorld Aggregates:**

1. **User Aggregate**
   - Root: User entity
   - Boundary: user data only
   - Repository: UserRepository

2. **Article Aggregate**
   - Root: Article entity
   - Boundary: article + tags + favorites
   - Repository: ArticleRepository (manages all)
   - Tags are value objects within aggregate
   - Favorites are part of article aggregate

3. **Comment Aggregate**
   - Root: Comment entity
   - Boundary: comment data
   - Repository: CommentRepository

4. **Follow Aggregate**
   - Root: Follow relationship (no entity, just association)
   - Boundary: follower_id + followee_id pairs
   - Repository: FollowRepository

**NOT Separate Aggregates:**
- Tags (value objects in Article)
- Favorites (managed by Article)
- No TagRepository
- No FavoriteRepository

**Why This Design:**
- Article + tags change together (consistency)
- Favoriting is an operation on Article aggregate
- Comments are separate (can exist/change independently)
- Follows are separate relationships

### Repository Pattern

**Purpose:**
- Encapsulate persistence logic
- Provide collection-like interface for aggregates
- Hide database implementation details

**Structure:**
- Interface defines contract: `ArticleRepository`
- Implementation uses jOOQ: `JooqArticleRepository`
- One repository per aggregate root

**Method Signatures:**
- Accept/return full aggregates
- Example: `save(article: Article): Article`
- Example: `findBySlug(slug: String): Article?`
- Example: `delete(article: Article)`

**Responsibilities:**
- Persist entire aggregate atomically
- Load aggregate with all child entities
- Example: ArticleRepository loads article + tags + favorites

### Domain Entities

**Rich Domain Model:**
- Entities contain behavior, not just data
- Business logic lives in domain layer
- Validation happens during construction

**Entity Structure:**
- Private constructors for invariant protection
- Factory methods in companion object
- Behavior methods (updateTitle, publish, archive)
- Authorization checks in domain methods

**Example Entity:**
- Article entity has `updateTitle(newTitle: String, requesterId: Long)`
- Method validates requesterId matches authorId
- Method generates new slug from title
- Throws ForbiddenException if unauthorized

**Why Rich Model:**
- Business rules in one place
- Easier to test (no mocking needed)
- Prevents invalid state
- Self-documenting

### CQRS Separation

**Commands (Write Side):**
- Operations that change state
- Defined in `*Service.kt` files
- Examples: CreateArticle, UpdateArticle, DeleteArticle
- Use repositories to persist changes
- Wrapped in @Transactional

**Queries (Read Side):**
- Operations that read state
- Centralized in `QueryService.kt`
- Return DTOs optimized for presentation
- Use jOOQ with optimized queries (multiset, joins)
- No transactions needed

**Why Separate:**
- Different optimization strategies
- Commands enforce business rules
- Queries optimized for performance
- Easier to scale independently

### Transaction Boundaries

**Location:**
- Place @Transactional on service methods
- NOT on repository methods
- NOT on resource methods

**Scope:**
- One business operation = one transaction
- Example: Create article + save tags + return result
- Keep transactions short and focused

**Read-Only Operations:**
- QueryService methods don't need @Transactional
- Direct jOOQ queries without transaction overhead
- Use connection pooling for concurrency

**Why Service Layer:**
- Service orchestrates multiple repository calls
- Ensures consistency across aggregate operations
- Rollback on any failure in business operation

### Error Handling Strategy

**Domain Exceptions:**
- NotFoundException - resource doesn't exist (404)
- ValidationException - invalid input (422)
- ForbiddenException - insufficient permissions (403)
- UnauthorizedException - not authenticated (401)

**Exception Flow:**
1. Domain/Service throws domain exception
2. ExceptionMapper catches and maps to HTTP response
3. Returns structured error JSON

**Validation:**
- Business rule validation in domain entities
- Input validation in service layer
- ValidationException includes field-level errors
- Format: `{ "errors": { "email": ["is already taken"] } }`

### DTO Mapping Strategy

**Location:**
- DTOs defined in query/ package
- Mapping happens at Resource (API boundary) layer
- Never expose domain entities via API

**DTOs:**
- ProfileDto - user profile view
- ArticleDto - article with author and metadata
- CommentDto - comment with author

**Why DTOs:**
- Decouple API from domain model
- API stability (domain can change)
- Different representations for different contexts
- Query optimization (fetch exactly what API needs)

## Data Model

### Core Entities

**users**
- id (PK, bigserial)
- email (unique, indexed)
- username (unique, indexed)
- password_hash
- bio, image (nullable)
- created_at, updated_at

**followers**
- follower_id (FK → users.id)
- followee_id (FK → users.id)
- PRIMARY KEY (follower_id, followee_id)

**articles**
- id (PK, bigserial)
- slug (unique, indexed)
- title, description, body
- author_id (FK → users.id)
- created_at, updated_at

**tags**
- id (PK, bigserial)
- name (unique, indexed)

**article_tags**
- article_id (FK → articles.id)
- tag_id (FK → tags.id)
- PRIMARY KEY (article_id, tag_id)

**favorites**
- user_id (FK → users.id)
- article_id (FK → articles.id)
- PRIMARY KEY (user_id, article_id)

**comments**
- id (PK, bigserial)
- body
- article_id (FK → articles.id)
- author_id (FK → users.id)
- created_at, updated_at

### Indexes (Critical for Performance)

**Unique Indexes (Already in Schema)**
- users.email
- users.username
- articles.slug

**Performance Indexes (To Add)**
- articles.author_id
- articles.created_at (DESC for ordering)
- comments.article_id
- favorites.article_id
- article_tags.article_id
- article_tags.tag_id
- followers.followee_id

## Package Structure

### user/ (User Aggregate)
```
user/
├── User.kt                 # Domain entity with behavior
├── UserRepository.kt       # Interface
├── JooqUserRepository.kt   # jOOQ implementation
├── UserService.kt          # Commands: Register, Login, Update
└── UserResource.kt         # API implementation
```

**Commands:**
- `RegisterUser(email, username, password)` → UserWithToken
- `LoginUser(email, password)` → UserWithToken
- `UpdateUser(userId, email?, username?, password?, bio?, image?)` → User

### profile/ (Follow Aggregate)
```
profile/
├── FollowRepository.kt         # Interface
├── JooqFollowRepository.kt     # Implementation
├── ProfileService.kt           # Commands
└── ProfileResource.kt          # API
```

**Aggregate Boundary:**
- Follow relationships are separate aggregate (follower_id, followee_id pairs)
- No entity class needed (simple relationship)
- Repository manages follow/unfollow operations

**Commands:**
- `FollowUser(followerId, followeeUsername)` → ProfileDto
- `UnfollowUser(followerId, followeeUsername)` → ProfileDto

### article/ (Article Aggregate)
```
article/
├── Article.kt                  # Aggregate root (includes tags)
├── ArticleRepository.kt        # Interface
├── JooqArticleRepository.kt    # Implementation (manages article + tags + favorites)
├── ArticleService.kt           # Commands
├── ArticleResource.kt          # API
└── TagResource.kt              # API for tags list
```

**Aggregate Boundary:**
- Article aggregate includes: article data, tags list, favorites
- Repository handles all persistence within aggregate
- No separate TagRepository or FavoriteRepository

**Commands:**
- `CreateArticle(title, description, body, tagList, authorId)` → Article
- `UpdateArticle(slug, title?, description?, body?, requesterId)` → Article
- `DeleteArticle(slug, requesterId)` → Unit
- `FavoriteArticle(slug, userId)` → Unit
- `UnfavoriteArticle(slug, userId)` → Unit

### comment/ (Comment Aggregate)
```
comment/
├── Comment.kt
├── CommentRepository.kt
├── JooqCommentRepository.kt
├── CommentService.kt
└── (comments in ArticleResource)
```

**Commands:**
- `CreateComment(slug, body, authorId)` → Comment
- `DeleteComment(slug, commentId, requesterId)` → Unit

### query/ (Read Model)
```
query/
└── QueryService.kt
```

**DTOs:**
- ProfileDto: username, bio, image, following
- ArticleDto: slug, title, description, body, tagList, createdAt, updatedAt, favorited, favoritesCount, author
- CommentDto: id, body, createdAt, updatedAt, author

**Query Methods:**
- getProfile(username, currentUserId?) → ProfileDto
- getArticle(slug, currentUserId?) → ArticleDto
- getArticles(tag?, author?, favorited?, limit, offset, currentUserId?) → Pair<List<ArticleDto>, Int>
- getArticlesFeed(currentUserId, limit, offset) → Pair<List<ArticleDto>, Int>
- getArticleComments(slug, currentUserId?) → List<CommentDto>
- getAllTags() → List<String>

### shared/ (Cross-cutting)
```
shared/
├── security/
│   ├── JwtService.kt              # Token generation
│   ├── SecurityContext.kt         # Current user extraction
│   ├── PasswordHasher.kt          # Argon2 wrapper
│   └── UnauthorizedException.kt
├── exceptions/
│   ├── Exceptions.kt              # Domain exceptions
│   └── ExceptionMappers.kt        # JAX-RS mappers
└── utils/
    └── SlugUtils.kt               # URL slug generation
```

## Query Optimization

### Avoid N+1 Queries with Multiset

**Pattern for fetching articles with nested data:**

Use jOOQ's multiset() to fetch all related data in a single query:
- Main article data (ARTICLES, USERS join)
- Tags list (multiset from TAGS + ARTICLE_TAGS)
- Favorite status for current user (multiset from FAVORITES)
- Favorites count (subquery with count)
- Following status for article author (multiset from FOLLOWERS)

**Result:** 1 query instead of 1 + (N * 4) queries

**Key Principles:**
- All nested collections fetched via multiset()
- All aggregations (counts) via subqueries
- Single join to ARTICLES and USERS
- No queries inside map() loops

## Security Design

### Authentication Flow

**Registration/Login:**
1. User submits credentials to UserService
2. PasswordHasher creates Argon2id hash
3. UserRepository stores user with hash
4. JwtService generates JWT token (RS256, 60-day expiry)
5. Return UserWithToken response

**Protected Endpoints:**
1. Request includes "Authorization: Token <jwt>" header
2. SmallRye JWT validates signature automatically
3. SecurityContext extracts userId from JWT subject
4. Service uses userId for business logic and authorization

### Authorization Patterns

**Read Operations (Optional Auth):**
- SecurityContext.currentUserId returns nullable Long
- Different responses based on authentication status
- Example: showing "favorited" field only for authenticated users

**Write Operations (Required Auth):**
- SecurityContext.requireUserId() throws if not authenticated
- Returns 401 Unauthorized if token missing or invalid
- Always requires valid user identity

**Domain-Level Authorization:**
- Authorization checks in Service layer, not Resource
- Check ownership before mutations (article author, comment author)
- Throw ForbiddenException (403) for insufficient permissions
- Example: Only article author can delete article

## API Design

### OpenAPI-First Workflow

1. Define endpoint in src/main/resources/openapi.yaml
2. Run gradle generateApi task
3. Implement generated JAX-RS interface in Resource class
4. Generated models use fluent setters (chain method calls)
5. Return generated response wrapper objects

### Error Response Format

**Validation Error (422):**
- Field-level errors with descriptive messages
- Format: errors object with field names as keys

**Not Found (404):**
- Generic error message
- Consistent structure across all 404s

**Unauthorized (401):**
- Authentication required message
- Returned when JWT token missing or invalid

**Forbidden (403):**
- Insufficient permissions message
- Returned when authenticated but not authorized for action

## Database Migration Strategy

### Atlas HCL Schema (Source of Truth)

**File:** db/schema.hcl

**Structure:**
- Define schema "public"
- Table definitions with columns, types, constraints
- Primary keys and foreign keys
- Indexes (unique and performance)
- Use PostgreSQL-specific types (bigserial, varchar, etc.)

### Migration Workflow

1. Edit HCL schema file (db/schema.hcl)
2. Generate migration: atlas migrate diff <description> --env local
3. Review generated SQL in db/migrations/
4. Apply to database: atlas migrate apply --env local
5. Regenerate jOOQ code: gradle generateJooq

### Environment Configuration

**File:** atlas.hcl

**Environments:**
- local: Development database at localhost:5432
- test: Test database at localhost:5433
- Both use HCL schema as source (file://db/schema.hcl)
- Migrations stored in file://db/migrations
- Dev environment uses docker://postgres/15/dev

## Testing Strategy

### Test Pyramid

**Unit Tests (Many)**
- Domain logic in entities
- Utility functions (slug generation, password hashing)
- DTO mappings
- No external dependencies
- Fast execution

**Integration Tests (Some)**
- Repository tests with Testcontainers (real PostgreSQL)
- Service tests with mocked repositories
- API tests with full Quarkus stack
- Verify database constraints and transactions

**E2E Tests (Few)**
- Critical user flows only
- Authentication and authorization flow
- Article CRUD with nested resources
- Slower but high confidence

### Testcontainers Setup

- Use @QuarkusTest for integration tests
- Use @QuarkusTestResource with PostgresResource
- Spin up real PostgreSQL container per test class
- Test against actual database constraints
- Verify transactional behavior
- Test unique constraint violations, cascade deletes, etc.

## Performance Targets

### Response Time (95th percentile)
- GET /api/articles: < 50ms
- GET /api/articles/feed: < 100ms
- POST /api/articles: < 100ms
- POST /api/users: < 200ms (due to Argon2)

### Database
- Connection pool: 10-50 connections (Agroal)
- Query timeout: 5 seconds
- All queries use prepared statements (jOOQ default)

### Optimization Checklist
- [ ] All foreign keys indexed
- [ ] Pagination on list endpoints (limit/offset)
- [ ] No N+1 queries (use multiset)
- [ ] Response caching headers on GET endpoints
- [ ] Database query logging in dev (disabled in prod)

## Deployment

### Development (JVM Mode)
- Build: `gradle build`
- Run: JAR from build/quarkus-app/
- Fast iteration, hot reload with quarkusDev
- Easier debugging

### Production (GraalVM Native Image)
**Primary deployment target for production**

- Build: `gradle build -Dquarkus.package.type=native`
- Startup: <100ms (vs ~2-3s JVM)
- Memory: <50MB RSS (vs ~200MB JVM)
- Container: ~50MB image (vs ~200MB JVM)
- No JVM overhead, no JIT warmup

**Native Build Options:**
```bash
# Local build (requires GraalVM installed)
gradle build -Dquarkus.package.type=native

# Container build (no local GraalVM needed)
gradle build \
  -Dquarkus.package.type=native \
  -Dquarkus.native.container-build=true
```

### Docker
- Use Dockerfile.native for multi-stage build
- Base image: distroless or alpine (minimal)
- Final image size: ~50MB
- Run on port 8080

### Environment Variables
- QUARKUS_DATASOURCE_JDBC_URL: Database connection string
- QUARKUS_DATASOURCE_USERNAME/PASSWORD: Database credentials
- MP_JWT_VERIFY_ISSUER: JWT issuer URL
- MP_JWT_VERIFY_PUBLICKEY_LOCATION: Path to JWT public key
- QUARKUS_LOG_LEVEL: Logging level (INFO for production)

## Implementation Order

### Phase 1: Foundation
1. Setup Gradle with plugins (Quarkus, OpenAPI, jOOQ, ktlint)
2. Create Atlas HCL schema (db/schema.hcl)
3. Setup Docker Compose (PostgreSQL)
4. Apply schema & generate jOOQ code
5. Configure JWT keys & SmallRye JWT
6. Create OpenAPI spec for all endpoints
7. Generate API interfaces

### Phase 2: Shared Infrastructure
1. JwtService (token generation)
2. SecurityContext (current user)
3. PasswordHasher (Argon2)
4. Exception classes & mappers
5. SlugUtils

### Phase 3: User & Auth
1. User entity with behavior
2. UserRepository interface & implementation
3. UserService (register, login, update commands)
4. UserResource (API implementation)

### Phase 4: Profile
1. FollowRepository interface & implementation
2. ProfileService (follow, unfollow commands)
3. ProfileResource

### Phase 5: Article
1. Article entity with behavior (aggregate root includes tags)
2. ArticleRepository interface & implementation (manages article + tags + favorites)
3. ArticleService (all commands)
4. ArticleResource
5. TagResource

### Phase 6: Comment
1. Comment entity
2. CommentRepository interface & implementation
3. CommentService (create, delete commands)
4. Add comment endpoints to ArticleResource

### Phase 7: Query Layer
1. Create DTOs (ProfileDto, ArticleDto, CommentDto)
2. QueryService with optimized multiset queries
3. Replace direct repository calls in Resources with QueryService

### Phase 8: Quality & Testing
1. Add ktlint configuration
2. Write integration tests for repositories
3. Write API tests for critical flows
4. Performance testing with k6/Gatling
5. Add database indexes for performance

## Success Criteria

- [ ] All 20+ RealWorld API endpoints implemented
- [ ] No N+1 queries in any endpoint
- [ ] All tests passing
- [ ] ktlint passing
- [ ] Build successful (JVM mode)
- [ ] **Native build successful (GraalVM)**
- [ ] **Native image starts in <100ms**
- [ ] **Native image uses <50MB memory**
- [ ] All performance targets met
- [ ] Comprehensive documentation
- [ ] Security review passed
- [ ] Ready for production deployment
