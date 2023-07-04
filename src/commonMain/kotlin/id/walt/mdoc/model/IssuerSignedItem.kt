package id.walt.mdoc.model

import cbor.ByteString
import id.walt.mdoc.model.dataelement.*
import kotlinx.serialization.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = IssuerSignedItemSerializer::class)
data class IssuerSignedItem(
    val digestID: NumberElement,
    val random: ByteStringElement,
    val elementIdentifier: StringElement,
    val elementValue: AnyDataElement
) {
    fun toMapElement(): MapElement {
        return MapElement(mapOf(
            MapKey("digestID") to digestID,
            MapKey("random") to random,
            MapKey("elementIdentifier") to elementIdentifier,
            MapKey("elementValue") to elementValue
        ))
    }

    companion object {
        fun fromMapElement(element: MapElement): IssuerSignedItem {
            return IssuerSignedItem(
                (element.value[MapKey("digestID")]!! as NumberElement),
                element.value[MapKey("random")]!! as ByteStringElement,
                element.value[MapKey("elementIdentifier")]!! as StringElement,
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
