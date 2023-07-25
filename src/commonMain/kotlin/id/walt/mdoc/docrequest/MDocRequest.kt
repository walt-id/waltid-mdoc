package id.walt.mdoc.docrequest

import id.walt.mdoc.cose.COSECryptoProvider
import id.walt.mdoc.cose.COSESign1
import id.walt.mdoc.dataelement.*
import id.walt.mdoc.mdocauth.DeviceAuthentication
import id.walt.mdoc.readerauth.ReaderAuthentication
import kotlinx.serialization.Serializable

@Serializable
data class MDocRequest internal constructor(
  val itemsRequest: EncodedCBORElement,
  val readerAuth: COSESign1? = null
) {
  private var _decodedItemsRequest: ItemsRequest? = null
  val decodedItemsRequest
    get() = _decodedItemsRequest ?: itemsRequest.decode<ItemsRequest>().also {
      _decodedItemsRequest = it
    }
  val nameSpaces
    get() = decodedItemsRequest.nameSpaces.value.keys.map { it.str }

  val docType
    get() = decodedItemsRequest.docType.value
  fun getRequestedItemsFor(nameSpace: String): Map<String, Boolean> {
    return (decodedItemsRequest.nameSpaces.value[MapKey(nameSpace)] as? MapElement)?.value?.map {
      Pair(it.key.str, (it.value as BooleanElement).value)
    }?.toMap() ?: mapOf()
  }

  private fun getReaderSignedPayload(readerAuthentication: ReaderAuthentication) = EncodedCBORElement(readerAuthentication.toDE()).toCBOR()

  fun verify(verificationParams: MDocRequestVerificationParams, cryptoProvider: COSECryptoProvider): Boolean {
    return (!verificationParams.requiresReaderAuth ||
            readerAuth?.let {
              val readerAuthentication = verificationParams.readerAuthentication?.let { getReaderSignedPayload(it) } ?: throw Exception("No reader authentication payload given")
              cryptoProvider.verify1(it.attachPayload(readerAuthentication), verificationParams.readerKeyId)
            } ?: false) &&
        (verificationParams.allowedToRetain == null || nameSpaces.all { ns ->
          getRequestedItemsFor(ns).all { reqItem ->
            !reqItem.value || // intent to retain is false
            (verificationParams.allowedToRetain[ns]?.contains(reqItem.key) ?: false) }
        })
  }

  fun toMapElement() = buildMap {
    put(MapKey("itemsRequest"), itemsRequest)
    readerAuth?.let {
      put(MapKey("readerAuth"), it.toDE())
    }
  }.toDE()
}