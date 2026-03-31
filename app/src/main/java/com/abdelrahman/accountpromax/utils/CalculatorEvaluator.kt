package com.abdelrahman.accountpromax.utils

import javax.script.ScriptEngineManager

object CalculatorEvaluator {
    fun eval(expr: String): Double? {
        return runCatching {
            val engine = ScriptEngineManager().getEngineByName("rhino")
            (engine.eval(expr.replace("%", "/100")) as Number).toDouble()
        }.getOrNull()
    }
}
