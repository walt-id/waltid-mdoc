package id.walt.mdoc.doc

enum class VerificationType {
  VALIDITY,
  DOC_TYPE,
  CERTIFICATE_CHAIN,
  ITEMS_TAMPER_CHECK,
  ISSUER_SIGNATURE,
  DEVICE_SIGNATURE;

  infix fun and(other: VerificationType): VerificationTypes = setOf(this, other)

  companion object {
    val all: VerificationTypes
      get() = VerificationType.values().toSet()

    val forPresentation: VerificationTypes
      get() = all

    val forIssuance: VerificationTypes
      get() = (VALIDITY and DOC_TYPE and CERTIFICATE_CHAIN and ITEMS_TAMPER_CHECK and ISSUER_SIGNATURE)
  }
}

typealias VerificationTypes = Set<VerificationType>

infix fun VerificationTypes.has(other: VerificationType) = this.contains(other)
infix fun VerificationTypes.allOf(other: VerificationTypes) = this.containsAll(other)
infix fun VerificationTypes.and(other: VerificationType): VerificationTypes = setOf(other, *this.toTypedArray())
