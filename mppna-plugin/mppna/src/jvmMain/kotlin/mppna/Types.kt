@file:Suppress("FINAL_UPPER_BOUND", "EXPERIMENTAL_API_USAGE")

package mppna

import com.sun.jna.*
import com.sun.jna.ptr.*

actual abstract class CPointed
actual abstract class CValuesRef<T : CPointed>
actual abstract class CVariable : CPointed()
actual class CPointerVarOf<T : CPointer<*>> : CVariable() // mppna.mppna.CVariable
actual class CPointer<T : CPointed>(val jnaPointer: Pointer) : CValuesRef<T>()
actual sealed class CPrimitiveVar : CVariable()
actual abstract class CStructVar: CVariable()

actual class BooleanVarOf<T : Boolean> : CPrimitiveVar()
actual class ByteVarOf<T : Byte>(val jnaByReference: ByteByReference) : CPrimitiveVar()
actual class ShortVarOf<T : Short>(val jnaByReference: ShortByReference) : CPrimitiveVar()
actual class IntVarOf<T : Int>(val jnaByReference: IntByReference) : CPrimitiveVar()
actual class LongVarOf<T : Long>(val jnaByReference: LongByReference) : CPrimitiveVar()

// TODO: unsigned types (???)
actual class UByteVarOf<T : UByte>(val jnaByReference: ByteByReference) : CPrimitiveVar()
actual class UShortVarOf<T : UShort>(val jnaByReference: ShortByReference): CPrimitiveVar()
actual class UIntVarOf<T : UInt>(val jnaByReference: IntByReference): CPrimitiveVar()
actual class ULongVarOf<T : ULong>(val jnaByReference: LongByReference): CPrimitiveVar()

actual class FloatVarOf<T : Float>(val jnaByReference: FloatByReference): CPrimitiveVar()
actual class DoubleVarOf<T : Double>(val jnaByReference: DoubleByReference): CPrimitiveVar()

fun <T: CPointed> Pointer.toCPointer(): CPointer<T> = CPointer(this)