package id.walt.mdoc.model

import cbor.ByteString
import id.walt.mdoc.model.dataelement.*
import kotlinx.serialization.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = IssuerSignedItemSerializer::class)
data class IssuerSignedItem(
    val digestID: UInt,
    @ByteString val random: ByteArray,
    val elementIdentifier: String,
    val elementValue: AnyDataElement
) {
    fun toMapElement(): MapElement {
        return MapElement(mapOf(
            MapKey("digestID") to NumberElement(digestID.toLong()),
            MapKey("random") to ByteStringElement(random),
            MapKey("elementIdentifier") to StringElement(elementIdentifier),
            MapKey("elementValue") to elementValue
        ))
    }

    companion object {
        fun fromMapElement(element: MapElement): IssuerSignedItem {
            return IssuerSignedItem(
                (element.value[MapKey("digestID")]!!.value as Number).toInt().toUInt(),
                element.value[MapKey("random")]!!.value as ByteArray,
                element.value[MapKey("elementIdentifier")]!!.value as String,
                element.value[MapKey("elementValue")]!!
            )
        }
    }
}

@Serializer(forClass = IssuerSignedItem::class)
class IssuerSignedItemSerializer: KSerializer<IssuerSignedItem> {
    override fun serialize(encoder: Encoder, value: IssuerSignedItem) {
        encoder.encodeSerializableValue(DataElementSerializer, value.toMapElement())
    }

    override fun deserialize(decoder: Decoder): IssuerSignedItem {
        return IssuerSignedItem.fromMapElement(decoder.decodeSerializableValue(DataElementSerializer) as MapElement)
    }
}
