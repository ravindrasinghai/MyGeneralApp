package com.rwt.mygeneral.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rwt.mygeneral.data.ConfirmSignUpDataSource
import com.rwt.mygeneral.data.ConfirmSignUpRepository

/**
 * ViewModel provider factory to instantiate ConfirmSignUpViewModel.
 * Required given ConfirmSignUpViewModel has a non-empty constructor
 */
class ConfirmSignUpViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfirmSignUpViewModel::class.java)) {
            return ConfirmSignUpViewModel(
                confirmSignUpRepository = ConfirmSignUpRepository(
                    dataSource = ConfirmSignUpDataSource()
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}