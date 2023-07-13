package id.walt.mdoc.docrequest

import id.walt.mdoc.cose.COSESign1
import id.walt.mdoc.dataelement.*
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
    return (decodedItemsRequest.nameSpaces.value[MapKey(nameSpace)] as MapElement).value.map {
      Pair(it.key.str, (it.value as BooleanElement).value)
    }.toMap()
  }

  fun toMapElement() = buildMap {
    put(MapKey("itemsRequest"), itemsRequest)
    readerAuth?.let {
      put(MapKey("readerAuth"), it.toDE())
    }
  }.toDE()
}