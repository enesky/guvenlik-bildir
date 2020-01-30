package com.enesky.guvenlikbildir.ui.activity

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.utils.NetworkStatusChangeListener

abstract class BaseActivity : AppCompatActivity(), NetworkStatusChangeListener {

    fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}