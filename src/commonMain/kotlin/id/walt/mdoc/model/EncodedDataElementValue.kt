package id.walt.mdoc.model

import cbor.ByteString
import cbor.Cbor
import cbor.internal.ENCODED_CBOR
import cbor.internal.decoding.decodeByteString
import cbor.internal.encoding.encodeByteString
import cbor.internal.encoding.encodeTag
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = EncodedDataElementValueSerializer::class)
@OptIn(ExperimentalSerializationApi::class)
class EncodedDataElementValue(val data: ByteArray) {
  constructor(value: DataElementValue) : this(Cbor.encodeToByteArray(value))
  fun decode(): DataElementValue {
    return Cbor.decodeFromByteArray(data)
  }
}

@Serializer(forClass = EncodedDataElementValue::class)
object EncodedDataElementValueSerializer: KSerializer<EncodedDataElementValue> {
  @OptIn(InternalSerializationApi::class)
  override val descriptor: SerialDescriptor
    get() = buildClassSerialDescriptor(EncodedDataElementValue::class.qualifiedName!!) {
      element("data", buildSerialDescriptor("EmbeddedCBORData", StructureKind.LIST), listOf(ByteString()))
    }

  override fun deserialize(decoder: Decoder): EncodedDataElementValue {
    val data = decoder.decodeByteString()
    return EncodedDataElementValue(data)
  }

  override fun serialize(encoder: Encoder, value: EncodedDataElementValue) {
    encoder.encodeTag(ENCODED_CBOR.toULong())
    encoder.encodeByteString(value.data)
  }
}