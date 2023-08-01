package id.walt.mdoc.docrequest

import id.walt.mdoc.cose.COSESign1
import id.walt.mdoc.dataelement.BooleanElement
import id.walt.mdoc.dataelement.EncodedCBORElement
import id.walt.mdoc.dataelement.MapKey
import id.walt.mdoc.dataelement.toDE

/**
 * MDoc request builder
 * @param docType doc type of requested document
 */
class MDocRequestBuilder(val docType: String) {
  val nameSpaces = mutableMapOf<String, MutableMap<String, Boolean>>()

  /**
   * Add request for issuer signed data element
   * @param nameSpace Name space of the data element
   * @param elementIdentifier Element identifier
   * @param intentToRetain  Whether the reader intends to retain (store) the element data in a long term storage
   * @return this builder object
   */
  fun addDataElementRequest(nameSpace: String, elementIdentifier: String, intentToRetain: Boolean): MDocRequestBuilder {
    nameSpaces.getOrPut(nameSpace) { mutableMapOf() }.put(elementIdentifier, intentToRetain)
    return this
  }

  /**
   * Build mdoc request object
   * @param reader authentication COSE Sign1 structure, if required
   * @return the mdoc request object
   */
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