package id.walt.mdoc.model

import id.walt.mdoc.model.dataelement.AnyDataElement
import id.walt.mdoc.model.dataelement.DataElement
import id.walt.mdoc.model.dataelement.DataElementSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = IssuerAuthSerializer::class)
class IssuerAuth(
    val cose_sign1: List<AnyDataElement>
)

@Serializer(forClass = IssuerAuth::class)
object IssuerAuthSerializer {
    override fun serialize(encoder: Encoder, value: IssuerAuth) {
        encoder.encodeSerializableValue(ListSerializer(DataElementSerializer), value.cose_sign1)
    }

    override fun deserialize(decoder: Decoder): IssuerAuth {
        return IssuerAuth(
            decoder.decodeSerializableValue(ListSerializer(DataElementSerializer))
        )
    }
}