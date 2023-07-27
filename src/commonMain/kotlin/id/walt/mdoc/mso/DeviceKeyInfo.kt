package id.walt.mdoc.mso

import id.walt.mdoc.dataelement.MapElement
import id.walt.mdoc.dataelement.toDE
import kotlinx.serialization.Serializable

@Serializable
class DeviceKeyInfo (
  val deviceKey: MapElement,
  val keyAuthorizations: MapElement? = null,
  val keyInfo: MapElement? = null
) {
  /**
   * Convert to CBOR map element
   */
  fun toMapElement() = buildMap {
    put("deviceKey", deviceKey)
    keyAuthorizations?.let { put("keyAuthorizations", it) }
    keyInfo?.let { put("keyInfo", it) }
  }.toDE()
}