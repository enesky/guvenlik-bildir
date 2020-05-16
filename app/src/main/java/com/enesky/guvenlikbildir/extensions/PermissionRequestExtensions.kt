package com.enesky.guvenlikbildir.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.enesky.guvenlikbildir.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsRequest
import timber.log.Timber

/**
 * Created by Enes Kamil YILMAZ on 06.02.2020
 */

fun Context.requireSendSmsPermission(function: () -> Any) = runWithPermissions(
    Manifest.permission.SEND_SMS,
    options = getQuickPermissionOptions()
) {
    Timber.tag("PermissionRequestExtension").d("requireSendSmsPermission: Send Sms permission granted")
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        function()
    }
}

fun Context.requireCallPhonePermission(function: () -> Any) = runWithPermissions(
    Manifest.permission.CALL_PHONE,
    options = getQuickPermissionOptions()
) {
    Timber.tag("PermissionRequestExtension").d("requireCallPhonePermission: Call Phone permission granted")
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
    ) {
        function()
    }
}

fun Context.requireReadContactsPermission(function: () -> Any) = runWithPermissions(
    Manifest.permission.READ_CONTACTS,
    options = getQuickPermissionOptions()
) {
    Timber.tag("PermissionRequestExtension").d("requireReadContactsPermission: Read Contacts permission granted")
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    )
        function()
}

fun Context.requireLocationPermission(function: () -> Any) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { runWithPermissions(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        options = getQuickPermissionOptions()
    ) {
        Timber.tag("PermissionRequestExtension")
            .d("requireLocationPermission: Location permissions granted")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            function()
        }
    }
} else { //if("VERSION.SDK_INT < Q")
        runWithPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            options = getQuickPermissionOptions()
        ) {
            Timber.tag("PermissionRequestExtension").d("requireLocationPermission: Location permissions granted")
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                function()
            }
        }
}

fun Context.requireAllPermissions() = runWithPermissions(
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.CALL_PHONE,
    Manifest.permission.SEND_SMS,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION, options = getQuickPermissionOptions()
) {
    Timber.tag("PermissionRequestExtension").d("requireAllPermissions: All permissions granted")
}

fun Context.getQuickPermissionOptions(): QuickPermissionsOptions {
    return QuickPermissionsOptions(
        rationaleMessage = getString(R.string.label_permission_granted),
        permanentlyDeniedMessage = getString(R.string.label_permission_denied),
        rationaleMethod = { rationaleCallback(it) },
        permanentDeniedMethod = { permissionsPermanentlyDenied(it) },
        permissionsDeniedMethod = { whenPermAreDenied(it) }
    )
}

fun Context.rationaleCallback(req: QuickPermissionsRequest) {
    // this will be called when permission is denied once or more time.
    MaterialAlertDialogBuilder(this)
        .setTitle(getString(R.string.label_denied_permission_found))
        .setMessage(getString(R.string.label_require_permission))
        .setPositiveButton(getString(R.string.label_grant_permission)) { _, _ -> req.proceed() }
        .setNegativeButton(getString(R.string.label_deny_permission)) { _, _ -> req.cancel() }
        .setCancelable(false)
        .show()
}

fun Context.permissionsPermanentlyDenied(req: QuickPermissionsRequest) {
    // this will be called when some/all permissions required by the method are permanently denied.
    MaterialAlertDialogBuilder(this)
        .setTitle(getString(R.string.label_all_permissions_granted))
        .setMessage(getString(R.string.label_pls_grant_permission))
        .setPositiveButton(getString(R.string.label_open_app_settings)) { _, _ -> req.openAppSettings() }
        .setNegativeButton(getString(R.string.label_cancel)) { _, _ -> req.cancel() }
        .setCancelable(false)
        .show()
}

fun Context.whenPermAreDenied(req: QuickPermissionsRequest) {
    // handle something when permissions are not granted and the request method cannot be called
    /*MaterialAlertDialogBuilder(this)
        .setTitle("İzinleri reddettiniz.")
        .setMessage("İzinlerin ${req.deniedPermissions.size}/${req.permissions.size} 'i reddedildi.\n" +
                    "Bazı fonksiyonlardan mahrum kalacaksınız :/" )
        .setPositiveButton("Tamam") { _, _ -> }
        .setCancelable(false)
        .show()*/
}