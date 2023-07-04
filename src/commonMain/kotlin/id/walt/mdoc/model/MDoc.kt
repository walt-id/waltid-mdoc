package id.walt.mdoc.model

import cbor.Cbor
import id.walt.mdoc.model.dataelement.*
import id.walt.mdoc.model.mso.MSO
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

@Serializable
data class MDoc(
    val docType: StringElement,
    val issuerSigned: IssuerSigned,
    val deviceSigned: DeviceSigned
) {
    fun getIssuerSignedItems(nameSpace: String): List<IssuerSignedItem> {
        return issuerSigned.nameSpaces?.get(nameSpace)?.map {
            it.decode<IssuerSignedItem>()
        }?.toList() ?: listOf()
    }

    val MSO
        get() = issuerSigned.issuerAuth.getMSO()

    val nameSpaces
        get() = issuerSigned.nameSpaces?.keys ?: setOf()
}

class MDocBuilder(val docType: String) {
    val nameSpacesMap = mutableMapOf<String, MutableList<EncodedCBORElement>>()

    fun addIssuerSignedItems(nameSpace: String, vararg item: IssuerSignedItem): MDocBuilder {
        nameSpacesMap.getOrPut(nameSpace) { mutableListOf() }.addAll(item.map { EncodedCBORElement(Cbor.encodeToByteArray(it)) })
        return this
    }

    // TODO: add/generate IssuerAuth MSO object
    // TODO: add/generate DeviceSigned, DeviceAuth

    fun build(): MDoc {
        return MDoc(
            StringElement(docType),
            IssuerSigned(nameSpacesMap.mapValues { it.value.toList() }, IssuerAuth(ListElement())),
            DeviceSigned(
                EncodedCBORElement(MapElement(mapOf())),
                DeviceAuth(deviceMac = ListElement(listOf()))
            )
        )
    }
}