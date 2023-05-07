package com.kenig.gps.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Toast
import com.kenig.gps.R
import com.kenig.gps.database.TrackItem
import com.kenig.gps.databinding.SaveDialogBinding


object DialogManager { //7
    fun showLocEnabledDialog(context: Context, listener: Listener){ //7.1.1
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle(R.string.location_disabled) //(принимает сразу название ресурса)
        dialog.setMessage(context.getString(R.string.location_disabled_message)) //(не принимает название ресурса, нужно делать через Context)
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.yes_dialog)){
            _, _ -> listener.onClick() //7.1.2
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.no_dialog)){
            _, _ -> Toast.makeText(context, context.getString(R.string.no_toast), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show() //(нужно обязательно дописать чтобы Dialog запустился)
    }

    fun showSaveDialog(context: Context, item: TrackItem?, listener: Listener){ //17
        val builder = AlertDialog.Builder(context)
        val binding = SaveDialogBinding.inflate(LayoutInflater.from(context), null, false)
        builder.setView(binding.root)
        val dialog = builder.create()
        binding.apply{
            tvTime.text = item?.time //18.1.1
            tvAvSpeed.text = item?.av_speed //18.1.2
            tvDistance.text = item?.distance // 18.1.3

            bSave.setOnClickListener{
                listener.onClick()
                dialog.dismiss()
            }
            bCancel.setOnClickListener{
                dialog.dismiss()
            }
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //(это чтобы убрать белое поле самого Диалога)
        dialog.show()
    }

    interface Listener { //7.1(слушатель на позитивную кнопку)
        fun onClick()
    }
}