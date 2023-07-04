package id.walt.mdoc

import cbor.Cbor
import id.walt.mdoc.dataelement.EncodedCBORElement
import id.walt.mdoc.dataelement.ListElement
import id.walt.mdoc.dataelement.MapElement
import id.walt.mdoc.dataelement.toDE
import id.walt.mdoc.devicesigned.DeviceAuth
import id.walt.mdoc.devicesigned.DeviceSigned
import id.walt.mdoc.mso.DeviceKeyInfo
import id.walt.mdoc.mso.ValidityInfo
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
    val mdoc = MDocBuilder("org.iso.18013.5.1.mDL")
      .addItemToSign("org.iso.18013.5.1", "family_name", "Doe".toDE())
      .sign(SimpleCOSECryptoProvider(),
        ValidityInfo(Clock.System.now(), Clock.System.now(), Clock.System.now().plus(365*24, DateTimeUnit.HOUR)),
        DeviceKeyInfo(MapElement(mapOf())),
        DeviceSigned(EncodedCBORElement(MapElement(mapOf())), DeviceAuth(ListElement())),
      )
    println("SIGNED MDOC:")
    println(Cbor.encodeToHexString(mdoc))
  }
}