package com.example.bookxpertassignment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.provider.Settings

class AccountUtils {
    companion object{
        const val BASE_URL = "https://fssservices.bookxpert.co/api/"
        const val PDF_URL = "https://fssservices.bookxpert.co/GeneratedPDF/Companies/nadc/2024-2025/BalanceSheet.pdf"
        fun isInternetAvailable(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

        fun showNoInternetDialog(context: Context) {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.no_internet))
                .setMessage(context.getString(R.string.internet_warn_message))
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.go_to_setting)) { dialog, _ ->
                    dialog.dismiss()
                    context.startActivity(Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS))
                }.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

    }
}