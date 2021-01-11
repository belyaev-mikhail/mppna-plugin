import libmylibMppna.*
import mppna.*
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class MyLibTest {
    @Test
    fun testStrings() {
        assertEquals("MPPNA", return_string()?.toKString())
        assertEquals(5, pass_string("MPPNA".cstr))
    }

    @Test
    fun testPointers() {
        val ptr = return_pointer_to_int()
        val sum = pass_pointer_to_int(ptr)
        assertEquals(3, sum)
    }

    @Test
    fun testUnsigned() {
        assertEquals((1 shl 7).toUByte(), return_unsigned_char())
        assertEquals((1 shl 15).toUShort(), return_unsigned_short())
        assertEquals(1U shl 31, return_unsigned_int())
        assertEquals(1UL shl 63, return_unsigned_long_long())
    }
}