package com.accountbook.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.accountbook.app.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).dao()

    val accounts = dao.accounts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val vouchers = dao.vouchers()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val balances = dao.balances()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            if (dao.accountCount() == 0) {
                listOf(
                    Account(name = "Cash", type = "ASSET"),
                    Account(name = "Bank", type = "ASSET"),
                    Account(name = "Sales", type = "INCOME"),
                    Account(name = "Purchase", type = "EXPENSE"),
                    Account(name = "Rent", type = "EXPENSE"),
                    Account(name = "Salary", type = "EXPENSE"),
                    Account(name = "Capital", type = "CAPITAL")
                ).forEach { dao.addAccount(it) }
            }
        }
    }

    fun addAccount(name: String, type: String, isParty: Boolean, opening: Double) {
        viewModelScope.launch {
            dao.addAccount(Account(name = name.trim(), type = type, isParty = isParty, opening = opening))
        }
    }

    fun addVoucher(type: String, debitId: Long, creditId: Long, amount: Double, narration: String) {
        viewModelScope.launch {
            dao.addVoucher(
                Voucher(
                    date = System.currentTimeMillis(),
                    type = type,
                    debitId = debitId,
                    creditId = creditId,
                    amount = amount,
                    narration = narration.trim()
                )
            )
        }
    }

    fun deleteVoucher(v: Voucher) {
        viewModelScope.launch { dao.deleteVoucher(v) }
    }
}
