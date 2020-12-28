@file:Suppress("FINAL_UPPER_BOUND", "EXPERIMENTAL_API_USAGE")

package mppna

import com.sun.jna.*
import com.sun.jna.ptr.*

interface VariableByReference {
    val jnaByReference: ByReference
}

actual abstract class CPointed
actual abstract class CValuesRef<T : CPointed>(val jnaPointer: Pointer)
actual abstract class CValues<T : CVariable>(jnaPointer: Pointer) : CValuesRef<T>(jnaPointer)
private class CString<T : CVariable>(jnaPointer: Pointer): CValues<T>(jnaPointer)
actual abstract class CVariable : CPointed(), VariableByReference
actual class CPointerVarOf<T : CPointer<*>>(override val jnaByReference: PointerByReference) : CVariable()
actual class CPointer<T : CPointed>(jnaPointer: Pointer, val cVariable: CVariable? = null) : CValuesRef<T>(jnaPointer)
actual sealed class CPrimitiveVar : CVariable()
actual abstract class CStructVar : CVariable()

actual class BooleanVarOf<T : Boolean>(override val jnaByReference: ByteByReference) : CPrimitiveVar()
actual class ByteVarOf<T : Byte>(override val jnaByReference: ByteByReference) : CPrimitiveVar()
actual class ShortVarOf<T : Short>(override val jnaByReference: ShortByReference) : CPrimitiveVar()
actual class IntVarOf<T : Int>(override val jnaByReference: IntByReference) : CPrimitiveVar()
actual class LongVarOf<T : Long>(override val jnaByReference: LongByReference) : CPrimitiveVar()
actual class UByteVarOf<T : UByte>(override val jnaByReference: ByteByReference) : CPrimitiveVar()
actual class UShortVarOf<T : UShort>(override val jnaByReference: ShortByReference) : CPrimitiveVar()
actual class UIntVarOf<T : UInt>(override val jnaByReference: IntByReference) : CPrimitiveVar()
actual class ULongVarOf<T : ULong>(override val jnaByReference: NativeLongByReference) : CPrimitiveVar()
actual class FloatVarOf<T : Float>(override val jnaByReference: FloatByReference) : CPrimitiveVar()
actual class DoubleVarOf<T : Double>(override val jnaByReference: DoubleByReference) : CPrimitiveVar()

fun ByteByReference.toCPointer(): CPointer<BooleanVar> = CPointer(this.pointer, BooleanVarOf<Boolean>(this))
fun ShortByReference.toCPointer(): CPointer<ShortVar> = CPointer(this.pointer, ShortVarOf<Short>(this))
fun IntByReference.toCPointer(): CPointer<IntVar> = CPointer(this.pointer, IntVarOf<Int>(this))
fun LongByReference.toCPointer(): CPointer<LongVar> = CPointer(this.pointer, LongVarOf<Long>(this))
fun NativeLongByReference.toCPointer(): CPointer<ULongVar> = CPointer(this.pointer, ULongVarOf<ULong>(this))
fun FloatByReference.toCPointer(): CPointer<FloatVar> = CPointer(this.pointer, FloatVarOf<Float>(this))
fun DoubleByReference.toCPointer(): CPointer<DoubleVar> = CPointer(this.pointer, DoubleVarOf<Double>(this))

fun <T : CPointed> Pointer.toCPointer(): CPointer<T> = CPointer(this)
fun Byte.toBoolean(): Boolean = this != 0.toByte()
fun Boolean.toByte(): Byte = (if (this) 1 else 0).toByte()
fun <V : CVariable> CValuesRef<V>.getJnaByReference(): ByReference? = (this as? CPointer)?.cVariable?.jnaByReference

actual interface CEnum {
    val value: Any
}

actual fun CPointer<ByteVar>.toKString(): String = this.jnaPointer.getString(0)
actual val String.cstr: CValues<ByteVar>
    get() = CString(Memory(this@cstr.length + 1.toLong()).apply { this.setString(0, this@cstr) })