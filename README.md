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

#### Create, parse and verify an mDL request

```kotlin
val cryptoProvider = SimpleCOSECryptoProvider(listOf(
  COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, readerKeyPair.public, readerKeyPair.private)
))
val sessionTranscript = ListElement(/*... create session transcript according to ISO/IEC FDIS 18013-5, section 9.1.5.1 ...*/)

val docReq = MDocRequestBuilder("org.iso.18013.5.1.mDL")
  .addDataElementRequest("org.iso.18013.5.1", "family_name", true)
  .addDataElementRequest("org.iso.18013.5.1", "birth_date", false)
  .sign(sessionTranscript, cryptoProvider, READER_KEY_ID)

val deviceRequest = DeviceRequest(listOf(docReq))
var devReqCbor = deviceRequest.toCBORHex()
println("DEVICE REQUEST: $devReqCbor")

val parsedReq = DeviceRequest.fromCBORHex(devReqCbor)
val firstParsedDocRequest = parsedReq.docRequests.first()
val reqVerified = firstParsedDocRequest.verify(
  MDocRequestVerificationParams(
    requiresReaderAuth = true,
    READER_KEY_ID,
    allowedToRetain = mapOf("org.iso.18013.5.1" to setOf("family_name")),
    ReaderAuthentication(sessionTranscript, firstParsedDocRequest.itemsRequest)
  ), cryptoProvider
)
println("Request verified: $reqVerified")
println("Requested doc type: ${firstParsedDocRequest.docType}")
println("Requested items:")
firstParsedDocRequest.nameSpaces.forEach { ns ->
  println("- NameSpace: $ns")
  firstParsedDocRequest.getRequestedItemsFor(ns).forEach {
    println("-- ${it.key} (intent-to-retain: ${it.value})")
  }
}
```
_Example output_:
```text
DEVICE REQUEST: a26776657273696f6e63312e306b646f63526571756573747381a26c6974656d7352657175657374d8185857a267646f6354797065756f72672e69736f2e31383031332e352e312e6d444c6a6e616d65537061636573a1716f72672e69736f2e31383031332e352e31a26b66616d696c795f6e616d65f56a62697274685f64617465f46a726561646572417574688443a10126a11821f6f65840d52b28bbd50252ea93181d9bbcb5b01cbeb11ae442a05bf839dcc3fb9dc6cb92c6fc5eaed6b430ee19a111a1678f2ea959cd8232c6c9828101016caffd3de771
Request verified: true
Requested doc type: org.iso.18013.5.1.mDL
Requested items:
- NameSpace: org.iso.18013.5.1
-- family_name (intent-to-retain: true)
-- birth_date (intent-to-retain: false)
```

* Parse mDL doc (device) response
* Verify MSO and device auth
* List mdoc properties and values
* 



## License

Licensed under the [Apache License, Version 2.0](https://github.com/walt-id/waltid-xyzkit/blob/master/LICENSE)
