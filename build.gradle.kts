
plugins {
	kotlin("multiplatform") version "1.9.21"
}

group = "org.lf"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

kotlin {
	js {
		moduleName = "app"
		browser {
			webpackTask {
				mainOutputFileName = "app.js"
				output.libraryTarget = "commonjs2"
				cssSupport {
					enabled = true
				}
			}
			distribution {
				outputDirectory = File("$projectDir/docs/")
			}
		}
		binaries.executable()
	}
	sourceSets {
		val jsMain by getting {
			dependencies {
//				implementation("org.lf.github_page:1.0.0")
				implementation(kotlin("stdlib-js"))
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
			}
		}
	}
}

//dependencies {
//	implementation(kotlin("stdlib-js"))
//}