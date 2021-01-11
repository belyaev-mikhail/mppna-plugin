package mppna

import kotlinx.cinterop.toKString
import kotlinx.cinterop.cstr

actual typealias CPointed = kotlinx.cinterop.CPointed
actual typealias CValuesRef<T> = kotlinx.cinterop.CValuesRef<T>
actual typealias CValues<T> = kotlinx.cinterop.CValues<T>
actual typealias CVariable = kotlinx.cinterop.CVariable
actual typealias CPointerVarOf<T> = kotlinx.cinterop.CPointerVarOf<T>
actual typealias CPointer<T> = kotlinx.cinterop.CPointer<T>
actual typealias CPrimitiveVar = kotlinx.cinterop.CPrimitiveVar
actual typealias CStructVar = kotlinx.cinterop.CStructVar

actual typealias BooleanVarOf<T> = kotlinx.cinterop.BooleanVarOf<T>
actual typealias ByteVarOf<T> = kotlinx.cinterop.ByteVarOf<T>
actual typealias ShortVarOf<T> = kotlinx.cinterop.ShortVarOf<T>
actual typealias IntVarOf<T> = kotlinx.cinterop.IntVarOf<T>
actual typealias LongVarOf<T> = kotlinx.cinterop.LongVarOf<T>
actual typealias UByteVarOf<T> = kotlinx.cinterop.UByteVarOf<T>
actual typealias UShortVarOf<T> = kotlinx.cinterop.UShortVarOf<T>
actual typealias UIntVarOf<T> = kotlinx.cinterop.UIntVarOf<T>
actual typealias ULongVarOf<T> = kotlinx.cinterop.ULongVarOf<T>
actual typealias FloatVarOf<T> = kotlinx.cinterop.FloatVarOf<T>
actual typealias DoubleVarOf<T> = kotlinx.cinterop.DoubleVarOf<T>

actual typealias CEnum = kotlinx.cinterop.CEnum

actual fun CPointer<ByteVar>.toKString(): String = this.toKString()
actual val String.cstr: CValues<ByteVar>
    get() = this.cstr