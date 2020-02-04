package com.enesky.guvenlikbildir.ui.fragment.notify

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentNotifyBinding
import com.enesky.guvenlikbildir.extensions.Constants
import com.enesky.guvenlikbildir.extensions.getViewModel
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_notify.*

class NotifyFragment : BaseFragment() {

    private lateinit var binding: FragmentNotifyBinding
    private lateinit var notifyVM: NotifyVM
    private var phoneNumber: String = Constants.polis

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notify, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        notifyVM = getViewModel()
        binding.apply {
            viewModel = notifyVM
            lifecycleOwner = this@NotifyFragment
        }
        notifyVM.init(binding)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ll_polis.setOnClickListener {
            phoneNumber = Constants.polis
            checkPhoneCallPermission()
        }

        ll_yardım.setOnClickListener {
            phoneNumber = Constants.acilYardım
            checkPhoneCallPermission()
        }

        ll_iftaiye.setOnClickListener {
            phoneNumber = Constants.itfaiye
            checkPhoneCallPermission()
        }

        iv_safe.setOnClickListener {
            openInfoCountDownDialog("1")
        }

        iv_unsafe.setOnClickListener {
            openInfoCountDownDialog("1")
        }

    }

    fun checkPhoneCallPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.CALL_PHONE
                )) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CALL_PHONE),
                    42)
            }
        } else {
            // Permission has already been granted
            openInfoCountDownDialog(phoneNumber)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 42) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                openInfoCountDownDialog(phoneNumber)
            else
                // permission denied, boo! Disable the functionality
            return
        }
    }

}