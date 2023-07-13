package id.walt.mdoc.docrequest

import id.walt.mdoc.cose.COSESign1
import id.walt.mdoc.dataelement.BooleanElement
import id.walt.mdoc.dataelement.EncodedCBORElement
import id.walt.mdoc.dataelement.MapKey
import id.walt.mdoc.dataelement.toDE

class MDocRequestBuilder(val docType: String) {
  val nameSpaces = mutableMapOf<String, MutableMap<String, Boolean>>()

  fun addDataElementRequest(nameSpace: String, elementIdentifier: String, intentToRetain: Boolean): MDocRequestBuilder {
    nameSpaces.getOrPut(nameSpace) { mutableMapOf() }.put(elementIdentifier, intentToRetain)
    return this
  }

  fun build(readerAuth: COSESign1? = null) = MDocRequest(
    EncodedCBORElement(ItemsRequest(
      docType = docType.toDE(),
      nameSpaces = nameSpaces.map { ns ->
        Pair(MapKey(ns.key), ns.value.map { item ->
          Pair(MapKey(item.key), BooleanElement(item.value))
        }.toMap().toDE())
      }.toMap().toDE()
    ).toMapElement()),
    readerAuth
  )
}