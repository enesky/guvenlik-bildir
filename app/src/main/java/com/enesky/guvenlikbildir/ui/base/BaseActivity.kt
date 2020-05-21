package com.enesky.guvenlikbildir.ui.base

import androidx.appcompat.app.AppCompatActivity
import pub.devrel.easypermissions.EasyPermissions

abstract class BaseActivity : AppCompatActivity() {

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}