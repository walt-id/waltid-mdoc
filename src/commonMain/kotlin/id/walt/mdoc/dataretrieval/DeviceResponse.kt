package id.walt.mdoc.dataretrieval

import cbor.Cbor
import id.walt.mdoc.doc.MDoc
import id.walt.mdoc.dataelement.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromHexString

@Serializable
class DeviceResponse(
    val documents: List<MDoc>,
    val version: StringElement = "1.0".toDE(),
    val status: NumberElement = DeviceResponseStatus.OK.status.toDE(),
    val documentErrors: MapElement? = null
) {
    fun toMapElement() = MapElement(
        buildMap {
            put(MapKey("version"), version)
            put(MapKey("documents"), documents.map { it.toMapElement() }.toDE())
            put(MapKey("status"), status)
            documentErrors?.let {
                put(MapKey("documentErrors"), it)
            }
        }
    )

    fun toCBOR() = toMapElement().toCBOR()
    fun toCBORHex() = toMapElement().toCBORHex()

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun fromCBOR(cbor: ByteArray) = Cbor.decodeFromByteArray<DeviceResponse>(cbor)
        @OptIn(ExperimentalSerializationApi::class)
        fun fromCBORHex(cbor: String) = Cbor.decodeFromHexString<DeviceResponse>(cbor)
    }
}