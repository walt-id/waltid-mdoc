package id.walt.mdoc.model

import id.walt.mdoc.model.dataelement.EncodedCBORElement
import kotlinx.serialization.Serializable

@Serializable
data class DeviceSigned (
  val nameSpaces: EncodedCBORElement,
  val deviceAuth: DeviceAuth
)