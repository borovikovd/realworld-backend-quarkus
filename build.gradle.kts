plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.allopen") version "2.2.20"
    id("io.quarkus")
    id("org.openapi.generator") version "7.16.0"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("nu.studer.jooq") version "9.0"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-swagger-ui")
    implementation("io.quarkus:quarkus-smallrye-jwt")
    implementation("io.quarkus:quarkus-smallrye-jwt-build")
    implementation("io.quarkus:quarkus-agroal")
    implementation("io.quarkiverse.jooq:quarkus-jooq:2.1.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    implementation("de.mkammerer:argon2-jvm:2.12")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:4.0.0")
    implementation("io.swagger:swagger-annotations:1.6.15")
    implementation("jakarta.validation:jakarta.validation-api:3.1.0")

    jooqGenerator("org.postgresql:postgresql:42.7.4")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.testcontainers:postgresql:1.21.3")
    testImplementation("org.testcontainers:testcontainers:1.21.3")
}

group = "com.example"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        javaParameters = true
    }
    sourceSets["main"].kotlin.srcDirs("build/generated/openapi/src/main/kotlin")
}

openApiGenerate {
    generatorName.set("jaxrs-spec")
    inputSpec.set("$rootDir/src/main/resources/openapi.yaml")
    outputDir.set("$buildDir/generated/openapi")
    apiPackage.set("com.example.api")
    modelPackage.set("com.example.api.model")
    invokerPackage.set("com.example.api.invoker")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "interfaceOnly" to "true",
            "returnResponse" to "true",
            "useTags" to "true",
            "generateBuilders" to "true",
            "useJakartaEe" to "true",
        ),
    )
}

tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
}

ktlint {
    version.set("1.5.0")
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    enableExperimentalRules.set(false)
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask> {
    mustRunAfter("generateJooq", "openApiGenerate")
    setSource(fileTree("src/main/kotlin"))
}

jooq {
    version.set("3.19.15")
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/realworld"
                    user = "realworld"
                    password = "realworld"
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.example.jooq"
                        directory = "build/generated/jooq"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDir("build/generated/openapi/src/gen/java")
        }
    }
}

tasks.named("compileKotlin") {
    dependsOn("generateJooq")
}
