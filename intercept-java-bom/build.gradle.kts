plugins {
    id("unconquerable.base-conventions")
    `java-platform`
}

dependencies {
    constraints {
        // Use the project's own version for internal modules
        api(project(":intercept-java-core"))

        // Third party versions
        api("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    }
}