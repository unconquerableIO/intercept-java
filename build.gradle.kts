plugins {
    base
}

allprojects {
    group   = property("projectGroup").toString()
    version = property("projectVersion").toString()
}