package mppna

actual typealias CPointed = kotlinx.cinterop.CPointed
actual typealias CValuesRef<T> = kotlinx.cinterop.CValuesRef<T>
actual typealias CVariable = kotlinx.cinterop.CVariable
actual typealias CPointerVarOf<T> = kotlinx.cinterop.CPointerVarOf<T>
actual typealias CPointer<T> = kotlinx.cinterop.CPointer<T>
actual typealias CPrimitiveVar = kotlinx.cinterop.CPrimitiveVar
actual typealias CStructVar = kotlinx.cinterop.CStructVar
//actual typealias CFunction<T> = kotlinx.cinterop.CFunction<T>

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

//actual typealias pthread_attr_t = libthread.pthread_attr_t

//actual fun pthread_join(__th: pthread_t, __thread_return: CValuesRef<CPointerVarOf<CPointer<out CPointed>>>) = libthread.pthread_join(__th, __thread_return)
//actual fun pthread_create(__newthread: CValuesRef<mppna.pthread_tVar>, __attr: CValuesRef<pthread_attr_t>?, __start_routine: mppna.mppna.CPointer<CFunction<(mppna.COpaquePointer?) -> mppna.COpaquePointer?>>?, __arg: CValuesRef<*>?): kotlin.Int = libthread.pthread_create(__newthread, __attr, __start_routine, __arg)