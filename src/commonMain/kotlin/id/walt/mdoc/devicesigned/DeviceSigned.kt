package id.walt.mdoc.devicesigned

import id.walt.mdoc.dataelement.EncodedCBORElement
import id.walt.mdoc.dataelement.MapElement
import id.walt.mdoc.dataelement.MapKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

@Serializable
data class DeviceSigned (
  val nameSpaces: EncodedCBORElement,
  val deviceAuth: DeviceAuth
) {
  fun toMapElement(): MapElement {
    return MapElement(buildMap {
      put(MapKey("nameSpaces"), nameSpaces)
      put(MapKey("deviceAuth"), deviceAuth.toMapElement())
    })
  }

  companion object {
    fun fromMapElement(mapElement: MapElement) = DeviceSigned(
      mapElement.value[MapKey("nameSpaces")] as? EncodedCBORElement ?: throw SerializationException("No nameSpaces property found on DeviceSigned object"),
      mapElement.value[MapKey("deviceAuth")]?.let { DeviceAuth.fromMapElement(it as MapElement) } ?: throw SerializationException("No deviceAuth property found on DeviceSigned object")
    )
  }
}