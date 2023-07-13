package id.walt.mdoc.cose

import id.walt.mdoc.dataelement.*
import korlibs.crypto.HMAC
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = COSEMac0Serializer::class)
class COSEMac0(
  override val data: List<AnyDataElement>
): COSESimpleBase<COSEMac0>() {
  constructor(): this(listOf())

  override fun detachPayload() = COSEMac0(getDetachedPayloadData())
  companion object {
    fun createForMDocAuth(payload: ByteArray, sharedSecret: ByteArray): COSEMac0 {
      val identifier = StringElement("MAC0")
      val protectedHeaderData = ByteStringElement(
        mapOf(
          MapKey(ALG_LABEL) to NumberElement(HMAC256)
        ).toDE().toCBOR()
      )
      val externalData = ByteStringElement(byteArrayOf())
      val mac0Content = ListElement(listOf(identifier, protectedHeaderData, externalData, ByteStringElement(payload))).toCBOR()
      val tag = HMAC.hmacSHA256(sharedSecret, mac0Content).bytes
      return COSEMac0(listOf(
        protectedHeaderData, MapElement(mapOf()), NullElement(), ByteStringElement(tag))
      )
    }
  }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = COSEMac0::class)
object COSEMac0Serializer {
  override fun serialize(encoder: Encoder, value: COSEMac0) {
    encoder.encodeSerializableValue(ListSerializer(DataElementSerializer), value.data)
  }

  override fun deserialize(decoder: Decoder): COSEMac0 {
    return COSEMac0(
      decoder.decodeSerializableValue(ListSerializer(DataElementSerializer))
    )
  }
}