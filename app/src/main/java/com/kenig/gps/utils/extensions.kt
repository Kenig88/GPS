package com.kenig.gps.utils

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.kenig.gps.R


//2

fun AppCompatActivity.openFragment(f: Fragment){
    if(supportFragmentManager.fragments.isNotEmpty()) {
        if(supportFragmentManager.fragments[0].javaClass == f.javaClass) return
    }
    supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        .replace(R.id.place_holder, f)
        .commit()
}

fun Fragment.openFragment(f: Fragment){
    (activity as AppCompatActivity).supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        .replace(R.id.place_holder, f)
        .commit()
}

fun Fragment.showToast(s: String){
    Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
}

fun Fragment.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        activity as AppCompatActivity, permission) == PackageManager.PERMISSION_GRANTED
}

