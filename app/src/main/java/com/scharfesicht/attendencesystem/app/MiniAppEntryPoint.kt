package com.scharfesicht.attendencesystem.app

import android.content.Context
import android.content.Intent
import sa.gov.moi.absherinterior.core_logic.IAbsherHelper
import sa.gov.moi.absherinterior.core_logic.IMiniApp

class MiniAppEntryPoint : IMiniApp {

    override fun launch(context: Context, data: IAbsherHelper) {
        superData = data
        context.startActivity(Intent(context, MainActivity::class.java))
    }

    companion object {
        var superData: IAbsherHelper? = null
    }
}
