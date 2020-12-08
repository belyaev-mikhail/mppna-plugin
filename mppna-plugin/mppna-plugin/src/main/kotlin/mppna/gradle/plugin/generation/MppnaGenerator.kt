package mppna.gradle.plugin.generation

import kastree.ast.MutableVisitor
import kastree.ast.Node
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
    private val classes = mutableListOf<Node.Decl.Structured>()
    private val constants = mutableListOf<Node.Decl.Property>()

    // KN: typealias <-> C: typedef, enum
    private val typeAliases = mutableListOf<Node.Decl.TypeAlias>()
    private val libraryTypesToKotlinTypes = mutableMapOf<Node.TypeRef.Simple, Type>()

    init {
        declarations = Parser.parseFile(klibSourceCodeFile.readText()).decls
                .map { decl -> replacePackages(decl) }
                .map { decl -> removeAnnotations(decl) }

        for (decl in declarations) {
            when (decl) {
                is Node.Decl.Func -> functions.add(
                        decl.copy(
                                mods = filterModifiers(decl.mods, listOf(Node.Modifier.Keyword.EXTERNAL)),
                                body = null
                        )
                )
                is Node.Decl.Property -> {
                    if (decl.mods.any { it is Node.Modifier.Lit && it.keyword == Node.Modifier.Keyword.CONST })
                        constants.add(decl)
                }
                is Node.Decl.Structured -> classes.add(
                        decl.copy(members = emptyList())
                )
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
        for (function in functions) {
            commonDeclarations.add(function.copy(mods = function.mods + Node.Modifier.Lit(Node.Modifier.Keyword.EXPECT)))
        }
        return commonDeclarations
    }

    private fun generateNativeDeclarations(): List<Node.Decl> {
        val nativeDeclarations = mutableListOf<Node.Decl>()
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
        val arg = Node.ValueArg(name = null, asterisk = false, expr = Node.Expr.Name(name))
        val typeRef = type?.ref ?: return arg
        val (simple, isNullable) = getSimpleFromTypeRef(typeRef) ?: return arg
        if (getTypeFromSimple(simple)?.withJnaPointer == true) {
            val token = if (isNullable) Node.Expr.BinaryOp.Token.DOT_SAFE else Node.Expr.BinaryOp.Token.DOT
            return Node.ValueArg(name = null, asterisk = false, expr = Node.Expr.BinaryOp(
                    lhs = Node.Expr.Name(name),
                    oper = Node.Expr.BinaryOp.Oper.Token(token),
                    rhs = Node.Expr.Name(JNA_POINTER_PROPERTY_NAME)
            ))
        }
        return arg
    }

    private fun generateJvmDeclarations(): List<Node.Decl> {
        val jvmDeclarations = mutableListOf<Node.Decl>()
        val libraryInstance = Node.Expr.BinaryOp(
                lhs = Node.Expr.Name("${libraryC.capitalize()}Library"),
                oper = Node.Expr.BinaryOp.Oper.Token(Node.Expr.BinaryOp.Token.DOT),
                rhs = Node.Expr.Name("INSTANCE")
        )
        for (function in functions) {
            val functionName = function.name ?: continue
            val call = Node.Expr.Call(
                    expr = Node.Expr.Name(functionName),
                    args = function.params.map { it.toJnaArgument() },
                    typeArgs = emptyList(),
                    lambda = null
            )

            val (simple, isNullable) = function.type?.ref?.let { ref ->
                getSimpleFromTypeRef(ref)
            } ?: null to false
            val needToConvert = simple?.let { getTypeFromSimple(simple) }?.withJnaPointer == true
            val expr = Node.Expr.BinaryOp(
                    lhs = libraryInstance,
                    oper = Node.Expr.BinaryOp.Oper.Token(Node.Expr.BinaryOp.Token.DOT),
                    rhs = call
            )
            val body = Node.Decl.Func.Body.Expr(
                    expr = if (needToConvert) {
                        val token = if (isNullable) Node.Expr.BinaryOp.Token.DOT_SAFE else Node.Expr.BinaryOp.Token.DOT
                        val convertCall = Node.Expr.Call(
                                expr = Node.Expr.Name(CONVERT_FUNCTION_NAME),
                                args = emptyList(),
                                typeArgs = emptyList(),
                                lambda = null
                        )
                        Node.Expr.BinaryOp(
                                lhs = expr,
                                oper = Node.Expr.BinaryOp.Oper.Token(token),
                                rhs = convertCall
                        )
                    } else expr
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
        val nodeFiles = listOf(
                NodeFile(generateCommonDeclarations()) to targetsToSrcDirs.filterKeys { it == null }.values,
                NodeFile(generateNativeDeclarations()) to targetsToSrcDirs.filterKeys { it is KotlinNativeTargetWithHostTests }.values,
                NodeFile(generateJvmDeclarations()) to targetsToSrcDirs.filterKeys { it is KotlinJvmTarget }.values
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
    private fun NodeFile(decls: List<Node.Decl>): Node.File = Node.File(
            pkg = Node.Package(mods = emptyList(), names = listOf(packageName)),
            decls = decls,
            anns = emptyList(),
            imports = listOf(Node.Import(listOf("mppna"), wildcard = true, alias = null))
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
        const val MPPNA_PACKAGE_NAME = "mppna"
        const val JNA_POINTER_PROPERTY_NAME = "jnaPointer"
        const val CONVERT_FUNCTION_NAME = "toCPointer"
        val CINTEROP_PACKAGE_NAMES = listOf("kotlinx", "cinterop")
    }
}

