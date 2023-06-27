package id.walt.mdoc.model

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.cbor.ByteString
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeCollection

@Serializable
data class IssuerSignedItem<T>(
  val digestID: UInt,
  @ByteString val random: ByteArray,
  val elementIdentifier: String,
  val elementValue: T
)

@Serializable
data class Dummy(
  @ByteString val bytes: ByteArray
)

@Serializable(with = IssuerSignedItemBytesSerializer::class)
class IssuerSignedItemBytes(
  @ByteString val bytes: ByteArray
)

class IssuerSignedItemBytesSerializer<T>: KSerializer<IssuerSignedItemBytes> {
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  override val descriptor: SerialDescriptor
    get() = buildClassSerialDescriptor("IssuerSignedItemBytes") {
      this.element<ByteArray>("kotlin.ByteArray", listOf(ByteString()))
    }

  override fun deserialize(decoder: Decoder): IssuerSignedItemBytes {
    val bytes = decoder.decodeSerializableValue(ByteArraySerializer())
    return IssuerSignedItemBytes(bytes)
  }

  @OptIn(ExperimentalSerializationApi::class)
  override fun serialize(encoder: Encoder, value: IssuerSignedItemBytes) {
    encoder.encodeSerializableValue(ByteArraySerializer(), value.bytes)
  }

}
