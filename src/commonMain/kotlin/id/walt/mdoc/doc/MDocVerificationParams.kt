package id.walt.mdoc.doc

import id.walt.mdoc.docrequest.MDocRequest
import id.walt.mdoc.mdocauth.DeviceAuthentication

data class MDocVerificationParams(
  val verificationTypes: VerificationTypes,
  val issuerKeyID: String? = null,
  val deviceKeyID: String? = null,
  val ephemeralMacKey: ByteArray? = null,
  val deviceAuthentication: DeviceAuthentication? = null,
  val mDocRequest: MDocRequest? = null
)
