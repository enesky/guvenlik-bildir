package com.enesky.guvenlikbildir.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsRequest

/**
 * Created by Enes Kamil YILMAZ on 06.02.2020
 */

fun Context.requireSendSmsPermission(function: () -> Any) = runWithPermissions(
    Manifest.permission.SEND_SMS,
    options = getQuickPermissionOptions()
) {
    Log.d("CalendarEventExtensions", "requireSendSmsPermission: Send Sms permission granted")
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
        function()
    }
}

fun Context.requireCallPhonePermission(function: () -> Any) = runWithPermissions(
    Manifest.permission.CALL_PHONE,
    options = getQuickPermissionOptions()
) {
    Log.d("CalendarEventExtensions", "requireCallPhonePermission: Call Phone permission granted")
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
        function()
    }
}

fun Context.requireReadContactsPermission(function: () -> Any) = runWithPermissions(
    Manifest.permission.READ_CONTACTS,
    options = getQuickPermissionOptions()
) {
    Log.d("CalendarEventExtensions", "requireReadContactsPermission: Read Contacts permission granted")
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
        function()
}

fun Context.requireLocationPermission(function: () -> Any) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { runWithPermissions(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        options = getQuickPermissionOptions()
    ) {
        Log.d("CalendarEventExtensions", "requireLocationPermission: Location permissions granted")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            function()
        }
    }
} else { //if("VERSION.SDK_INT < Q")
        runWithPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            options = getQuickPermissionOptions()
        ) {
            Log.d("CalendarEventExtensions", "requireLocationPermission: Location permissions granted")
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
    Log.d("CalendarEventExtensions", "requireAllPermissions: All permissions granted")
}

fun Context.getQuickPermissionOptions(): QuickPermissionsOptions {
    return QuickPermissionsOptions(
        rationaleMessage = "Izin alındı.",
        permanentlyDeniedMessage = "Izin reddedildi.",
        rationaleMethod = { rationaleCallback(it) },
        permanentDeniedMethod = { permissionsPermanentlyDenied(it) },
        permissionsDeniedMethod = { whenPermAreDenied(it) }
    )
}

fun Context.rationaleCallback(req: QuickPermissionsRequest) {
    // this will be called when permission is denied once or more time.
    MaterialAlertDialogBuilder(this)
        .setTitle("Reddettiğiniz izinler bulundu.")
        .setMessage("İlgili fonksiyonları kullanabilmeniz için izniniz gerekiyor.")
        .setPositiveButton("İste hadi") { _, _ -> req.proceed() }
        .setNegativeButton("İsteme") { _, _ -> req.cancel() }
        .setCancelable(false)
        .show()
}

fun Context.permissionsPermanentlyDenied(req: QuickPermissionsRequest) {
    // this will be called when some/all permissions required by the method are permanently denied.
    MaterialAlertDialogBuilder(this)
        .setTitle("İzinler tamamen reddedildi.")
        .setMessage(
            "İzinleri kabul edip ilgili fonksiyonları kullanabilmek için lütfen ayarlardaki izinler sekmesinde bulunan izinlere onay veriniz."
        )
        .setPositiveButton("Uygulama Ayarlarını Aç") { _, _ -> req.openAppSettings() }
        .setNegativeButton("İptal") { _, _ -> req.cancel() }
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