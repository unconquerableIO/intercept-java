plugins {
    id("unconquerable.java-conventions")
}

dependencies {

    implementation(platform(project(":intercept-java-bom")))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    
}