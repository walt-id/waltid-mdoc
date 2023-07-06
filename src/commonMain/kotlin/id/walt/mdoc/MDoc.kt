package id.walt.mdoc

import id.walt.mdoc.cose.AsyncCOSECryptoProvider
import id.walt.mdoc.cose.COSECryptoProvider
import id.walt.mdoc.dataelement.AnyDataElement
import id.walt.mdoc.dataelement.EncodedCBORElement
import id.walt.mdoc.dataelement.StringElement
import id.walt.mdoc.devicesigned.DeviceSigned
import id.walt.mdoc.cose.COSESign1
import id.walt.mdoc.issuersigned.IssuerSigned
import id.walt.mdoc.issuersigned.IssuerSignedItem
import id.walt.mdoc.mso.DeviceKeyInfo
import id.walt.mdoc.mso.MSO
import id.walt.mdoc.mso.ValidityInfo
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class MDoc(
    val docType: StringElement,
    val issuerSigned: IssuerSigned,
    val deviceSigned: DeviceSigned
) {
    var _mso: MSO? = null
    val MSO
        get() = _mso ?: issuerSigned.issuerAuth.payload?.let { data ->
            EncodedCBORElement.fromEncodedCBORElementData(data).decode<MSO>()
        }.also { _mso = it }

    val nameSpaces
        get() = issuerSigned.nameSpaces?.keys ?: setOf()

    fun getIssuerSignedItems(nameSpace: String): List<IssuerSignedItem> {
        return issuerSigned.nameSpaces?.get(nameSpace)?.map {
            it.decode<IssuerSignedItem>()
        }?.toList() ?: listOf()
    }

    fun verifyIssuerSignedItems(): Boolean {
        val mso = MSO ?: throw Exception("No MSO object found on this mdoc")

        return issuerSigned.nameSpaces?.all { nameSpace ->
            mso.verifySignedItems(nameSpace.key, nameSpace.value)
        } ?: true                                                       // 3.
    }

    fun verifyCertificate(cryptoProvider: COSECryptoProvider, keyID: String? = null): Boolean {
        return cryptoProvider.verifyX5Chain(issuerSigned.issuerAuth, keyID)
    }

    fun verifyValidity(): Boolean {
        val mso = MSO ?: throw Exception("No MSO object found on this mdoc")
        return mso.validityInfo.validFrom.value <= Clock.System.now()      && // 5.2
        mso.validityInfo.validUntil.value >= Clock.System.now()               // 5.3
    }

    fun verifyDocType(): Boolean {
        val mso = MSO ?: throw Exception("No MSO object found on this mdoc")
        return mso.docType == docType                                         // 4.
    }

    fun verifySignature(cryptoProvider: COSECryptoProvider, keyID: String? = null): Boolean {
        return cryptoProvider.verify1(issuerSigned.issuerAuth, keyID)
    }

    fun verify(cryptoProvider: COSECryptoProvider, keyID: String? = null): Boolean {
        // check points 1-5 of ISO 18013-5: 9.3.1
        return  verifyValidity() && verifyDocType()      &&  // 4.,5.
                verifyCertificate(cryptoProvider, keyID) &&  // 1.
                verifyIssuerSignedItems()                &&  // 3.
                verifySignature(cryptoProvider, keyID)       // 2.
    }
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
                items.maxOfOrNull { it.digestID.value.toLong().toUInt() }?.plus(1u) ?: 0u,
                elementIdentifier,
                elementValue
            )
        )
        return this
    }

    // TODO: add/generate IssuerAuth MSO object
    // TODO: add/generate DeviceSigned, DeviceAuth

    fun build(deviceSigned: DeviceSigned, issuerAuth: COSESign1): MDoc {
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
        val issuerAuth = cryptoProvider.sign1(mso.toMapElement().toEncodedCBORElement().toCBOR(), keyID)
        return build(deviceSigned, issuerAuth)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun sign(cryptoProvider: COSECryptoProvider, validityInfo: ValidityInfo,
             deviceKeyInfo: DeviceKeyInfo, deviceSigned: DeviceSigned,
             keyID: String? = null): MDoc {
        val mso = MSO.createFor(nameSpacesMap, deviceKeyInfo, docType, validityInfo)
        val issuerAuth = cryptoProvider.sign1(mso.toMapElement().toEncodedCBORElement().toCBOR(), keyID)
        return build(deviceSigned, issuerAuth)
    }
}