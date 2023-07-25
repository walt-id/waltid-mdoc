package id.walt.mdoc.readerauth

import cbor.Cbor
import id.walt.mdoc.dataelement.*
import id.walt.mdoc.mdocauth.DeviceAuthentication
import id.walt.mdoc.mdocauth.DeviceAuthenticationSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ReaderAuthenticationSerializer::class)
class ReaderAuthentication internal constructor(
  val data: List<AnyDataElement>
) {
  constructor(sessionTranscript: ListElement, itemsRequest: EncodedCBORElement) : this(
    listOf(
      StringElement("ReaderAuthentication"),
      sessionTranscript,
      itemsRequest
    )
  )

  @OptIn(ExperimentalSerializationApi::class)
  fun toCBOR() = Cbor.encodeToByteArray(ReaderAuthenticationSerializer, this)

  fun toDE() = ListElement(data)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ReaderAuthentication::class)
object ReaderAuthenticationSerializer: KSerializer<ReaderAuthentication> {
  override fun deserialize(decoder: Decoder): ReaderAuthentication {
    return ReaderAuthentication(
      decoder.decodeSerializableValue(ListSerializer(DataElementSerializer))
    )
  }

  override fun serialize(encoder: Encoder, value: ReaderAuthentication) {
    encoder.encodeSerializableValue(ListSerializer(DataElementSerializer), value.data)
  }
}