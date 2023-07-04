package id.walt.mdoc.model.dataelement

import cbor.Cbor
import id.walt.mdoc.model.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray

/** Data Element Type,
 * see also:
 * CBOR Major types: https://www.rfc-editor.org/rfc/rfc8949#name-major-types
 * CBOR Prelude: https://www.rfc-editor.org/rfc/rfc8610.html#appendix-D
 * CBOR date extension: https://datatracker.ietf.org/doc/html/rfc8943
 */
enum class DEType {
  number,     // #0, #1, #7.25, #7.26, #7.27
  boolean,    // #7.20, #7.21
  textString, // #3
  byteString, // #2
  nil,        // #7.22
  dateTime,   // #6.0, #6.1
  fullDate,   // #6.1004, #6.100
  list,       // #4
  map,        // #5,
  encodedCbor // #6.24
}

/**
 * Data Element attribute
 */
open class DEAttribute(val type: DEType)

/**
 * Data Element DateTime mode: tdate (rfc3339 string) or time since epoch (as int/long or floating point)
 */
enum class DEDateTimeMode {
  tdate,          // #6.0
  time_int,       // #6.1
  time_float,     // #6.1
  time_double,    // #6.1
}

/**
 * Data Element full date mode: rfc3339 string date-only part or number of days since epoch as int/long
 */
enum class DEFullDateMode {
  full_date_str,  // #6.1004
  full_date_int   // #6.100
}

/**
 * Data Element DateTime attribute
 */
class DEDateTimeAttribute(val mode: DEDateTimeMode = DEDateTimeMode.tdate) : DEAttribute(DEType.dateTime)
/**
 * Data Element full date attribute
 */
class DEFullDateAttribute(val mode: DEFullDateMode = DEFullDateMode.full_date_str): DEAttribute(DEType.fullDate)

@Serializable(with = DataElementSerializer::class)
abstract class DataElement<T> (
  val value: T, val attribute: DEAttribute
) {
  val type
    get() = attribute.type

  override fun equals(other: Any?): Boolean {
    val res = other is DataElement<*> && other.type == type && when(type) {
      DEType.byteString, DEType.encodedCbor -> (value as ByteArray).contentEquals(other.value as ByteArray)
      DEType.list -> (value as List<AnyDataElement>).all { (other.value as List<AnyDataElement>).contains(it) }
      DEType.map -> (value as Map<MapKey, AnyDataElement>).all { (other.value as  Map<MapKey, AnyDataElement>)[it.key] == it.value }
      else -> value == other.value
    }
    return res
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }

  @OptIn(ExperimentalSerializationApi::class)
  fun toEncodedCBORElement() = EncodedCBORElement(Cbor.encodeToByteArray(DataElementSerializer, this))
}

typealias AnyDataElement = DataElement<*>
@Serializable(with = DataElementSerializer::class)
class NumberElement(value: Number): DataElement<Number>(value, DEAttribute(DEType.number)) {
  constructor(value: UInt) : this(value.toLong())
}
@Serializable(with = DataElementSerializer::class)
class BooleanElement(value: Boolean): DataElement<Boolean>(value, DEAttribute(DEType.boolean))
@Serializable(with = DataElementSerializer::class)
class StringElement(value: String): DataElement<String>(value, DEAttribute(DEType.textString))
@Serializable(with = DataElementSerializer::class)
class ByteStringElement(value: ByteArray): DataElement<ByteArray>(value, DEAttribute(DEType.byteString))
@Serializable(with = DataElementSerializer::class)
class ListElement(value: List<AnyDataElement>): DataElement<List<AnyDataElement>>(value, DEAttribute(DEType.list)) {
  constructor() : this(listOf())
}
@Serializable(with = DataElementSerializer::class)
class MapElement(value: Map<MapKey, AnyDataElement>): DataElement<Map<MapKey, AnyDataElement>>(value, DEAttribute(DEType.map))
@Serializable(with = DataElementSerializer::class)
class NullElement(value: Nothing? = null): DataElement<Nothing?>(null, DEAttribute(DEType.nil))

// tdate: #6.0, time: #6.1
@Serializable(with = DataElementSerializer::class)
open class DateTimeElement(value: Instant, subType: DEDateTimeMode = DEDateTimeMode.tdate): DataElement<Instant>(value, DEDateTimeAttribute(subType))
@Serializable(with = DataElementSerializer::class)
class TDateElement(value: Instant) : DateTimeElement(value, DEDateTimeMode.tdate)
// full-date #6.1004, #6.100
@Serializable(with = DataElementSerializer::class)
class FullDateElement(value: LocalDate, subType: DEFullDateMode = DEFullDateMode.full_date_str): DataElement<LocalDate>(value, DEFullDateAttribute(subType))

@Serializable(with = DataElementSerializer::class)
class EncodedCBORElement(cborData: ByteArray): DataElement<ByteArray>(cborData, DEAttribute(DEType.encodedCbor)) {
  @OptIn(ExperimentalSerializationApi::class)
  constructor(element: AnyDataElement) : this(Cbor.encodeToByteArray(DataElementSerializer, element))

  fun decode(): AnyDataElement {
    return decodeDataElement()
  }
  inline fun <reified T: DataElement<*>> decodeDataElement(): T {
    return Cbor.decodeFromByteArray(DataElementSerializer, value) as T
  }
  inline fun <reified T> decode(): T {
    return Cbor.decodeFromByteArray(value)
  }

  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    fun fromEncodedCBORElementData(data: ByteArray)
      = Cbor.decodeFromByteArray(DataElementSerializer, data) as EncodedCBORElement
  }
}

