@file:JvmName("TypesJvm")
@file:Suppress("EXPERIMENTAL_API_USAGE", "FINAL_UPPER_BOUND")

package mppna

import kotlin.jvm.JvmName

expect abstract class CPointed
expect abstract class CValuesRef<T : CPointed>
expect abstract class CVariable : CPointed
expect class CPointerVarOf<T : CPointer<*>> : CVariable
expect class CPointer<T : CPointed> : CValuesRef<T>
expect sealed class CPrimitiveVar : CVariable
expect abstract class CStructVar : CVariable

typealias COpaquePointer = CPointer<out CPointed>
typealias CArrayPointer<T> = CPointer<T>

expect class BooleanVarOf<T : Boolean> : CPrimitiveVar
expect class ByteVarOf<T : Byte> : CPrimitiveVar
expect class ShortVarOf<T : Short> : CPrimitiveVar
expect class IntVarOf<T : Int> : CPrimitiveVar
expect class LongVarOf<T : Long> : CPrimitiveVar
expect class UByteVarOf<T : UByte> : CPrimitiveVar
expect class UShortVarOf<T : UShort> : CPrimitiveVar
expect class UIntVarOf<T : UInt> : CPrimitiveVar
expect class ULongVarOf<T : ULong> : CPrimitiveVar
expect class FloatVarOf<T : Float> : CPrimitiveVar
expect class DoubleVarOf<T : Double> : CPrimitiveVar

typealias BooleanVar = BooleanVarOf<Boolean>
typealias ByteVar = ByteVarOf<Byte>
typealias ShortVar = ShortVarOf<Short>
typealias IntVar = IntVarOf<Int>
typealias LongVar = LongVarOf<Long>
typealias UByteVar = UByteVarOf<UByte>
typealias UShortVar = UShortVarOf<UShort>
typealias UIntVar = UIntVarOf<UInt>
typealias ULongVar = ULongVarOf<ULong>
typealias FloatVar = FloatVarOf<Float>
typealias DoubleVar = DoubleVarOf<Double>

typealias COpaquePointerVar = CPointerVarOf<COpaquePointer>