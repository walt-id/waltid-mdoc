package id.walt.mdoc.docrequest

import cbor.Cbor
import id.walt.mdoc.dataelement.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromHexString

@Serializable
data class ItemsRequest internal constructor (
  val docType: StringElement,
  val nameSpaces: MapElement
) {
  fun toMapElement() = buildMap {
    put(MapKey("docType"), docType)
    put(MapKey("nameSpaces"), nameSpaces)
  }.toDE()

}

