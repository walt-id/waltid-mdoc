package id.walt.mdoc.doc

import cbor.Cbor
import id.walt.mdoc.cose.COSECryptoProvider
import id.walt.mdoc.cose.COSEMac0
import id.walt.mdoc.dataelement.EncodedCBORElement
import id.walt.mdoc.dataelement.MapElement
import id.walt.mdoc.dataelement.MapKey
import id.walt.mdoc.dataelement.StringElement
import id.walt.mdoc.devicesigned.DeviceAuth
import id.walt.mdoc.devicesigned.DeviceSigned
import id.walt.mdoc.docrequest.MDocRequest
import id.walt.mdoc.issuersigned.IssuerSigned
import id.walt.mdoc.issuersigned.IssuerSignedItem
import id.walt.mdoc.mdocauth.DeviceAuthentication
import id.walt.mdoc.mdocauth.DeviceAuthenticationMode
import id.walt.mdoc.mso.MSO
import korlibs.crypto.HMAC
import kotlinx.datetime.Clock
import kotlinx.serialization.*

@Serializable(with = MDocSerializer::class)
data class MDoc(
    val docType: StringElement,
    val issuerSigned: IssuerSigned,
    val deviceSigned: DeviceSigned?,
    val errors: MapElement? = null
) {
    var _mso: MSO? = null
    val MSO
        get() = _mso ?: issuerSigned.issuerAuth?.payload?.let { data ->
            EncodedCBORElement.fromEncodedCBORElementData(data).decode<MSO>()
        }?.also { _mso = it }

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
        return issuerSigned.issuerAuth?.let {
            cryptoProvider.verifyX5Chain(it, keyID)
        } ?: throw Exception("No issuer auth found on document")
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
        return issuerSigned.issuerAuth?.let {
            cryptoProvider.verify1(it, keyID)
        } ?: throw Exception("No issuer auth found on document")
    }

    fun verify(cryptoProvider: COSECryptoProvider, keyID: String? = null): Boolean {
        // check points 1-5 of ISO 18013-5: 9.3.1
        return  verifyValidity() && verifyDocType()      &&  // 4.,5.
                verifyCertificate(cryptoProvider, keyID) &&  // 1.
                verifyIssuerSignedItems()                &&  // 3.
                verifySignature(cryptoProvider, keyID)       // 2.
    }

    private fun selectDisclosures(mDocRequest: MDocRequest): IssuerSigned {
        TODO()
    }

    fun presentWithDeviceSignature(cryptoProvider: COSECryptoProvider, mDocRequest: MDocRequest, deviceAuthentication: DeviceAuthentication): MDoc {
        TODO()
    }

    fun presentWithDeviceMAC(mDocRequest: MDocRequest, deviceAuthentication: DeviceAuthentication, ephemeralMACKey: ByteArray): MDoc {
        val coseMac0 = COSEMac0.createForMDocAuth(EncodedCBORElement(deviceAuthentication.toDE()).toCBOR(), ephemeralMACKey).detachPayload()
        // TODO: selective disclosure according to mDocRequest
        val selectiveDisclosures = selectDisclosures(mDocRequest)
        return MDoc(docType, issuerSigned, DeviceSigned(EncodedCBORElement(MapElement(mapOf())), DeviceAuth(coseMac0)))

    }

    fun toMapElement() = MapElement(
        buildMap {
            put(MapKey("docType"), docType)
            put(MapKey("issuerSigned"), issuerSigned.toMapElement())
            deviceSigned?.let {
                put(MapKey("deviceSigned"), it.toMapElement())
            }
            errors?.let {
                put(MapKey("errors"), it)
            }
        }
    )

    fun toCBOR() = toMapElement().toCBOR()
    fun toCBORHex() = toMapElement().toCBORHex()

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun fromCBOR(cbor: ByteArray) = Cbor.decodeFromByteArray<MDoc>(cbor)
        @OptIn(ExperimentalSerializationApi::class)
        fun fromCBORHex(cbor: String) = Cbor.decodeFromHexString<MDoc>(cbor)

        fun fromMapElement(mapElement: MapElement) = MDoc(
            mapElement.value[MapKey("docType")] as? StringElement ?: throw SerializationException("No docType property found on object"),
            (mapElement.value[MapKey("issuerSigned")] as? MapElement)?.let { IssuerSigned.fromMapElement(it) } ?: throw SerializationException("No issuerSigned property found on object"),
            (mapElement.value[MapKey("deviceSigned")] as? MapElement)?.let { DeviceSigned.fromMapElement(it) },
            mapElement.value[MapKey("errors")] as? MapElement
        )
    }
}