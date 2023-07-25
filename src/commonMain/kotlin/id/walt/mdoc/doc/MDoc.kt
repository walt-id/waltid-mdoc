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

    fun verifyDeviceSignature(deviceAuthentication: DeviceAuthentication, cryptoProvider: COSECryptoProvider, keyID: String?): Boolean {
        val deviceSignature = deviceSigned?.deviceAuth?.deviceSignature ?: throw Exception("No device signature found on MDoc")
        return cryptoProvider.verify1(
            deviceSignature.attachPayload(getDeviceSignedPayload(deviceAuthentication)),
            keyID
        )
    }

    fun verifyDeviceMAC(deviceAuthentication: DeviceAuthentication, ephemeralMACKey: ByteArray): Boolean {
        val deviceMac = deviceSigned?.deviceAuth?.deviceMac ?: throw Exception("No device MAC found on MDoc")
        return deviceMac.attachPayload(getDeviceSignedPayload(deviceAuthentication)).verify(ephemeralMACKey)
    }

    private fun verifyDeviceSigOrMac(verificationParams: MDocVerificationParams, cryptoProvider: COSECryptoProvider): Boolean {
        val mdocDeviceAuth = deviceSigned?.deviceAuth ?: throw Exception("MDoc has no device authentication")
        val deviceAuthenticationPayload = verificationParams.deviceAuthentication ?: throw Exception("No device authentication payload given, for check of device signature or MAC")
        return if(mdocDeviceAuth.deviceMac != null) {
            verifyDeviceMAC(
                deviceAuthenticationPayload,
                verificationParams.ephemeralMacKey ?: throw Exception("No ephemeral MAC key given, for check of device MAC")
            )
        } else if(mdocDeviceAuth.deviceSignature != null) {
            verifyDeviceSignature(
                deviceAuthenticationPayload,
                cryptoProvider, verificationParams.deviceKeyID
            )
        } else throw Exception("MDoc device auth has neither MAC nor signature")
    }

    fun verify(verificationParams: MDocVerificationParams, cryptoProvider: COSECryptoProvider): Boolean {
        // check points 1-5 of ISO 18013-5: 9.3.1
        return VerificationType.all.all { type ->
            !verificationParams.verificationTypes.has(type) || when(type) {
                VerificationType.VALIDITY -> verifyValidity()
                VerificationType.DOC_TYPE -> verifyDocType()
                VerificationType.CERTIFICATE_CHAIN -> verifyCertificate(cryptoProvider, verificationParams.issuerKeyID)
                VerificationType.ITEMS_TAMPER_CHECK -> verifyIssuerSignedItems()
                VerificationType.ISSUER_SIGNATURE -> verifySignature(cryptoProvider, verificationParams.issuerKeyID)
                VerificationType.DEVICE_SIGNATURE -> verifyDeviceSigOrMac(verificationParams, cryptoProvider)
            }
        }
    }

    private fun selectDisclosures(mDocRequest: MDocRequest): IssuerSigned {
        return IssuerSigned(
            issuerSigned.nameSpaces?.mapValues { entry ->
                val requestedItems = mDocRequest.getRequestedItemsFor(entry.key)
                entry.value.filter { encodedItem ->
                    requestedItems.containsKey(encodedItem.decode<IssuerSignedItem>().elementIdentifier.value)
                }
            },
            issuerSigned.issuerAuth
        )
    }

    fun presentWithDeviceSignature(mDocRequest: MDocRequest, deviceAuthentication: DeviceAuthentication, cryptoProvider: COSECryptoProvider, keyID: String? = null): MDoc {
        TODO()
    }

    private fun getDeviceSignedPayload(deviceAuthentication: DeviceAuthentication) = EncodedCBORElement(deviceAuthentication.toDE()).toCBOR()

    fun presentWithDeviceMAC(mDocRequest: MDocRequest, deviceAuthentication: DeviceAuthentication, ephemeralMACKey: ByteArray): MDoc {
        val coseMac0 = COSEMac0.createWithHMAC256(getDeviceSignedPayload(deviceAuthentication), ephemeralMACKey).detachPayload()
        return MDoc(
            docType,
            selectDisclosures(mDocRequest),
            DeviceSigned(EncodedCBORElement(MapElement(mapOf())), DeviceAuth(coseMac0)))
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