package id.walt.mdoc.devicesigned

import id.walt.mdoc.dataelement.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Encoder

@Serializable(with = DeviceAuthSerializer::class)
data class DeviceAuth (
  val deviceMac: ListElement? = null,
  val deviceSignature: AnyDataElement? = null
) {
  fun toMapElement(): MapElement {
    return MapElement(buildMap {
      deviceMac?.let { put(MapKey("deviceMac"), it) }
      deviceSignature?.let { put(MapKey("deviceSignature"), it) }
    })
  }
}

@Serializer(forClass = DeviceAuth::class)
object DeviceAuthSerializer: KSerializer<DeviceAuth> {
  override fun serialize(encoder: Encoder, value: DeviceAuth) {
    encoder.encodeSerializableValue(DataElementSerializer, value.toMapElement())
  }
}