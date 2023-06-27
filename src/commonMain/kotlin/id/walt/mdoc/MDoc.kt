package id.walt.mdoc

import cbor.Cbor
import id.walt.mdoc.model.Document
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToHexString

@Serializable
class MDoc(
    val version: String,
    val documents: List<Document>
)