package libstpMppna

import mppna.toCPointer

actual fun vc_Destroy(vc: VC?): kotlin.Unit = StpLibrary.INSTANCE.vc_Destroy(vc?.jnaPointer)

actual fun vc_assertFormula(vc: VC?, e: Expr?): kotlin.Unit  = StpLibrary.INSTANCE.vc_assertFormula(vc?.jnaPointer, e?.jnaPointer)

actual fun vc_bvConstExprFromInt(vc: VC?, bitWidth: kotlin.Int, value: kotlin.UInt): Expr? = StpLibrary.INSTANCE.vc_bvConstExprFromInt(vc?.jnaPointer, bitWidth, value.toInt())?.toCPointer()

actual fun vc_bvMultExpr(vc: VC?, bitWidth: kotlin.Int, left: Expr?, right: Expr?): Expr? = StpLibrary.INSTANCE.vc_bvMultExpr(vc?.jnaPointer, bitWidth, left?.jnaPointer, right?.jnaPointer)?.toCPointer()

actual fun vc_bvPlusExpr(vc: VC?, bitWidth: kotlin.Int, left: Expr?, right: Expr?): Expr? = StpLibrary.INSTANCE.vc_bvPlusExpr(vc?.jnaPointer, bitWidth, left?.jnaPointer, right?.jnaPointer)?.toCPointer()

actual fun vc_bvType(vc: VC?, no_bits: kotlin.Int): Type? = StpLibrary.INSTANCE.vc_bvType(vc?.jnaPointer, no_bits)?.toCPointer()

actual fun vc_createValidityChecker(): VC? = StpLibrary.INSTANCE.vc_createValidityChecker()?.toCPointer()

actual fun vc_eqExpr(vc: VC?, child0: Expr?, child1: Expr?): Expr? = StpLibrary.INSTANCE.vc_eqExpr(vc?.jnaPointer, child0?.jnaPointer, child1?.jnaPointer)?.toCPointer()

actual fun vc_printAsserts(vc: VC?, simplify_print: kotlin.Int): kotlin.Unit = StpLibrary.INSTANCE.vc_printAsserts(vc?.jnaPointer, simplify_print)

actual fun vc_printCounterExample(vc: VC?): kotlin.Unit = StpLibrary.INSTANCE.vc_printCounterExample(vc?.jnaPointer)

actual fun vc_printQuery(vc: VC?): kotlin.Unit = StpLibrary.INSTANCE.vc_printQuery(vc?.jnaPointer)

actual fun vc_query(vc: VC?, e: Expr?): kotlin.Int = StpLibrary.INSTANCE.vc_query(vc?.jnaPointer, e?.jnaPointer)

actual fun vc_trueExpr(vc: VC?): Expr? = StpLibrary.INSTANCE.vc_trueExpr(vc?.jnaPointer)?.toCPointer()

actual fun vc_varExpr(vc: VC?, name: String?, type: Type?): Expr? = StpLibrary.INSTANCE.vc_varExpr(vc?.jnaPointer, name, type?.jnaPointer)?.toCPointer()
