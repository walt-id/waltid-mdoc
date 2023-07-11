package id.walt.mdoc.dataretrieval

import cbor.Cbor
import id.walt.mdoc.dataelement.StringElement
import id.walt.mdoc.dataelement.toDE
import id.walt.mdoc.docrequest.MDocRequest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromHexString

@Serializable
class DeviceRequest(
  val docRequests: List<MDocRequest>,
  val version: StringElement = "1.0".toDE()
) {

  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    fun fromCBOR(cbor: ByteArray) = Cbor.decodeFromByteArray<DeviceRequest>(cbor)
    @OptIn(ExperimentalSerializationApi::class)
    fun fromCBORHex(cbor: String) = Cbor.decodeFromHexString<DeviceRequest>(cbor)
  }
}