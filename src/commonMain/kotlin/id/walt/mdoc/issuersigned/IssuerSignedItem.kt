package id.walt.mdoc.issuersigned

import id.walt.mdoc.dataelement.*
import korlibs.crypto.SecureRandom
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
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

        fun createWithRandomSalt(digestID: UInt, elementIdentifier: String, elementValue: AnyDataElement): IssuerSignedItem {
            return IssuerSignedItem(digestID.toDE(),  SecureRandom.nextBytes(16).toDE(), elementIdentifier.toDE(), elementValue)
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
