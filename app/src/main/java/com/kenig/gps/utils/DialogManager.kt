package com.kenig.gps.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.kenig.gps.R


object DialogManager { //7
    fun showLocEnabledDialog(context: Context, listener: Listener){ //7.1.1
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle(R.string.location_disabled) //(принимает сразу название ресурса)
        dialog.setMessage(context.getString(R.string.location_disabled_message)) //(не принимает название ресурса, нужно делать через Context)
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.yes_dialog)){
            _, _ -> listener.onClick() //7.1.2
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.no_dialog)){
            _, _ -> Toast.makeText(context, context.getString(R.string.no_toast), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show() //(нужно обязательно дописсать чтобы Dialog запустился)
    }

    interface Listener { //7.1(слушатель на позитивную кнопку)
        fun onClick()
    }

}