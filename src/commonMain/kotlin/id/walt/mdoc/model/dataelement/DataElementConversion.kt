package id.walt.mdoc.model.dataelement

import id.walt.mdoc.model.MapKey
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

fun Number.toDE() = NumberElement(this)
fun UInt.toDE() = NumberElement(this)
fun Float.toDE() = NumberElement(this)
fun String.toDE() = StringElement(this)
fun Boolean.toDE() = BooleanElement(this)
fun ByteArray.toDE() = ByteStringElement(this)
fun List<AnyDataElement>.toDE() = ListElement(this)
fun <KT> Map<KT, AnyDataElement>.toDE() = MapElement(this.mapKeys { when(it.key) {
  is String -> MapKey(it.key as String)
  is Int -> MapKey(it.key as Int)
  is MapKey -> it.key as MapKey
  else -> throw Exception("Unsupported map key type")
} })
fun Instant.toDE(subType: DEDateTimeMode = DEDateTimeMode.tdate) = when(subType) {
  DEDateTimeMode.tdate -> TDateElement(this)
  else -> DateTimeElement(this, subType)
}
fun LocalDate.toDE(subType: DEFullDateMode = DEFullDateMode.full_date_str) = FullDateElement(this, subType)
