package id.walt.mdoc

import kotlinx.serialization.*
import cbor.Cbor
import id.walt.mdoc.model.*
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class MDocTest {

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testSerialization() {

        val textItem = IssuerSignedItem(0u,byteArrayOf(1, 2, 3),"family_name", DataElementValue("Doe"))
        val byteStringItem = IssuerSignedItem(0u,byteArrayOf(1, 2, 3),"byte_array", DataElementValue(byteArrayOf(0,1,2)))
        val intItem = IssuerSignedItem(0u,byteArrayOf(1, 2, 3),"age", DataElementValue(35))
        val floatItem = IssuerSignedItem(0u,byteArrayOf(1, 2, 3),"factor_x", DataElementValue(0.5f))
        val booleanItem = IssuerSignedItem(0u,byteArrayOf(1, 2, 3),"is_over_18", DataElementValue(true))
        val listItem = IssuerSignedItem(0u,byteArrayOf(1, 2, 3),"driving_privileges", DataElementValue(listOf(DataElementValue("A"), DataElementValue("B"))))
        val mapItem = IssuerSignedItem(0u,byteArrayOf(1, 2, 3),"attributes", DataElementValue(mapOf("attribute1" to DataElementValue("X"), "attribute2" to DataElementValue("Y"))))
        val nullItem = IssuerSignedItem(0u, byteArrayOf(1,2,3), "nothing", DataElementValue(null))
        val embeddedCborValue = "The encoded item"
        val cborItem = IssuerSignedItem(0u, byteArrayOf(1,2,3), "encoded_cbor", DataElementValue(EncodedDataElementValue(Cbor.encodeToByteArray(embeddedCborValue))))
        val tdateItem = IssuerSignedItem(0u, byteArrayOf(1,2,3), "issue_date", DataElementValue(Clock.System.now()))
        val tdateIntItem = IssuerSignedItem(0u, byteArrayOf(1,2,3), "issue_date_int", DataElementValue(Clock.System.now(), DEDateTimeMode.time_int))
        val tdateDblItem = IssuerSignedItem(0u, byteArrayOf(1,2,3), "issue_date_dbl", DataElementValue(Clock.System.now(), DEDateTimeMode.time_double))
        val fullDateStrItem = IssuerSignedItem(0u, byteArrayOf(1,2,3), "birth_date", DataElementValue(LocalDate.parse("1983-07-05"), DEFullDateMode.full_date_str))
        val fullDateIntItem = IssuerSignedItem(0u, byteArrayOf(1,2,3), "expiry_date", DataElementValue(LocalDate.parse("2025-12-31"), DEFullDateMode.full_date_int))

        val mdoc = MDocResponse(
            version = "1.0",
            documents = listOf(
                MDocBuilder("org.iso.18013.5.1.mDL").
                    addIssuerSignedItems(
                        "org.iso.18013.5.1", textItem, byteStringItem, intItem, floatItem, booleanItem, listItem, mapItem, nullItem, cborItem, tdateItem, tdateIntItem, tdateDblItem, fullDateStrItem, fullDateIntItem
                    ).build()
            )
        )
        val mdocHex = Cbor.encodeToHexString(mdoc).uppercase()
        println("SERIALIZED MDOC:")
        println(mdocHex)

        val mdocParsed = Cbor.decodeFromHexString<MDocResponse>(mdocHex)
        mdocParsed.version shouldBe mdoc.version
        mdocParsed.documents shouldHaveSize mdoc.documents.size
        mdocParsed.documents[0].docType shouldBe mdoc.documents[0].docType
        mdocParsed.documents[0].issuerSigned.nameSpaces!!.keys shouldContainAll mdoc.documents[0].issuerSigned.nameSpaces!!.keys

        val originalIssuerSignedItems = mdoc.documents[0].getIssuerSignedItems("org.iso.18013.5.1")
        val parsedDocIssuerSignedItems = mdocParsed.documents[0].getIssuerSignedItems("org.iso.18013.5.1")
        parsedDocIssuerSignedItems.size shouldBe originalIssuerSignedItems.size

        parsedDocIssuerSignedItems.union(originalIssuerSignedItems).groupBy { it.elementIdentifier }.values.forEach { grp ->
            grp.size shouldBe 2
            grp.first().elementValue.type shouldBe grp.last().elementValue.type
            grp.first().elementValue shouldBe grp.last().elementValue
            if(grp.first().elementValue.type == DEType.encodedCbor) {
                grp.first().elementValue.embeddedCBOR.decode() shouldBe grp.last().elementValue.embeddedCBOR.decode()
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testDeserialize() {
        val serializedDoc =
            "A26776657273696F6E63312E3069646F63756D656E747381A267646F6354797065756F72672E69736F2E31383031332E352E312E6D444C6C6973737565725369676E6564A16A6E616D65537061636573A1716F72672E69736F2E31383031332E352E3181D8185863A4686469676573744944006672616E646F6D58208798645B20EA200E19FFABAC92624BEE6AEC63ACEEDECFB1B80077D22BFC20E971656C656D656E744964656E7469666965726B66616D696C795F6E616D656C656C656D656E7456616C756563446F65"
        val mdoc = Cbor.decodeFromHexString<MDocResponse>(serializedDoc)
        println(mdoc)

        val item = Cbor.decodeFromByteArray<IssuerSignedItem>(mdoc.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![0].data)
        item.elementValue.type shouldBe DEType.textString
        item.elementValue.textString shouldBe "Doe"
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testx() {
        val exampleCborData = byteArrayOf((0xBF).toByte(), 0x1A, 0x12, 0x38, 0x00, 0x00, 0x41, 0x03, (0xFF).toByte())
        println(Cbor.decodeFromByteArray<Map<Int, EncodedDataElementValue>>(exampleCborData))
    }
}
