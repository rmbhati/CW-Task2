package com.kgk.task2.utils

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.widget.TextView
import com.kgk.task2.R

class CustomProgress(context: Context?, theme: Int) :
    Dialog(context!!, theme) {
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (customProgress == null) {
            return
        }
    }

    companion object {
        private var customProgress: CustomProgress? = null
        fun createDialog(context: Context?, msg: String?): CustomProgress? {
            customProgress = CustomProgress(context, R.style.Dialog)
            customProgress!!.setContentView(R.layout.progress_custom)
            customProgress!!.window!!.attributes.gravity = Gravity.CENTER
            customProgress!!.setCancelable(false)
            customProgress!!.setCanceledOnTouchOutside(false)
            /*val loader = customProgress!!.findViewById<ImageView>(R.id.loader)
            Glide.with(context!!).load(R.drawable.loader).into(loader);*/
            val tvMsg = customProgress!!.findViewById<TextView>(R.id.msg)
            tvMsg.text = msg
            return customProgress
        }
    }
}