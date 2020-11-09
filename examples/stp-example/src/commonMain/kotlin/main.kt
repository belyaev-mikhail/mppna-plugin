@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

import libstpMppna.*

fun main() {
    val width = 8
    val handle: VC? = vc_createValidityChecker()

    // Create variable "x"
    val x: Expr? = vc_varExpr(handle, "x", vc_bvType(handle, width))

    // Create bitvector x + x
    val xPlusx: Expr? = vc_bvPlusExpr(handle, width, x, x)

    // Create bitvector constant 2
    val two: Expr? = vc_bvConstExprFromInt(handle, width, 2U)

    // Create bitvector 2*x
    val xTimes2: Expr? = vc_bvMultExpr(handle, width, two, x)

    // Create bool expression x + x = 2*x
    val equality: Expr? = vc_eqExpr(handle, xPlusx , xTimes2)

    vc_assertFormula(handle, vc_trueExpr(handle))

    // We are asking STP: ∀ x. true → ( x + x = 2 * x )
    // This should be VALID.
    print("######First Query\n")
    handleQuery(handle, equality)

    // We are asking STP: ∀ x. true → ( x + x = 2 )
    // This should be INVALID.
    print("######Second Query\n")
    // Create bool expression x + x = 2
    val badEquality: Expr? = vc_eqExpr(handle, xPlusx , two)
    handleQuery(handle, badEquality)

    // Clean up
    vc_Destroy(handle)
}

fun handleQuery(handle: VC?, queryExpr: Expr?) {
    // Print the assertions
    print("Assertions:\n")
    vc_printAsserts(handle, 0)

    val result: Int = vc_query(handle, queryExpr)
    print("Query:\n")
    vc_printQuery(handle)
    when (result) {
        0 -> {
            print("Query is INVALID\n")

            // print counter example
            print("Counter example:\n")
            vc_printCounterExample(handle)
        }
        1 -> print("Query is VALID\n")
        2 -> print("Could not answer query\n")
        3 -> print("Timeout.\n")
        else -> print("Unhandled error\n")
    }
    print("\n\n");
}

