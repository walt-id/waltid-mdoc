package id.walt.mdoc.model

import cbor.internal.*
import cbor.internal.decoding.decodeByteString
import cbor.internal.decoding.decodeTag
import cbor.internal.decoding.peek
import cbor.internal.encoding.encodeByteString
import cbor.internal.encoding.encodeTag
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
object DataElementValueSerializer: KSerializer<DataElementValue> {
  @OptIn(InternalSerializationApi::class)
  override val descriptor: SerialDescriptor
    get() = buildSerialDescriptor("DataElementValue", SerialKind.CONTEXTUAL)

  override fun deserialize(decoder: Decoder): DataElementValue {
    val curHead = decoder.peek()
    val majorType = curHead.shr(5)
    return when(majorType) {
      0, 1 -> DataElementValue(decoder.decodeLong())
      2 -> DataElementValue(decoder.decodeByteString())
      3 -> DataElementValue(decoder.decodeString())
      4 -> DataElementValue(decoder.decodeSerializableValue(ListSerializer(DataElementValueSerializer)))
      5 -> DataElementValue(decoder.decodeSerializableValue(MapSerializer(String.serializer(), DataElementValueSerializer)))
      6 -> {
        val tag = decoder.decodeTag()
        when(tag) {
          ENCODED_CBOR -> DataElementValue(decoder.decodeSerializableValue(EncodedDataElementValueSerializer))
          TDATE, TIME -> deserializeDateTime(decoder, tag)
          FULL_DATE_STR, FULL_DATE_INT -> deserializeFullDate(decoder, tag)
          else -> throw SerializationException("The given tagged value type is currently not supported")
        }
      }
      7 -> when(curHead) {
        FALSE, TRUE -> DataElementValue(decoder.decodeBoolean())
        NULL -> DataElementValue(decoder.decodeNull())
        NEXT_HALF, NEXT_FLOAT, NEXT_DOUBLE -> DataElementValue(decoder.decodeDouble())
        else -> throw SerializationException("DataElement value mustn't be contentless data")
      }
      else -> throw SerializationException("Cannot deserialize value with given header $curHead")
    }
  }

  override fun serialize(encoder: Encoder, value: DataElementValue) {
    when(value.type) {
      DEType.number -> when(value.number) {
        is Int, is Long, is Short -> encoder.encodeLong(value.number.toLong())
        is Float -> encoder.encodeFloat(value.number.toFloat())
        is Double -> encoder.encodeDouble(value.number.toDouble())
      }
      DEType.boolean -> encoder.encodeBoolean(value.boolean)
      DEType.textString -> encoder.encodeString(value.textString)
      DEType.byteString -> encoder.encodeByteString(value.byteString)
      DEType.list -> encoder.encodeSerializableValue(
        ListSerializer(DataElementValueSerializer),
        value.list
      )
      DEType.map -> encoder.encodeSerializableValue(
        MapSerializer(String.serializer(), DataElementValueSerializer),
        value.map
      )
      DEType.nil -> encoder.encodeNull()
      DEType.encodedCbor -> encoder.encodeSerializableValue(EncodedDataElementValueSerializer, value.embeddedCBOR)
      DEType.dateTime -> serializeDateTime(encoder, value)
      DEType.fullDate -> serializeFullDate(encoder, value)
    }
  }

  private fun serializeDateTime(encoder: Encoder, value: DataElementValue) {
    val attribute = value.attribute as DEDateTimeAttribute
    when(attribute.mode) {
      DEDateTimeMode.tdate -> {
          encoder.encodeTag(TDATE.toULong())
          encoder.encodeString(value.dateTime.toString())
        }
      DEDateTimeMode.time_int, DEDateTimeMode.time_float, DEDateTimeMode.time_double -> {
        encoder.encodeTag(TIME.toULong())
        when(attribute.mode) {
          DEDateTimeMode.time_int -> encoder.encodeLong(value.dateTime.epochSeconds)
          DEDateTimeMode.time_float -> encoder.encodeFloat(value.dateTime.toEpochMilliseconds().toFloat() / 1000f)
          DEDateTimeMode.time_double -> encoder.encodeDouble(value.dateTime.toEpochMilliseconds().toDouble() / 1000.0)
          else -> {} // not possible
        }
      }
    }
  }

  private fun deserializeDateTime(decoder: Decoder, tag: Long): DataElementValue {
    val nextHead = decoder.peek()
    return when(tag) {
      TDATE -> DataElementValue(Instant.parse(decoder.decodeString()), DEDateTimeMode.tdate)
      TIME -> when(nextHead) {
        NEXT_HALF, NEXT_FLOAT -> DataElementValue(Instant.fromEpochMilliseconds((decoder.decodeFloat() * 1000.0f).toLong()), DEDateTimeMode.time_float)
        NEXT_DOUBLE -> DataElementValue(Instant.fromEpochMilliseconds((decoder.decodeDouble() * 1000.0).toLong()), DEDateTimeMode.time_double)
        else -> DataElementValue(Instant.fromEpochSeconds(decoder.decodeLong()), DEDateTimeMode.time_int)
      }
      else -> throw SerializationException("Unsupported tag number for DateTime value: #6.$tag, supported tags are #6.0, #6.1")
    }

  }

  private fun serializeFullDate(encoder: Encoder, value: DataElementValue) {
    val attribute = value.attribute as DEFullDateAttribute
    when(attribute.mode) {
      DEFullDateMode.full_date_str -> {
        encoder.encodeTag(FULL_DATE_STR.toULong())
        encoder.encodeString(value.fullDate.toString())
      }
      DEFullDateMode.full_date_int -> {
        encoder.encodeTag(FULL_DATE_INT.toULong())
        encoder.encodeInt(value.fullDate.toEpochDays())
      }
    }
  }

  private fun deserializeFullDate(decoder: Decoder, tag: Long): DataElementValue {
    return when(tag) {
      FULL_DATE_STR -> DataElementValue(LocalDate.parse(decoder.decodeString()), DEFullDateMode.full_date_str)
      FULL_DATE_INT -> DataElementValue(LocalDate.fromEpochDays(decoder.decodeLong().toInt()), DEFullDateMode.full_date_int)
      else -> throw SerializationException("Unsupported tag number for full-date: #6.$tag, supported tags are #6.1004, #6.100")
    }
  }

}