package id.walt.mdoc.model

import cbor.Cbor
import id.walt.mdoc.model.dataelement.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

@Serializable
data class MDoc(
    val docType: String,
    val issuerSigned: IssuerSigned,
    val deviceSigned: DeviceSigned
) {
    fun getIssuerSignedItems(nameSpace: String): List<IssuerSignedItem> {
        return issuerSigned.nameSpaces?.get(nameSpace)?.map {
            Cbor.decodeFromByteArray<IssuerSignedItem>(it.value)
        }?.toList() ?: listOf()
    }
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
            docType,
            IssuerSigned(nameSpacesMap.mapValues { it.value.toList() }, IssuerAuth(listOf())),
            DeviceSigned(
                EncodedCBORElement(MapElement(mapOf())),
                DeviceAuth(deviceMac = ListElement(listOf()))
            )
        )
    }
}