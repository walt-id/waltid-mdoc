package id.walt.mdoc

import cbor.Cbor
import id.walt.mdoc.dataelement.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromHexString

@Serializable
class MDocResponse(
    val version: StringElement,
    val documents: List<MDoc>,
    val status: NumberElement = MDocResponseStatus.OK.status.toDE()
) {
    fun toMapElement() = MapElement(
        buildMap {
            put(MapKey("version"), version)
            put(MapKey("documents"), documents.map { it.toMapElement() }.toDE())
            put(MapKey("status"), status)
        }
    )

    fun toCBOR() = toMapElement().toCBOR()
    fun toCBORHex() = toMapElement().toCBORHex()

    companion object {
        fun fromCBOR(cbor: ByteArray) = Cbor.decodeFromByteArray<MDocResponse>(cbor)
        fun fromCBORHex(cbor: String) = Cbor.decodeFromHexString<MDocResponse>(cbor)
    }
}