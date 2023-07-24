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

  override fun detachPayload() = COSEMac0(replacePayload(NullElement()))
  override fun attachPayload(payload: ByteArray) = COSEMac0(replacePayload(ByteStringElement(payload)))

  fun verify(sharedSecret: ByteArray, externalData: ByteArray = byteArrayOf()): Boolean {
    val mac0Content = createMacStructure(protectedHeader, payload ?: throw Exception("No payload given"), externalData).toCBOR()
    val tag = when(algorithm) {
      HMAC256 -> HMAC.hmacSHA256(sharedSecret, mac0Content).bytes
      else -> throw Exception("Algorithm $algorithm currently not supported, only supported algorithm is HMAC256 ($HMAC256)")
    }
    return signatureOrTag.contentEquals(tag)
  }

  companion object {
    private fun createMacStructure(protectedHeaderData: ByteArray, payload: ByteArray, externalData: ByteArray): ListElement {
      return ListElement(listOf(
        StringElement("MAC0"),
        ByteStringElement(protectedHeaderData),
        ByteStringElement(externalData),
        ByteStringElement(payload)
      ))
    }
    fun createWithHMAC256(payload: ByteArray, sharedSecret: ByteArray, externalData: ByteArray = byteArrayOf()): COSEMac0 {
      val protectedHeaderData = mapOf(
          MapKey(ALG_LABEL) to NumberElement(HMAC256)
      ).toDE().toCBOR()

      val mac0Content = createMacStructure(protectedHeaderData, payload, externalData).toCBOR()
      val tag = HMAC.hmacSHA256(sharedSecret, mac0Content).bytes
      return COSEMac0(listOf(
        ByteStringElement(protectedHeaderData),
        MapElement(mapOf()),
        ByteStringElement(payload),
        ByteStringElement(tag)
      ))
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