package id.walt.mdoc.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

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

@Serializable(with = DataElementValueSerializer::class)
data class DataElementValue private constructor (
  private val data: Any?,
  val attribute: DEAttribute
) {
  constructor(value: Number) : this(data = value, DEAttribute(DEType.number))
  constructor(value: Boolean): this(data = value, DEAttribute(DEType.boolean))
  constructor(value: String): this(data = value, DEAttribute(DEType.textString))
  constructor(value: ByteArray): this(data = value, DEAttribute(DEType.byteString))
  constructor(value: List<DataElementValue>): this(data = value, DEAttribute(DEType.list))
  constructor(value: Map<String, DataElementValue>): this(data = value, DEAttribute(DEType.map))
  constructor(value: Nothing?): this(data = value, DEAttribute(DEType.nil))

  // tdate #6.0, #6.1
  constructor(value: Instant, subType: DEDateTimeMode = DEDateTimeMode.tdate): this(data = value, DEDateTimeAttribute(subType))
  // full-date #6.1004, #6.100
  constructor(value: LocalDate, subType: DEFullDateMode = DEFullDateMode.full_date_str): this(data = value, DEFullDateAttribute(subType))
  constructor(value: EncodedDataElementValue): this(data = value, DEAttribute(DEType.encodedCbor))

  val type
    get() = attribute.type

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
  val dateTime
    get() = data as Instant
  val fullDate
    get() = data as LocalDate
  val embeddedCBOR
    get() = data as EncodedDataElementValue
}