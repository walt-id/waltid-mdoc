package id.walt.mdoc.devicesigned

import id.walt.mdoc.dataelement.EncodedCBORElement
import kotlinx.serialization.Serializable

@Serializable
data class DeviceSigned (
  val nameSpaces: EncodedCBORElement,
  val deviceAuth: DeviceAuth
)