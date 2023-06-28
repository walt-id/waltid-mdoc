package id.walt.mdoc.model

import cbor.internal.*
import cbor.internal.FALSE
import cbor.internal.NEXT_FLOAT
import cbor.internal.NEXT_HALF
import cbor.internal.NULL
import cbor.internal.TRUE
import cbor.internal.decoding.decodeByteString
import cbor.internal.decoding.peek
import cbor.internal.encoding.encodeByteString
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement

data class DataElementValue private constructor (
  private val data: Any?
) {
  constructor(value: Number) : this(data = value)
  constructor(value: Boolean): this(data = value)
  constructor(value: String): this(data = value)
  constructor(value: ByteArray): this(data = value)
  constructor(value: List<DataElementValue>): this(data = value)
  constructor(value: Map<String, DataElementValue>): this(data = value)
  constructor(value: Nothing?): this(data = value)

  val isNumber
    get() = data is Number
  val isBoolean
    get() = data is Boolean
  val isTextString
    get() = data is String
  val isByteString
    get() = data is ByteArray
  val isList
    get() = data is List<*>
  val isMap
    get() = data is Map<*,*>
  val isNull
    get() = data == null || data is Nothing

  val number
    get() = data as Number
  val boolean
    get() = data as Boolean
  val textString
    get() = data as String
  val byteString
    get() = data as ByteArray
  val list
    get() = data as List<DataElementValue>
  val map
    get() = data as Map<String, DataElementValue>

  @Serializer(forClass = DataElementValue::class)
  companion object: KSerializer<DataElementValue> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
      get() = buildSerialDescriptor("DataElementValue", SerialKind.CONTEXTUAL)

    override fun deserialize(decoder: Decoder): DataElementValue {
      val curHead = decoder.peek()
      return when(curHead.shr(5)) {
        0, 1 -> decoder.decodeLong()
        2 -> decoder.decodeByteString()
        3 -> decoder.decodeString()
        4 -> decoder.decodeSerializableValue(ListSerializer(Companion))
        5 -> decoder.decodeSerializableValue(MapSerializer(String.serializer(), Companion))
        7 -> when(curHead) {
          FALSE, TRUE -> decoder.decodeBoolean()
          NULL -> decoder.decodeNull()
          NEXT_HALF, NEXT_FLOAT, NEXT_DOUBLE -> decoder.decodeDouble()
          else -> throw SerializationException("DataElementValue content mustn't be contentless data")
        }
        else -> throw SerializationException("Cannot deserialize value with given header $curHead")
      }.let { DataElementValue(it) }
    }

    override fun serialize(encoder: Encoder, value: DataElementValue) {
      when(value.data) {
        is Int, is UInt, is Long, is ULong, is Short, is UShort -> encoder.encodeLong(value.number.toLong())
        is Float -> encoder.encodeFloat(value.number.toFloat())
        is Double -> encoder.encodeDouble(value.number.toDouble())
        is Boolean -> encoder.encodeBoolean(value.boolean)
        is String -> encoder.encodeString(value.textString)
        is ByteArray -> encoder.encodeByteString(value.byteString)
        is List<*> -> encoder.encodeSerializableValue(ListSerializer(Companion),
          value.list
        )
        is Map<*, *> -> encoder.encodeSerializableValue(MapSerializer(String.serializer(), Companion),
          value.map
        )
        is Nothing, null -> encoder.encodeNull()
        else -> throw SerializationException("Unsupported data value type")
      }
    }

  }
}