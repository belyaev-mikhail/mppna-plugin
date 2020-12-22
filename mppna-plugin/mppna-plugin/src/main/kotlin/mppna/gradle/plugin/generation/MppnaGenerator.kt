package mppna.gradle.plugin.generation

import kastree.ast.MutableVisitor
import kastree.ast.Node
import kastree.ast.Visitor
import kastree.ast.Writer
import kastree.ast.psi.Parser
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import java.io.File

class MppnaGenerator(val libraryC: String, val libraryKN: String, klibSourceCodeFile: File) {
    private val packageName = "$libraryKN${MPPNA_PACKAGE_NAME.capitalize()}"
    private val declarations: List<Node.Decl>
    private val functions = mutableListOf<Node.Decl.Func>()

    // KN: class <-> C: struct
    private val enums = mutableListOf<Node.Decl.Structured>()
    private val constants = mutableListOf<Node.Decl.Property>()

    // KN: typealias <-> C: typedef, enum
    private val typeAliases = mutableListOf<Node.Decl.TypeAlias>()
    private val libraryTypesToKotlinTypes = mutableMapOf<Node.TypeRef.Simple, Type>()
    private val libraryEnums = mutableListOf<String>()

    init {
        declarations = Parser.parseFile(klibSourceCodeFile.readText()).decls
                .map { decl -> replacePackages(decl) }
                .map { decl -> removeAnnotations(decl) }

        for (decl in declarations) {
            when (decl) {
                is Node.Decl.Func -> {
                    var ignoreDeclaration = false
                    Visitor.visit(decl) { v, _ ->
                        if (v is Node.TypeRef.Simple.Piece && v.name in IGNORE_LIST)
                            ignoreDeclaration = true
                    }
                    if (!ignoreDeclaration)
                        functions.add(decl.copy(
                                mods = filterModifiers(decl.mods, listOf(Node.Modifier.Keyword.EXTERNAL)),
                                body = null)
                        )
                }
                is Node.Decl.Property -> {
                    if (decl.mods.any { it is Node.Modifier.Lit && it.keyword == Node.Modifier.Keyword.CONST })
                        constants.add(decl)
                }
                is Node.Decl.Structured -> {
                    if (decl.form == Node.Decl.Structured.Form.ENUM_CLASS) {
                        enums.add(decl.copy(
                                members = decl.members.filterNot { member ->
                                    member is Node.Decl.Structured && member.form == Node.Decl.Structured.Form.CLASS
                                },
                                parents = listOf(Node.Decl.Structured.Parent.Type(type = Simple(MPPNA_C_ENUM_NAME), by = null)))
                        )
                        libraryEnums.add(decl.name)
                    }
                }
                is Node.Decl.TypeAlias -> {
                    typeAliases.add(decl)
                    (decl.type.ref as? Node.TypeRef.Simple)?.let { simple ->
                        Type.getByValue(simple)?.let { type ->
                            libraryTypesToKotlinTypes.put(Simple(decl.name), type)
                        }
                    }
                }
            }
        }
    }

    private fun filterModifiers(
            modifiers: List<Node.Modifier>,
            keywords: List<Node.Modifier.Keyword> = emptyList()
    ): List<Node.Modifier> = modifiers.filter { mod -> !(mod is Node.Modifier.Lit && mod.keyword in keywords) }

    private fun withoutAnnotations(mods: List<Node.Modifier>) = mods.filter { it !is Node.Modifier.AnnotationSet }

    private fun removeAnnotations(decl: Node.Decl): Node.Decl {
        if (decl is Node.WithModifiers) {
            val mods = withoutAnnotations(decl.mods)
            return when (decl) {
                is Node.Decl.Func -> {
                    val params = decl.params.map { param -> param.copy(mods = withoutAnnotations(param.mods)) }
                    decl.copy(mods, params = params)
                }
                is Node.Decl.Property -> decl.copy(mods = mods)
                is Node.Decl.Structured -> decl.copy(mods = mods)
                is Node.Decl.TypeAlias -> decl.copy(mods = mods)
                else -> decl
            }
        }
        return decl
    }

    private fun generateCommonDeclarations(): List<Node.Decl> {
        val commonDeclarations = mutableListOf<Node.Decl>()
        commonDeclarations.addAll(typeAliases)
        commonDeclarations.addAll(constants)
        for (enum in enums) {
            commonDeclarations.add(enum.copy(
                    mods = enum.mods + Node.Modifier.Lit(Node.Modifier.Keyword.EXPECT),
                    primaryConstructor = null,
                    members = enum.members.mapNotNull { entry ->
                        (entry as? Node.Decl.EnumEntry)?.copy(args = emptyList(), members = emptyList())
                    }
            ))
        }
        for (function in functions) {
            commonDeclarations.add(function.copy(mods = function.mods + Node.Modifier.Lit(Node.Modifier.Keyword.EXPECT)))
        }
        return commonDeclarations
    }

    private fun generateNativeDeclarations(): List<Node.Decl> {
        val nativeDeclarations = mutableListOf<Node.Decl>()
        for (enum in enums) {
            nativeDeclarations.add(Node.Decl.TypeAlias(mods = listOf(Node.Modifier.Lit(Node.Modifier.Keyword.ACTUAL)),
                    name = enum.name,
                    typeParams = emptyList(),
                    type = Node.Type(mods = emptyList(), ref = Simple(libraryKN, enum.name))))
        }
        for (function in functions) {
            val functionName = function.name ?: continue
            val call = Node.Expr.Call(
                    expr = Node.Expr.Name(functionName),
                    args = function.params.map { param ->
                        Node.ValueArg(
                                name = null,
                                asterisk = false,
                                expr = Node.Expr.Name(param.name)
                        )
                    },
                    typeArgs = emptyList(),
                    lambda = null
            )
            val body = Node.Decl.Func.Body.Expr(
                    expr = Node.Expr.BinaryOp(
                            lhs = Node.Expr.Name(libraryKN),
                            oper = Node.Expr.BinaryOp.Oper.Token(Node.Expr.BinaryOp.Token.DOT),
                            rhs = call
                    )
            )
            nativeDeclarations.add(
                    function.copy(
                            mods = function.mods + Node.Modifier.Lit(Node.Modifier.Keyword.ACTUAL),
                            body = body
                    )
            )
        }
        return nativeDeclarations
    }

    private fun getTypeFromSimple(simple: Node.TypeRef.Simple): Type? {
        return libraryTypesToKotlinTypes[simple] ?: Type.getByValue(simple)
    }

    private fun Node.Decl.Func.Param.toJnaArgument(): Node.ValueArg {
        return Node.ValueArg(
                name = null,
                asterisk = false,
                expr = getExpressionWithConvertCall(Node.Expr.Name(name), type?.ref, conversionTo = false)
        )
    }

    private fun valueToEnum(expr: Node.Expr, name: String, conversionTo: Boolean): Node.Expr {
        if (conversionTo) {
            val call = Node.Expr.Call(
                    expr = Node.Expr.Name("byValue"),
                    args = listOf(Node.ValueArg(name = null, asterisk = false, expr = expr)),
                    typeArgs = emptyList(),
                    lambda = null
            )
            return Node.Expr.BinaryOp(
                    lhs = Node.Expr.Name(name),
                    oper = Node.Expr.BinaryOp.Oper.Token(Node.Expr.BinaryOp.Token.DOT),
                    rhs = call
            )
        } else {
            return Node.Expr.BinaryOp(
                    lhs = expr,
                    oper = Node.Expr.BinaryOp.Oper.Token(Node.Expr.BinaryOp.Token.DOT),
                    rhs = Node.Expr.Name("value")
            )
        }
    }

    private fun getExpressionWithConvertCall(expr: Node.Expr, ref: Node.TypeRef?, conversionTo: Boolean): Node.Expr {
        val (simple, isNullable) = getSimpleFromTypeRef(ref)
        if (simple != null && simple.pieces.map { it.name }.first() in libraryEnums) {
            return valueToEnum(expr, simple.pieces.map { it.name }.first(), conversionTo)
        }
        val type: Type = simple?.let { getTypeFromSimple(it) } ?: return expr
        val convert = if (type.conversion != Conversion.NONE) {
            val conversionInfo = if (conversionTo) type.conversion.to else type.conversion.from
            val name = Node.Expr.Name(conversionInfo.name)
            if (conversionInfo.usingCall)
                Node.Expr.Call(
                        expr = name,
                        args = emptyList(),
                        typeArgs = emptyList(),
                        lambda = null
                ) else name
        } else null
        val convertWithCast = if (type.conversion == Conversion.JNA_BY_REFERENCE_C_POINTER && !conversionTo) {
            castJnaByReference(convert, simple, isNullable)
        } else convert
        return if (convertWithCast != null) {
            val token = if (isNullable) Node.Expr.BinaryOp.Token.DOT_SAFE else Node.Expr.BinaryOp.Token.DOT
            Node.Expr.BinaryOp(
                    lhs = expr,
                    oper = Node.Expr.BinaryOp.Oper.Token(token),
                    rhs = convertWithCast
            )
        } else expr
    }

    private fun castJnaByReference(expr: Node.Expr?, simple: Node.TypeRef.Simple, isNullable: Boolean): Node.Expr? {
        val typeParam = simple.pieces.first().typeParams.firstOrNull()
        val simpleTypeParam = getSimpleFromTypeRef(typeParam?.ref).first ?: return expr
        val type = when (simpleTypeParam.pieces.first().name) {
            "BooleanVar" -> "ByteByReference"
            "ByteVar" -> "ByteByReference"
            "ShortVar" -> "ShortByReference"
            "IntVar" -> "IntByReference"
            "LongVar" -> "LongByReference"
            "UByteVar" -> "ByteByReference"
            "UShortVar" -> "ShortByReference"
            "UIntVar" -> "IntByReference"
            "ULongVar" -> "NativeLongByReference"
            "FloatVar" -> "FloatByReference"
            "DoubleVar" -> "DoubleByReference"
            else -> "PointerByReference"
        }
        val token = if (isNullable) Node.Expr.TypeOp.Token.AS_SAFE else Node.Expr.TypeOp.Token.AS
        return expr?.let {
            Node.Expr.TypeOp(
                    lhs = expr,
                    oper = Node.Expr.TypeOp.Oper(token),
                    rhs = Node.Type(mods = emptyList(), ref = Simple(type))
            )
        }
    }

    private fun generateJvmDeclarations(): List<Node.Decl> {
        val jvmDeclarations = mutableListOf<Node.Decl>()
        val libraryInstance = Node.Expr.BinaryOp(
                lhs = Node.Expr.Name("${libraryC.capitalize()}Library"),
                oper = Node.Expr.BinaryOp.Oper.Token(Node.Expr.BinaryOp.Token.DOT),
                rhs = Node.Expr.Name("INSTANCE")
        )
        for (enum in enums) {
            jvmDeclarations.add(MutableVisitor.preVisit(enum) { v, _ ->
                when (v) {
                    is Node.Expr.Const -> {
                        val newValue = v.value.removeSuffix("u")
                        if (newValue.isNotEmpty() && newValue.all { it.isDigit() }) v.copy(value = newValue)
                        else v
                    }
                    is Node.TypeRef -> {
                        val simple = getSimpleFromTypeRef(v).first
                        if (simple != null && Type.getByValue(simple) == Type.UINT) {
                            Type.INT.value
                        } else v
                    }
                    else -> v
                }
            }.copy(mods = enum.mods + listOf(Node.Modifier.Lit(Node.Modifier.Keyword.ACTUAL))))
        }
        for (function in functions) {
            val functionName = function.name ?: continue
            val call = Node.Expr.Call(
                    expr = Node.Expr.Name(functionName),
                    args = function.params.map { it.toJnaArgument() },
                    typeArgs = emptyList(),
                    lambda = null
            )
            val body = Node.Decl.Func.Body.Expr(
                    getExpressionWithConvertCall(
                            expr = Node.Expr.BinaryOp(
                                    lhs = libraryInstance,
                                    oper = Node.Expr.BinaryOp.Oper.Token(Node.Expr.BinaryOp.Token.DOT),
                                    rhs = call
                            ),
                            ref = function.type?.ref,
                            conversionTo = true
                    )
            )
            jvmDeclarations.add(
                    function.copy(
                            mods = function.mods + Node.Modifier.Lit(Node.Modifier.Keyword.ACTUAL),
                            body = body
                    )
            )
        }
        return jvmDeclarations
    }

    fun generateDeclarations(targetsToSrcDirs: Map<KotlinTarget?, File>) {
        val jvmImports = listOf(Node.Import(JNA_PTR_PACKAGE_NAMES, wildcard = true, alias = null))
        val nodeFiles = listOf(
                NodeFile(generateCommonDeclarations()) to targetsToSrcDirs.filterKeys { it == null }.values,
                NodeFile(generateNativeDeclarations()) to targetsToSrcDirs.filterKeys { it is KotlinNativeTargetWithHostTests }.values,
                NodeFile(generateJvmDeclarations(), imports = jvmImports) to targetsToSrcDirs.filterKeys { it is KotlinJvmTarget }.values
        )
        for ((nodeFile, dirs) in nodeFiles) {
            for (dir in dirs) {
                val packageDir = dir.resolve(packageName)
                packageDir.mkdirs()
                val file = File(packageDir, "${libraryKN}${dir.name.capitalize()}.kt")
                if (!file.exists()) file.createNewFile()
                file.writeText(Writer.write(nodeFile))
            }
        }
    }

    @Suppress("FunctionName")
    private fun NodeFile(decls: List<Node.Decl>, imports: List<Node.Import> = emptyList()): Node.File = Node.File(
            pkg = Node.Package(mods = emptyList(), names = listOf(packageName)),
            decls = decls,
            anns = emptyList(),
            imports = imports + listOf(Node.Import(listOf(MPPNA_PACKAGE_NAME), wildcard = true, alias = null))
    )

    private fun replacePackages(decl: Node.Decl): Node.Decl = MutableVisitor.preVisit(decl) { v, _ ->
        if (v is Node.TypeRef.Simple) {
            val newPieces = mutableListOf<Node.TypeRef.Simple.Piece>()
            var i = 0
            while (i < v.pieces.size) {
                if (v.pieces[i].name == libraryKN) {
                    newPieces.add(
                            Node.TypeRef.Simple.Piece(
                                    name = packageName,
                                    typeParams = emptyList()
                            )
                    )
                } else if (v.pieces[i].name == CINTEROP_PACKAGE_NAMES[0]
                        && v.pieces.subList(i, i + CINTEROP_PACKAGE_NAMES.size)
                                .map { it.name } == CINTEROP_PACKAGE_NAMES
                ) {
                    newPieces.add(
                            Node.TypeRef.Simple.Piece(
                                    name = MPPNA_PACKAGE_NAME,
                                    typeParams = emptyList()
                            )
                    )
                    i += CINTEROP_PACKAGE_NAMES.size
                    continue
                } else {
                    newPieces.add(v.pieces[i])
                }
                i++
            }
            Node.TypeRef.Simple(newPieces)
        } else v
    }

    companion object {
        const val MPPNA_C_ENUM_NAME = "CEnum"
        const val MPPNA_PACKAGE_NAME = "mppna"
        val CINTEROP_PACKAGE_NAMES = listOf("kotlinx", "cinterop")
        val JNA_PTR_PACKAGE_NAMES = listOf("com", "sun", "jna", "ptr")
        val IGNORE_LIST = listOf("CFunction")
    }
}

