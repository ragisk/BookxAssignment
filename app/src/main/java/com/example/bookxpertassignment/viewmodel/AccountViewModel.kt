package com.example.bookxpertassignment.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookxpertassignment.roomdb.AccountDatabase
import com.example.bookxpertassignment.roomdb.AccountEntityRepository
import com.example.bookxpertassignment.AccountRepository
import com.example.bookxpertassignment.AccountUtils
import com.example.bookxpertassignment.model.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountViewModel(private val context: Context) : ViewModel() {

    private val repository = AccountRepository()
    private lateinit var roomRepository : AccountEntityRepository

    private val _accounts = MutableLiveData<ArrayList<Account>>()
    val accounts: LiveData<ArrayList<Account>> get() = _accounts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun getAccountsFromApi() {
            viewModelScope.launch(Dispatchers.IO) {
                _isLoading.postValue(true)

                try {
                    val result = repository.fetchAccounts()
                    insertAll(result)
                    _accounts.postValue(result)
                } catch (e: Exception) {
                    e.printStackTrace()
                    _accounts.postValue(arrayListOf())
                } finally {
                    _isLoading.postValue(false)
                }
        }
    }

    fun insert(account: Account) = viewModelScope.launch {
        roomRepository.insert(account)
    }

    fun insertAll(accounts: ArrayList<Account>) = viewModelScope.launch(Dispatchers.IO) {
        roomRepository.insertAll(accounts)
    }

    fun update(account: Account) = viewModelScope.launch {
        roomRepository.update(account)
    }

    fun delete(account: Account) = viewModelScope.launch {
        roomRepository.delete(account)
    }

    fun getAllAccountsFromDB() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        roomRepository = AccountEntityRepository(AccountDatabase.getDatabase(context).accountDao())
        if(roomRepository.allAccounts.isNotEmpty()) {
            _accounts.postValue(roomRepository.allAccounts)
        }else{
            if(AccountUtils.isInternetAvailable(context)) {
                getAccountsFromApi()
            }else{
                withContext(Dispatchers.Main) {
                    AccountUtils.showNoInternetDialog(context)
                }
            }
        }
        _isLoading.postValue(false)
    }

    class AccountViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AccountViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}

