// ============================================
// Plugins
// ============================================
plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.allopen") version "2.2.20"
    id("io.quarkus")
    id("org.openapi.generator") version "7.16.0"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("nu.studer.jooq") version "10.1.1"
}

// ============================================
// Project Properties
// ============================================
group = "com.example"
version = "1.0.0"

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

// ============================================
// Repositories
// ============================================
repositories {
    mavenCentral()
}

// ============================================
// Dependencies
// ============================================
dependencies {
    // Quarkus BOM
    implementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))

    // Quarkus extensions (versions managed by BOM)
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-swagger-ui")
    implementation("io.quarkus:quarkus-smallrye-jwt")
    implementation("io.quarkus:quarkus-smallrye-jwt-build")
    implementation("io.quarkus:quarkus-arc")

    // jOOQ integration
    implementation("io.quarkiverse.jooq:quarkus-jooq:2.1.0")

    // External dependencies
    implementation("de.mkammerer:argon2-jvm:2.12")
    implementation("io.swagger:swagger-annotations:1.6.15")

    // Required for OpenAPI generated code (not provided by Quarkus BOM for standalone generated classes)
    implementation("jakarta.validation:jakarta.validation-api:3.1.0")

    // jOOQ code generation
    jooqGenerator("org.jooq:jooq-meta-extensions:3.20.8")

    // Test dependencies (versions managed by BOM)
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.testcontainers:postgresql:1.21.3")
    testImplementation("org.testcontainers:testcontainers:1.21.3")
}

// ============================================
// Java/Kotlin Configuration
// ============================================
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        javaParameters = true
    }
}

// Kotlin all-open plugin for Quarkus
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

// ============================================
// Source Sets
// ============================================
sourceSets {
    main {
        java {
            srcDir("${layout.buildDirectory.get()}/generated/openapi/src/gen/java")
        }
    }
}

// ============================================
// Plugin Configurations
// ============================================

// OpenAPI Generator
openApiGenerate {
    generatorName = "jaxrs-spec"
    inputSpec = "$rootDir/src/main/resources/openapi.yaml"
    outputDir = layout.buildDirectory.dir("generated/openapi").get().asFile.path
    apiPackage = "com.example.api"
    modelPackage = "com.example.api.model"
    invokerPackage = "com.example.api.invoker"
    validateSpec = true
    configOptions = mapOf(
        "dateLibrary" to "java8",
        "interfaceOnly" to "true",
        "returnResponse" to "true",
        "useTags" to "true",
        "useJakartaEe" to "true",
    )
}

// jOOQ code generation
jooq {
    version = "3.20.8"
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation = false
            jooqConfiguration.apply {
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                        properties.add(
                            org.jooq.meta.jaxb.Property().apply {
                                key = "scripts"
                                value = "db/migrations/*.sql"
                            },
                        )
                        properties.add(
                            org.jooq.meta.jaxb.Property().apply {
                                key = "sort"
                                value = "semantic"
                            },
                        )
                        properties.add(
                            org.jooq.meta.jaxb.Property().apply {
                                key = "defaultNameCase"
                                value = "lower"
                            },
                        )
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.example.jooq"
                        directory = layout.buildDirectory.dir("generated/jooq").get().asFile.path
                    }
                }
            }
        }
    }
}

// ktlint
ktlint {
    version = "1.5.0"
    android = false
    ignoreFailures = false
}

// ============================================
// Task Configuration
// ============================================

// Quarkus test logging configuration (required for @QuarkusTest)
tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

// Ensure code generation runs before compilation
// Required because:
// - OpenAPI Generator plugin doesn't auto-wire dependencies
// - jOOQ plugin auto-wiring is disabled (generateSchemaSourceOnCompilation = false)
tasks.named("compileKotlin") {
    dependsOn("openApiGenerate", "generateJooq")
}

// Ensure ktlint runs after code generation and only checks source code
tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask> {
    mustRunAfter("generateJooq", "openApiGenerate")
    setSource(fileTree("src/main/kotlin"))
}
