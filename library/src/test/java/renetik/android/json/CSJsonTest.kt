package renetik.android.json

import com.google.android.gms.maps.model.LatLng
import renetik.android.BuildConfig
import renetik.android.extensions.trimNewLines
import renetik.android.java.collections.CSMap
import renetik.android.lang.CSLang.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import renetik.android.java.collections.CSList

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, manifest = "src/main/AndroidManifest.xml")
class CSJsonTest {

    private val mapTest1 = map("id", 1, "isTrue", YES, "list", CSList.list("litItem1", "litItem2", "litItem3"))
    private val jsonTest1 = """{"isTrue":true,"id":1,"list":["litItem1","litItem2","litItem3"]}"""

    @Test
    fun testToJson1() {
        assertEquals(jsonTest1, toJson(mapTest1))
    }

    @Test
    fun testFromJson1() {
        val map = fromJson(jsonTest1) as Map<*, *>
        assertEquals("litItem2", (map["list"] as? List<*>)?.get(1))
    }

    private val mapTest2 = map("id", 1, "isTrue", YES, 1, CSList.list("litItem1", Object(), "litItem3"))
    private val jsonTest2 = """{"isTrue":true,"1":["litItem1","java.lang.Object@"""

    @Test
    fun testToJson2() {
        assertTrue(toJson(mapTest2).startsWith(jsonTest2))
    }

    private val json =
            """{"images":["path1","path2"],
                |"floors":[{"title":"first"},{"title":"second"}],
                |"location":[90,-138],"title":"Nice House"}""".trimMargin().trimNewLines()

    @Test
    fun testJsonDataSave() {
        val house = HouseJsonDataTest()
        house.floors.add(FlorJsonDataTest("first"))
        house.floors.add(FlorJsonDataTest("second"))
        house.title.string = "Nice House"
        house.location.latLng = LatLng(222.0, 222.0)
        assertEquals(json, toJson(house))
    }

    @Test
    fun testJsonDataLoad() {
        val house = HouseJsonDataTest()
        house.load(fromJson(json) as CSMap<String, Any?>)
        assertEquals("second", house.floors.list.second().title.string)
        assertEquals(LatLng(222.0, 222.0), house.location.latLng)
    }

}

class HouseJsonDataTest : CSJsonData() {
    val floors = CSJsonDataListProperty(this, FlorJsonDataTest::class, "floors")
    val title = CSJsonStringProperty(this, "title")
    val location = CSJsonLocationProperty(this, "location")
}

class FlorJsonDataTest() : CSJsonData() {

    val title = CSJsonStringProperty(this, "title")

    constructor(title: String) : this() {
        this.title.string = title
    }

}