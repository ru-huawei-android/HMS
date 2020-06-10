package com.huawei.hms.sample2

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

fun log(any: Any?) = Log.d("WARNING", any.toString())

fun AppCompatActivity.toast(any: Any?) = Toast.makeText(this, any.toString(), Toast.LENGTH_SHORT).show()