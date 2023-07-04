package id.walt.mdoc

enum class MDocResponseStatus(val status: UInt) {
  OK(0u),
  GENERAL_ERROR(10u),
  CBOR_DECODING_ERROR(11u),
  CBOR_VALIDATION_ERROR(12u)
}