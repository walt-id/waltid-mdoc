<div align="center">
 <h1>Kotlin Multiplatform MDoc library</h1>
 <span>by </span><a href="https://walt.id">walt.id</a>
 <p>...<p>
 

<a href="https://walt.id/community">
<img src="https://img.shields.io/badge/Join-The Community-blue.svg?style=flat" alt="Join community!" />
</a>
<a href="https://twitter.com/intent/follow?screen_name=walt_id">
<img src="https://img.shields.io/twitter/follow/walt_id.svg?label=Follow%20@walt_id" alt="Follow @walt_id" />
</a>


</div>

## Getting Started

## What is the MDoc library
This library implements the MDoc specification: [ISO/IEC FDIS 18013-5](https://www.iso.org/standard/69084.html), Personal identification -- ISO-compliant driving licence -- Part 5: Mobile driving licence (mDL) application.

### Features
* **Parse and verify** mdocs and mdoc requests, with verification of MSO-validity, doc type, certificate chains, items tamper check, issuer and device signatures.
* **Create and sign** MDoc documents with issuer-signed items and COSE Sign1 issuer authentication (mobile security object, MSO).
* **Present** MDoc documents with selective disclosure of issuer-signed items and MDoc device authentication, based on COSE Mac0 or COSE Sign1.
* **Create** MDoc requests object with COSE Sign1 reader authentication
* Support for **integration** with various crypto libraries and frameworks, to perform the cryptographic operations and key management
* **Multiplatform support**
  * Kotlin/Java for JVM
  * JavaScript
  * Native

## Usage with Maven or Gradle (JVM)

**Maven / Gradle repository**:

`https://maven.walt.id/repository/waltid-ssi-kit/`

**Maven**

```xml
[...]
<repositories>
  <repository>
    <id>waltid-ssikit</id>
    <name>waltid-ssikit</name>
    <url>https://maven.walt.id/repository/waltid-ssi-kit/</url>
  </repository>
</repositories>
[...]
<dependency>
    <groupId>id.walt</groupId>
    <artifactId>waltid-mdoc-jvm</artifactId>
    <version>[ version ]</version>
</dependency>
```

**Gradle**

_Kotlin DSL_
```kotlin
[...]
repositories {
  maven("https://maven.walt.id/repository/waltid-ssi-kit/")
}
[...]
val mdocVersion = "1.xxx.0"
[...]
dependencies {
  implementation("id.walt:waltid-mdoc-jvm:$mdocVersion")
}
```

## Usage with NPM/NodeJs (JavaScript)

**Install NPM package:**

`npm install waltid-mdoc`

**Manual build from source:**

`./gradlew jsNodeProductionLibraryPrepare jsNodeProductionLibraryDistribution`

Then include in your NodeJS project like this:

`npm install /path/to/waltid-mdoc/build/productionLibrary`

**NodeJS example**

Example script in:

`examples/js`

Execute like:

```bash
npm install
node index.js
```

## Examples
### Kotlin / JVM



## License

Licensed under the [Apache License, Version 2.0](https://github.com/walt-id/waltid-xyzkit/blob/master/LICENSE)
