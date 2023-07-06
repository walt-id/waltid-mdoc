package id.walt.mdoc.cose

import cbor.Cbor
import id.walt.mdoc.dataelement.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = COSESign1Serializer::class)
class COSESign1(
    val data: List<AnyDataElement>
) {
    val payload: ByteArray?
        get() {
            if (data.size != 4) throw SerializationException("Invalid COSE_Sign1 array")
            return when (data[2].type) {
                DEType.nil -> null
                DEType.byteString -> (data[2] as ByteStringElement).value
                else -> throw SerializationException("Invalid COSE_Sign1 payload")
            }
        }

    val x5Chain: ByteArray?
        get() {
            if (data.size != 4) throw SerializationException("Invalid COSE_Sign1 array")
            val unprotectedHeader = data[1] as? MapElement ?: throw SerializationException("Missing COSE_Sign1 unprotected header")
            val x5Chain = unprotectedHeader.value[MapKey(X5_CHAIN)] as? ByteStringElement
            return x5Chain?.value
        }

    @OptIn(ExperimentalSerializationApi::class)
    fun toCBOR() = Cbor.encodeToByteArray(COSESign1Serializer, this)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = COSESign1::class)
object COSESign1Serializer {
    override fun serialize(encoder: Encoder, value: COSESign1) {
        encoder.encodeSerializableValue(ListSerializer(DataElementSerializer), value.data)
    }

    override fun deserialize(decoder: Decoder): COSESign1 {
        return COSESign1(
            decoder.decodeSerializableValue(ListSerializer(DataElementSerializer))
        )
    }
}