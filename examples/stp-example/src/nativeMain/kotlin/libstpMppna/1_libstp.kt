package libstpMppna

actual fun vc_Destroy(vc: VC?): kotlin.Unit = libstp.vc_Destroy(vc)

actual fun vc_assertFormula(vc: VC?, e: Expr?): kotlin.Unit = libstp.vc_assertFormula(vc, e)

actual fun vc_bvConstExprFromInt(vc: VC?, bitWidth: kotlin.Int, value: kotlin.UInt): Expr? = libstp.vc_bvConstExprFromInt(vc, bitWidth, value)

actual fun vc_bvMultExpr(vc: VC?, bitWidth: kotlin.Int, left: Expr?, right: Expr?): Expr? = libstp.vc_bvMultExpr(vc, bitWidth, left, right)

actual fun vc_bvPlusExpr(vc: VC?, bitWidth: kotlin.Int, left: Expr?, right: Expr?): Expr? = libstp.vc_bvPlusExpr(vc, bitWidth, left, right)

actual fun vc_bvType(vc: VC?, no_bits: kotlin.Int): Type? = libstp.vc_bvType(vc, no_bits)

actual fun vc_createValidityChecker(): VC? = libstp.vc_createValidityChecker()

actual fun vc_eqExpr(vc: VC?, child0: Expr?, child1: Expr?): Expr? = libstp.vc_eqExpr(vc, child0, child1)

actual fun vc_printAsserts(vc: VC?, simplify_print: kotlin.Int): kotlin.Unit = libstp.vc_printAsserts(vc, simplify_print)

actual fun vc_printCounterExample(vc: VC?): kotlin.Unit = libstp.vc_printCounterExample(vc)

actual fun vc_printQuery(vc: VC?): kotlin.Unit = libstp.vc_printQuery(vc)

actual fun vc_query(vc: VC?, e: Expr?): kotlin.Int = libstp.vc_query(vc, e)

actual fun vc_trueExpr(vc: VC?): Expr? = libstp.vc_trueExpr(vc)

actual fun vc_varExpr(vc: VC?, name: String?, type: Type?): Expr? = libstp.vc_varExpr(vc, name, type)

