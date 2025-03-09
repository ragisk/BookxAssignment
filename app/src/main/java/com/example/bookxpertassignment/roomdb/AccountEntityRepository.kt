package com.example.bookxpertassignment.roomdb

import com.example.bookxpertassignment.model.Account

class AccountEntityRepository(private val accountDao: AccountDao) {

        val allAccounts: ArrayList<Account> = ArrayList(accountDao.getAllAccounts())

        suspend fun insert(account: Account) {
            accountDao.insert(account)
        }

        suspend fun insertAll(accounts: ArrayList<Account>) {
            accountDao.insertAll(accounts)
        }

        suspend fun update(account: Account) {
            accountDao.update(account)
        }

        suspend fun delete(account: Account) {
            accountDao.delete(account)
        }


    }
