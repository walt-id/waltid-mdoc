package id.walt.mdoc

import kotlinx.serialization.*
import cbor.Cbor
import id.walt.mdoc.model.*
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlin.test.Test

class MDocTest {

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testSerialization() {

        val item = IssuerSignedItem(
            0u,
            byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2),
            "family_name",
            "Doe"
        )
        println(Cbor.encodeToHexString(item).uppercase())

        val embeddedItem = EmbeddedCBORDataItem(Cbor.encodeToByteArray(item))
        val embeddedItemHex = Cbor.encodeToHexString(embeddedItem).uppercase()
        embeddedItemHex shouldStartWith "D818"
        println(embeddedItemHex)
        val mdoc = MDoc(
            version = "1.0",
            documents = listOf(
                Document(
                    "org.iso.18013.5.1.mDL",
                    IssuerSigned.Companion.IssuerSignedBuilder().addIssuerSignedItems(
                        "org.iso.18013.5.1", embeddedItem, embeddedItem, embeddedItem
                    ).build()
                )
            )
        )
        val mdocHex = Cbor.encodeToHexString(mdoc).uppercase()
        println("SERIALIZED MDOC")
        println(mdocHex)

        val mdocParsed = Cbor.decodeFromHexString<MDoc>(mdocHex)
        mdocParsed.version shouldBe mdoc.version
        mdocParsed.documents shouldHaveSize mdoc.documents.size

    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testDeserialize() {
        val serializedDoc =
            "A26776657273696F6E63312E3069646F63756D656E747381A267646F6354797065756F72672E69736F2E31383031332E352E312E6D444C6C6973737565725369676E6564A16A6E616D65537061636573A1716F72672E69736F2E31383031332E352E3181D8185863A4686469676573744944006672616E646F6D58208798645B20EA200E19FFABAC92624BEE6AEC63ACEEDECFB1B80077D22BFC20E971656C656D656E744964656E7469666965726B66616D696C795F6E616D656C656C656D656E7456616C756563446F65"
        val mdoc = Cbor.decodeFromHexString<MDoc>(serializedDoc)
        println(mdoc)

        val item = Cbor.decodeFromByteArray<IssuerSignedItem<String>>(mdoc.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![0].data)
        item.elementValue shouldBe "Doe"
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testx() {
        val exampleCborData = byteArrayOf((0xBF).toByte(), 0x1A, 0x12, 0x38, 0x00, 0x00, 0x41, 0x03, (0xFF).toByte())
        println(Cbor.decodeFromByteArray<Map<Int, EmbeddedCBORDataItem>>(exampleCborData))
    }
}
