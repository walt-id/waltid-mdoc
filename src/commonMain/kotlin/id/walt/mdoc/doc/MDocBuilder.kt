package id.walt.mdoc.doc

import id.walt.mdoc.cose.AsyncCOSECryptoProvider
import id.walt.mdoc.cose.COSECryptoProvider
import id.walt.mdoc.cose.COSESign1
import id.walt.mdoc.dataelement.AnyDataElement
import id.walt.mdoc.dataelement.EncodedCBORElement
import id.walt.mdoc.dataelement.StringElement
import id.walt.mdoc.devicesigned.DeviceSigned
import id.walt.mdoc.issuersigned.IssuerSigned
import id.walt.mdoc.issuersigned.IssuerSignedItem
import id.walt.mdoc.mso.DeviceKeyInfo
import id.walt.mdoc.mso.MSO
import id.walt.mdoc.mso.ValidityInfo
import kotlinx.serialization.ExperimentalSerializationApi

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

  // TODO: add/generate DeviceSigned, DeviceAuth

  fun build(issuerAuth: COSESign1?, deviceSigned: DeviceSigned? = null): MDoc {
    return MDoc(
      StringElement(docType),
      IssuerSigned(nameSpacesMap.mapValues { it.value.map { item -> EncodedCBORElement(item.toMapElement()) } }, issuerAuth),
      deviceSigned
    )
  }

  @OptIn(ExperimentalSerializationApi::class)
  suspend fun signAsync(cryptoProvider: AsyncCOSECryptoProvider, validityInfo: ValidityInfo,
                        deviceKeyInfo: DeviceKeyInfo, keyID: String? = null): MDoc {
    val mso = MSO.createFor(nameSpacesMap, deviceKeyInfo, docType, validityInfo)
    val issuerAuth = cryptoProvider.sign1(mso.toMapElement().toEncodedCBORElement().toCBOR(), keyID)
    return build(issuerAuth)
  }

  @OptIn(ExperimentalSerializationApi::class)
  fun sign(cryptoProvider: COSECryptoProvider, validityInfo: ValidityInfo,
           deviceKeyInfo: DeviceKeyInfo, keyID: String? = null): MDoc {
    val mso = MSO.createFor(nameSpacesMap, deviceKeyInfo, docType, validityInfo)
    val issuerAuth = cryptoProvider.sign1(mso.toMapElement().toEncodedCBORElement().toCBOR(), keyID)
    return build(issuerAuth)
  }
}
