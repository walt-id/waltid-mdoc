package id.walt.mdoc.model

import cbor.ByteString
import cbor.internal.encoding.CborEncoder
import cbor.internal.encoding.encodeByteString
import cbor.internal.encoding.encodeTag
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@Serializable(with = EmbeddedCBORDataItemSerializer::class)
//@JvmInline
@OptIn(ExperimentalSerializationApi::class)
class EmbeddedCBORDataItem(@ByteString val data: ByteArray)

class EmbeddedCBORDataItemSerializer: KSerializer<EmbeddedCBORDataItem> {
  @OptIn(InternalSerializationApi::class)
  override val descriptor: SerialDescriptor
    get() = buildClassSerialDescriptor(EmbeddedCBORDataItem::class.qualifiedName!!) {
      element("data", buildSerialDescriptor("EmbeddedCBORData", StructureKind.LIST), listOf(ByteString()))
    }

  override fun deserialize(decoder: Decoder): EmbeddedCBORDataItem {
    val data = decoder.decodeSerializableValue(ByteArraySerializer())
    return EmbeddedCBORDataItem(data)
  }

  override fun serialize(encoder: Encoder, value: EmbeddedCBORDataItem) {
    encoder.encodeTag(24u)
    encoder.encodeByteString(value.data)
  }
}