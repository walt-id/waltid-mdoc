package id.walt.mdoc.model.mso

import id.walt.mdoc.model.dataelement.MapElement
import kotlinx.serialization.Serializable

@Serializable
class DeviceKeyInfo (
  val deviceKey: MapElement,
  val keyAuthorizations: MapElement? = null,
  val keyInfo: MapElement? = null
)