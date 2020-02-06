package com.enesky.guvenlikbildir.extensions

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsRequest

/**
 * Created by Enes Kamil YILMAZ on 06.02.2020
 */

fun Context.requireSendSmsPermission() = runWithPermissions(
    Manifest.permission.SEND_SMS,
    options = getQuickPermissionOptions()
) {
    Log.d("CalendarEventExtensions", "requireSendSmsPermission: Send Sms permission granted")
}

fun Context.requireCallPhonePermission() = runWithPermissions(
    Manifest.permission.CALL_PHONE,
    options = getQuickPermissionOptions()
) {
    Log.d("CalendarEventExtensions", "requireCallPhonePermission: Call Phone permission granted")
}

fun Context.requireReadContactsPermission() = runWithPermissions(
    Manifest.permission.READ_CONTACTS,
    options = getQuickPermissionOptions()
) {
    Log.d("CalendarEventExtensions", "requireReadContactsPermission: Read Contacts permission granted")
}

fun Context.requireAllPermissions() = runWithPermissions(
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.CALL_PHONE,
    Manifest.permission.SEND_SMS, options = getQuickPermissionOptions()
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
    MaterialAlertDialogBuilder(this)
        .setTitle("İzinleri reddettiniz.")
        .setMessage("İzinlerin ${req.deniedPermissions.size}/${req.permissions.size} 'i reddedildi.\n" +
                    "Bazı fonksiyonlardan mahrum kalacaksınız :/" )
        .setPositiveButton("Tamam") { _, _ -> }
        .setCancelable(false)
        .show()
}