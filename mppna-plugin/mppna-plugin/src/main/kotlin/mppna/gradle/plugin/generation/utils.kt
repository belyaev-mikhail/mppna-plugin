package mppna.gradle.plugin.generation

import kastree.ast.Node

@Suppress("FunctionName")
fun Simple(vararg names: String): Node.TypeRef.Simple {
    return Node.TypeRef.Simple(names.map { name -> (Node.TypeRef.Simple.Piece(name, emptyList())) })
}

fun getSimpleFromTypeRef(typeRef: Node.TypeRef): Pair<Node.TypeRef.Simple, Boolean>? {
    val simple = (if (typeRef is Node.TypeRef.Nullable) typeRef.type else typeRef) as? Node.TypeRef.Simple
    if (simple != null) {
        return simple to (typeRef is Node.TypeRef.Nullable)
    }
    return null
}

enum class Type(val value: Node.TypeRef.Simple, val withJnaPointer: Boolean = false) {
    BYTE(Simple("Byte")),
    SHORT(Simple("Short")),
    INT(Simple("Int")),
    LONG(Simple("Long")),
    FLOAT(Simple("Float")),
    DOUBLE(Simple("Double")),
    CHAR(Simple("Char")),
    BOOLEAN(Simple("Boolean")),
    UBYTE(Simple("UByte")),
    USHORT(Simple("UShort")),
    UINT(Simple("UInt")),
    ULONG(Simple("ULong")),
    STRING(Simple("String")),

    C_OPAQUE_POINTER(Simple("COpaquePointer"), withJnaPointer = true),
    C_POINTER(Simple("CPointer"), withJnaPointer = true),
    C_ARRAY_POINTER(Simple("CArrayPointer"), withJnaPointer = true);

    companion object {
        fun getByValue(value: Node.TypeRef.Simple) = values().firstOrNull { type -> type.value.pieces == value.pieces }
    }
}

fun jnaInterface(library: String) = "${library}Library"