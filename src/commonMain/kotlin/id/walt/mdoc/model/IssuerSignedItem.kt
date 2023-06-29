package id.walt.mdoc.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ByteArraySerializer
import cbor.ByteString
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class IssuerSignedItem(
    val digestID: UInt,
    @ByteString val random: ByteArray,
    val elementIdentifier: String,
    val elementValue: DataElementValue
) {
    fun toDataElementValue(): DataElementValue {
        return DataElementValue(mapOf(
            MapKey("digestID") to DataElementValue(digestID.toLong()),
            MapKey("random") to DataElementValue(random),
            MapKey("elementIdentifier") to DataElementValue(elementIdentifier),
            MapKey("elementValue") to elementValue
        ))
    }

    companion object {
        fun fromDataElementValue(value: DataElementValue): IssuerSignedItem {
            return IssuerSignedItem(
                value.map[MapKey("digestID")]!!.number.toInt().toUInt(),
                value.map[MapKey("random")]!!.byteString,
                value.map[MapKey("elementIdentifier")]!!.textString,
                value.map[MapKey("elementValue")]!!
            )
        }
    }
}
