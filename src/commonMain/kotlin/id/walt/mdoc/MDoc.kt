package id.walt.mdoc

import cbor.Cbor
import id.walt.mdoc.model.Document
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToHexString

@Serializable
class MDoc(
    val version: String = "1.0",
    val documents: List<Document>
) {
    @OptIn(ExperimentalSerializationApi::class)
    fun toCBOR(): ByteArray {
        return Cbor.encodeToByteArray(this)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun toCBORHex(): String {
        return Cbor.encodeToHexString(this)
    }

    companion object
}
