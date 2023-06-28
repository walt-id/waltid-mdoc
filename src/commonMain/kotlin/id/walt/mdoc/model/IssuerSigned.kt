package id.walt.mdoc.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import cbor.ByteString

@Serializable
data class IssuerSigned(
    val nameSpaces: Map<String, List<EmbeddedCBORDataItem>>?
    // val issuerAuth: IssuerAuth
) {
    companion object {
        class IssuerSignedBuilder {
            val nameSpacesMap = mutableMapOf<String, MutableList<EmbeddedCBORDataItem>>()

            @OptIn(ExperimentalSerializationApi::class)
            fun addIssuerSignedItems(nameSpace: String, vararg item: EmbeddedCBORDataItem): IssuerSignedBuilder {
                nameSpacesMap.getOrPut(nameSpace) { mutableListOf() }.addAll(item)
                return this
            }

            // add/generate IssuerAuth MSO object

            fun build(): IssuerSigned {
                return IssuerSigned(nameSpacesMap.mapValues { it.value.toList() })
            }
        }
    }
}
