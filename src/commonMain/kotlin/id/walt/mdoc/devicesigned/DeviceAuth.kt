package id.walt.mdoc.devicesigned

import id.walt.mdoc.cose.COSEMac0
import id.walt.mdoc.cose.COSESign1
import id.walt.mdoc.dataelement.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Encoder

@Serializable(with = DeviceAuthSerializer::class)
data class DeviceAuth (
  val deviceMac: COSEMac0? = null,
  val deviceSignature: COSESign1? = null
) {
  fun toMapElement(): MapElement {
    return MapElement(buildMap {
      deviceMac?.let { put(MapKey("deviceMac"), it.toDE()) }
      deviceSignature?.let { put(MapKey("deviceSignature"), it.toDE()) }
    })
  }

  companion object {
    fun fromMapElement(mapElement: MapElement) = DeviceAuth(
      (mapElement.value[MapKey("deviceMac")] as? ListElement)?.let { COSEMac0(it.value) },
      (mapElement.value[MapKey("deviceSignature")] as? ListElement)?.let { COSESign1(it.value) }
    )
  }
}

@Serializer(forClass = DeviceAuth::class)
object DeviceAuthSerializer: KSerializer<DeviceAuth> {
  override fun serialize(encoder: Encoder, value: DeviceAuth) {
    encoder.encodeSerializableValue(DataElementSerializer, value.toMapElement())
  }
}