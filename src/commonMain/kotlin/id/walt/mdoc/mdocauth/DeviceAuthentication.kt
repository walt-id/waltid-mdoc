package id.walt.mdoc.mdocauth

import cbor.Cbor
import id.walt.mdoc.cose.COSESign1Serializer
import id.walt.mdoc.dataelement.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = DeviceAuthenticationSerializer::class)
class DeviceAuthentication internal constructor (
  val data: List<AnyDataElement>
) {
  constructor(sessionTranscript: ListElement, docType: String, deviceNameSpaces: EncodedCBORElement) : this(
    listOf(
      StringElement("DeviceAuthentication"),
      sessionTranscript,
      StringElement(docType),
      deviceNameSpaces
    )
  )

  @OptIn(ExperimentalSerializationApi::class)
  fun toCBOR() = Cbor.encodeToByteArray(DeviceAuthenticationSerializer, this)

  fun toDE() = ListElement(data)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = DeviceAuthentication::class)
object DeviceAuthenticationSerializer: KSerializer<DeviceAuthentication> {
  override fun deserialize(decoder: Decoder): DeviceAuthentication {
    return DeviceAuthentication(
      decoder.decodeSerializableValue(ListSerializer(DataElementSerializer))
    )
  }

  override fun serialize(encoder: Encoder, value: DeviceAuthentication) {
    encoder.encodeSerializableValue(ListSerializer(DataElementSerializer), value.data)
  }
}