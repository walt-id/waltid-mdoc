package id.walt.mdoc

import COSE.AlgorithmID
import COSE.OneKey
import cbor.Cbor
import id.walt.mdoc.dataelement.*
import id.walt.mdoc.devicesigned.DeviceAuth
import id.walt.mdoc.devicesigned.DeviceSigned
import id.walt.mdoc.mso.DeviceKeyInfo
import id.walt.mdoc.mso.ValidityInfo
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToHexString
import kotlin.test.Test

class JVMMdocTest {

  @OptIn(ExperimentalSerializationApi::class)
  @Test
  fun testSigning() {
    val algorithmID = AlgorithmID.ECDSA_256
    val key = OneKey.generateKey(algorithmID)
    val cryptoProvider = SimpleCOSECryptoProvider(algorithmID, key)

    val mdoc = MDocBuilder("org.iso.18013.5.1.mDL")
      .addItemToSign("org.iso.18013.5.1", "family_name", "Doe".toDE())
      .sign(cryptoProvider,
        ValidityInfo(Clock.System.now(), Clock.System.now(), Clock.System.now().plus(365*24, DateTimeUnit.HOUR)),
        DeviceKeyInfo(MapElement(mapOf())),
        DeviceSigned(EncodedCBORElement(MapElement(mapOf())), DeviceAuth(ListElement())),
      )
    println("SIGNED MDOC:")
    println(Cbor.encodeToHexString(mdoc))

    mdoc.MSO shouldNotBe null
    mdoc.MSO!!.digestAlgorithm.value shouldBe "SHA-256"
    val signedItems = mdoc.getIssuerSignedItems("org.iso.18013.5.1")
    signedItems shouldHaveSize 1
    signedItems.first().digestID.value shouldBe 0
    mdoc.MSO!!.valueDigests.value shouldContainKey MapKey("org.iso.18013.5.1")
    mdoc.verify(cryptoProvider) shouldBe true
  }
}