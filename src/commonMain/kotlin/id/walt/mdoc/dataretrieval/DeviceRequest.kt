package id.walt.mdoc.dataretrieval

import cbor.Cbor
import id.walt.mdoc.dataelement.*
import id.walt.mdoc.docrequest.MDocRequest
import kotlinx.serialization.*

@Serializable
class DeviceRequest(
  val docRequests: List<MDocRequest>,
  val version: StringElement = "1.0".toDE()
) {
  fun toMapElement() = mapOf(
    MapKey("version") to version,
    MapKey("docRequests") to ListElement(docRequests.map { it.toMapElement() })
  ).toDE()

  @OptIn(ExperimentalSerializationApi::class)
  fun toCBOR() = toMapElement().toCBOR()
  @OptIn(ExperimentalSerializationApi::class)
  fun toCBORHex() = toMapElement().toCBORHex()
  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    fun fromCBOR(cbor: ByteArray) = Cbor.decodeFromByteArray<DeviceRequest>(cbor)
    @OptIn(ExperimentalSerializationApi::class)
    fun fromCBORHex(cbor: String) = Cbor.decodeFromHexString<DeviceRequest>(cbor)
  }
}