package libstpMppna

expect fun vc_Destroy(vc: VC?): kotlin.Unit

expect fun vc_assertFormula(vc: VC?, e: Expr?): kotlin.Unit

expect fun vc_bvConstExprFromInt(vc: VC?, bitWidth: kotlin.Int, value: kotlin.UInt): Expr?

expect fun vc_bvMultExpr(vc: VC?, bitWidth: kotlin.Int, left: Expr?, right: Expr?): Expr?

expect fun vc_bvPlusExpr(vc: VC?, bitWidth: kotlin.Int, left: Expr?, right: Expr?): Expr?

expect fun vc_bvType(vc: VC?, no_bits: kotlin.Int): Type?

expect fun vc_createValidityChecker(): VC?

expect fun vc_eqExpr(vc: VC?, child0: Expr?, child1: Expr?): Expr?

expect fun vc_printAsserts(vc: VC?, simplify_print: kotlin.Int): kotlin.Unit

expect fun vc_printCounterExample(vc: VC?): kotlin.Unit

expect fun vc_printQuery(vc: VC?): kotlin.Unit

expect fun vc_query(vc: VC?, e: Expr?): kotlin.Int

expect fun vc_trueExpr(vc: VC?): Expr?

expect fun vc_varExpr(vc: VC?, name: String?, type: Type?): Expr?
