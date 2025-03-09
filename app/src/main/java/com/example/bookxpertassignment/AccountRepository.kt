package com.example.bookxpertassignment

import com.example.bookxpertassignment.model.Account
import org.json.JSONArray


class AccountRepository {

    suspend fun fetchAccounts(): ArrayList<Account> {
        val call = RetrofitClient.instance.getAccounts()

        val response = call.execute()
        return if (response.isSuccessful) {
            val xmlString = response.body()

            val jsonString = xmlString
                ?.substringAfter(">")
                ?.substringBefore("</string>")
                ?: ""

            val jsonArray = JSONArray(jsonString)

            val accountList = ArrayList<Account>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                accountList.add(
                    Account(
                        ActName = obj.getString("ActName"),
                        actid = obj.getInt("actid")
                    )
                )
            }
            accountList
        } else {
            arrayListOf()
        }
    }
}

