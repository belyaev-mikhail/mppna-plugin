package mppna.gradle.plugin.generation

import kastree.ast.Node

@Suppress("FunctionName")
fun Simple(vararg names: String): Node.TypeRef.Simple {
    return Node.TypeRef.Simple(names.map { name -> (Node.TypeRef.Simple.Piece(name, emptyList())) })
}

fun getSimpleFromTypeRef(typeRef: Node.TypeRef?): Pair<Node.TypeRef.Simple?, Boolean> {
    if (typeRef != null) {
        val simple = (if (typeRef is Node.TypeRef.Nullable) typeRef.type else typeRef) as? Node.TypeRef.Simple
        if (simple != null) {
            return simple to (typeRef is Node.TypeRef.Nullable)
        }
    }
    return null to false
}

data class ConversionInfo(val name: String = "", val usingCall: Boolean = true)

enum class Conversion(val to: ConversionInfo = ConversionInfo(), val from: ConversionInfo = ConversionInfo()) {
    NONE,
    JNA_POINTER_C_OPAQUE_POINTER(ConversionInfo("toCPointer"), ConversionInfo("jnaPointer", false)),
    JNA_BY_REFERENCE_C_POINTER(ConversionInfo("toCPointer"), ConversionInfo("getJnaByReference")),
    BYTE_BOOLEAN(ConversionInfo("toBoolean"), ConversionInfo("toByte")),
    BYTE_UBYTE(ConversionInfo("toUByte"), ConversionInfo("toByte")),
    SHORT_USHORT(ConversionInfo("toUShort"), ConversionInfo("toShort")),
    INT_UINT(ConversionInfo("toUInt"), ConversionInfo("toInt")),
    LONG_ULONG(ConversionInfo("toULong"), ConversionInfo("toLong"));
}

enum class Type(val value: Node.TypeRef.Simple, val conversion: Conversion = Conversion.NONE) {
    BYTE(Simple("Byte")),
    SHORT(Simple("Short")),
    INT(Simple("Int")),
    LONG(Simple("Long")),
    FLOAT(Simple("Float")),
    DOUBLE(Simple("Double")),
    CHAR(Simple("Char")),
    BOOLEAN(Simple("Boolean"), Conversion.BYTE_BOOLEAN),
    UBYTE(Simple("UByte"), Conversion.BYTE_UBYTE),
    USHORT(Simple("UShort"), Conversion.SHORT_USHORT),
    UINT(Simple("UInt"), Conversion.INT_UINT),
    ULONG(Simple("ULong"), Conversion.LONG_ULONG),
    STRING(Simple("String")),

    C_OPAQUE_POINTER(Simple("COpaquePointer"), Conversion.JNA_POINTER_C_OPAQUE_POINTER),
    C_VALUES_REF(Simple("CValuesRef"), Conversion.JNA_BY_REFERENCE_C_POINTER),
    C_POINTER(Simple("CPointer"), Conversion.JNA_BY_REFERENCE_C_POINTER),
    C_ARRAY_POINTER(Simple("CArrayPointer"), Conversion.JNA_BY_REFERENCE_C_POINTER);

    companion object {
        fun getByValue(value: Node.TypeRef.Simple) = values().firstOrNull { type ->
            type.value.pieces.map { it.name } == value.pieces.map { it.name }
        }
    }
}

fun jnaInterface(library: String) = "${library}Library"