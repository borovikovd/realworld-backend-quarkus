-- Create "users" table
CREATE TABLE "public"."users" (
  "id" bigserial NOT NULL,
  "email" character varying(255) NOT NULL,
  "username" character varying(255) NOT NULL,
  "password_hash" character varying(255) NOT NULL,
  "bio" text NULL,
  "image" character varying(512) NULL,
  "created_at" timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  CONSTRAINT "users_email_key" UNIQUE ("email"),
  CONSTRAINT "users_username_key" UNIQUE ("username")
);
-- Create index "idx_users_email" to table: "users"
CREATE INDEX "idx_users_email" ON "public"."users" ("email");
-- Create index "idx_users_username" to table: "users"
CREATE INDEX "idx_users_username" ON "public"."users" ("username");
-- Create "articles" table
CREATE TABLE "public"."articles" (
  "id" bigserial NOT NULL,
  "slug" character varying(255) NOT NULL,
  "title" character varying(255) NOT NULL,
  "description" text NOT NULL,
  "body" text NOT NULL,
  "author_id" bigint NOT NULL,
  "created_at" timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  CONSTRAINT "articles_slug_key" UNIQUE ("slug"),
  CONSTRAINT "articles_author_id_fkey" FOREIGN KEY ("author_id") REFERENCES "public"."users" ("id") ON UPDATE CASCADE ON DELETE CASCADE
);
-- Create index "idx_articles_author" to table: "articles"
CREATE INDEX "idx_articles_author" ON "public"."articles" ("author_id");
-- Create index "idx_articles_created_at" to table: "articles"
CREATE INDEX "idx_articles_created_at" ON "public"."articles" ("created_at" DESC);
-- Create index "idx_articles_slug" to table: "articles"
CREATE INDEX "idx_articles_slug" ON "public"."articles" ("slug");
-- Create "tags" table
CREATE TABLE "public"."tags" (
  "id" bigserial NOT NULL,
  "name" character varying(255) NOT NULL,
  PRIMARY KEY ("id"),
  CONSTRAINT "tags_name_key" UNIQUE ("name")
);
-- Create index "idx_tags_name" to table: "tags"
CREATE INDEX "idx_tags_name" ON "public"."tags" ("name");
-- Create "article_tags" table
CREATE TABLE "public"."article_tags" (
  "article_id" bigint NOT NULL,
  "tag_id" bigint NOT NULL,
  PRIMARY KEY ("article_id", "tag_id"),
  CONSTRAINT "article_tags_article_id_fkey" FOREIGN KEY ("article_id") REFERENCES "public"."articles" ("id") ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT "article_tags_tag_id_fkey" FOREIGN KEY ("tag_id") REFERENCES "public"."tags" ("id") ON UPDATE CASCADE ON DELETE CASCADE
);
-- Create index "idx_article_tags_article_id" to table: "article_tags"
CREATE INDEX "idx_article_tags_article_id" ON "public"."article_tags" ("article_id");
-- Create index "idx_article_tags_tag_id" to table: "article_tags"
CREATE INDEX "idx_article_tags_tag_id" ON "public"."article_tags" ("tag_id");
-- Create "comments" table
CREATE TABLE "public"."comments" (
  "id" bigserial NOT NULL,
  "body" text NOT NULL,
  "article_id" bigint NOT NULL,
  "author_id" bigint NOT NULL,
  "created_at" timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  CONSTRAINT "comments_article_id_fkey" FOREIGN KEY ("article_id") REFERENCES "public"."articles" ("id") ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT "comments_author_id_fkey" FOREIGN KEY ("author_id") REFERENCES "public"."users" ("id") ON UPDATE CASCADE ON DELETE CASCADE
);
-- Create index "idx_comments_article_id" to table: "comments"
CREATE INDEX "idx_comments_article_id" ON "public"."comments" ("article_id");
-- Create index "idx_comments_author_id" to table: "comments"
CREATE INDEX "idx_comments_author_id" ON "public"."comments" ("author_id");
-- Create "favorites" table
CREATE TABLE "public"."favorites" (
  "user_id" bigint NOT NULL,
  "article_id" bigint NOT NULL,
  PRIMARY KEY ("user_id", "article_id"),
  CONSTRAINT "favorites_article_id_fkey" FOREIGN KEY ("article_id") REFERENCES "public"."articles" ("id") ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT "favorites_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users" ("id") ON UPDATE CASCADE ON DELETE CASCADE
);
-- Create index "idx_favorites_article_id" to table: "favorites"
CREATE INDEX "idx_favorites_article_id" ON "public"."favorites" ("article_id");
-- Create "followers" table
CREATE TABLE "public"."followers" (
  "follower_id" bigint NOT NULL,
  "followee_id" bigint NOT NULL,
  PRIMARY KEY ("follower_id", "followee_id"),
  CONSTRAINT "followers_followee_id_fkey" FOREIGN KEY ("followee_id") REFERENCES "public"."users" ("id") ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT "followers_follower_id_fkey" FOREIGN KEY ("follower_id") REFERENCES "public"."users" ("id") ON UPDATE CASCADE ON DELETE CASCADE
);
-- Create index "idx_followers_followee_id" to table: "followers"
CREATE INDEX "idx_followers_followee_id" ON "public"."followers" ("followee_id");
