@file:Suppress("FINAL_UPPER_BOUND", "EXPERIMENTAL_API_USAGE")

package mppna

import com.sun.jna.*
import com.sun.jna.ptr.*

interface VariableByReference {
    val jnaByReference: ByReference
}

actual abstract class CPointed
actual abstract class CValuesRef<T : CPointed>
actual abstract class CVariable : CPointed(), VariableByReference
actual class CPointerVarOf<T : CPointer<*>>(override val jnaByReference: PointerByReference) : CVariable()
actual class CPointer<T : CPointed>(val jnaPointer: Pointer, val cVariable: CVariable? = null) : CValuesRef<T>()
actual sealed class CPrimitiveVar : CVariable()
actual abstract class CStructVar : CVariable()

actual class BooleanVarOf<T : Boolean>(override val jnaByReference: ByteByReference) : CPrimitiveVar()
actual class ByteVarOf<T : Byte>(override val jnaByReference: ByteByReference) : CPrimitiveVar()
actual class ShortVarOf<T : Short>(override val jnaByReference: ShortByReference) : CPrimitiveVar()
actual class IntVarOf<T : Int>(override val jnaByReference: IntByReference) : CPrimitiveVar()
actual class LongVarOf<T : Long>(override val jnaByReference: LongByReference) : CPrimitiveVar()

// TODO: unsigned types (???)
actual class UByteVarOf<T : UByte>(override val jnaByReference: ByteByReference) : CPrimitiveVar()
actual class UShortVarOf<T : UShort>(override val jnaByReference: ShortByReference) : CPrimitiveVar()
actual class UIntVarOf<T : UInt>(override val jnaByReference: IntByReference) : CPrimitiveVar()
actual class ULongVarOf<T : ULong>(override val jnaByReference: NativeLongByReference) : CPrimitiveVar()

actual class FloatVarOf<T : Float>(override val jnaByReference: FloatByReference) : CPrimitiveVar()
actual class DoubleVarOf<T : Double>(override val jnaByReference: DoubleByReference) : CPrimitiveVar()

fun <T : CPointed> Pointer.toCPointer(): CPointer<T> = CPointer(this)
fun Byte.toBoolean(): Boolean = this != 0.toByte()
fun Boolean.toByte(): Byte = (if (this) 1 else 0).toByte()
fun <V : CVariable> CValuesRef<V>.getJnaByReference(): ByReference? = (this as? CPointer)?.cVariable?.jnaByReference

actual interface CEnum {
    val value: Any
}