package id.walt.mdoc.model

import cbor.Cbor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray

@Serializable
data class MDoc(
    val docType: String,
    val issuerSigned: IssuerSigned
)

class MDocBuilder(val docType: String) {
    val nameSpacesMap = mutableMapOf<String, MutableList<EncodedDataElementValue>>()

    @OptIn(ExperimentalSerializationApi::class)
    fun addIssuerSignedItems(nameSpace: String, vararg item: IssuerSignedItem): MDocBuilder {
        nameSpacesMap.getOrPut(nameSpace) { mutableListOf() }.addAll(item.map { EncodedDataElementValue(it.toDataElementValue()) })
        return this
    }

    // add/generate IssuerAuth MSO object

    fun build(): MDoc {
        return MDoc(docType, IssuerSigned(nameSpacesMap.mapValues { it.value.toList() }))
    }
}