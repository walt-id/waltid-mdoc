package id.walt.mdoc

import cbor.Cbor
import id.walt.mdoc.cose.AsyncCOSECryptoProvider
import id.walt.mdoc.cose.COSECryptoProvider
import id.walt.mdoc.dataelement.*
import id.walt.mdoc.devicesigned.DeviceSigned
import id.walt.mdoc.issuersigned.IssuerAuth
import id.walt.mdoc.issuersigned.IssuerSigned
import id.walt.mdoc.issuersigned.IssuerSignedItem
import id.walt.mdoc.mso.DeviceKeyInfo
import id.walt.mdoc.mso.MSO
import id.walt.mdoc.mso.ValidityInfo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

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
    val nameSpacesMap = mutableMapOf<String, MutableList<IssuerSignedItem>>()

    fun addIssuerSignedItems(nameSpace: String, vararg item: IssuerSignedItem): MDocBuilder {
        nameSpacesMap.getOrPut(nameSpace) { mutableListOf() }.addAll(item)
        return this
    }

    fun addItemToSign(nameSpace: String, elementIdentifier: String, elementValue: AnyDataElement): MDocBuilder {
        val items = nameSpacesMap.getOrPut(nameSpace) { mutableListOf() }
        items.add(
            IssuerSignedItem.createWithRandomSalt(
                (items.maxOfOrNull { it.digestID.value.toLong().toUInt() } ?: 0u) + 1u,
                elementIdentifier,
                elementValue
            )
        )
        return this
    }

    // TODO: add/generate IssuerAuth MSO object
    // TODO: add/generate DeviceSigned, DeviceAuth

    fun build(deviceSigned: DeviceSigned, issuerAuth: IssuerAuth): MDoc {
        return MDoc(
            StringElement(docType),
            IssuerSigned(nameSpacesMap.mapValues { it.value.map { item -> EncodedCBORElement(item.toMapElement()) } }, issuerAuth),
            deviceSigned
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun signAsync(cryptoProvider: AsyncCOSECryptoProvider, validityInfo: ValidityInfo,
                          deviceKeyInfo: DeviceKeyInfo, deviceSigned: DeviceSigned,
                          keyID: String? = null): MDoc {
        val mso = MSO.createFor(nameSpacesMap, deviceKeyInfo, docType, validityInfo)
        val cose_sign1 = cryptoProvider.sign1(Cbor.encodeToByteArray(DataElementSerializer, EncodedCBORElement(mso.toMapElement())), keyID)
        return build(deviceSigned, IssuerAuth(cose_sign1))
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun sign(cryptoProvider: COSECryptoProvider, validityInfo: ValidityInfo,
             deviceKeyInfo: DeviceKeyInfo, deviceSigned: DeviceSigned,
             keyID: String? = null): MDoc {
        val mso = MSO.createFor(nameSpacesMap, deviceKeyInfo, docType, validityInfo)
        val cose_sign1 = cryptoProvider.sign1(Cbor.encodeToByteArray(DataElementSerializer, EncodedCBORElement(mso.toMapElement())), keyID)
        return build(deviceSigned, IssuerAuth(cose_sign1))
    }
}