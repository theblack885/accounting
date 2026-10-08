package com.accountbook.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.accountbook.app.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccountTheme { AppRoot() }
        }
    }
}

@Composable
fun AppRoot(vm: MainViewModel = viewModel()) {
    val accounts by vm.accounts.collectAsState()
    val vouchers by vm.vouchers.collectAsState()
    val balances by vm.balances.collectAsState()
    val names = remember(accounts) { accounts.associate { it.id to it.name } }

    var tab by remember { mutableIntStateOf(0) }
    var showVoucher by remember { mutableStateOf(false) }
    var showAccount by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(tab == 0, { tab = 0 }, { Icon(Icons.Filled.Home, null) }, label = { Text("Home") })
                NavigationBarItem(tab == 1, { tab = 1 }, { Icon(Icons.Filled.Receipt, null) }, label = { Text("Entries") })
                NavigationBarItem(tab == 2, { tab = 2 }, { Icon(Icons.Filled.Group, null) }, label = { Text("Accounts") })
                NavigationBarItem(tab == 3, { tab = 3 }, { Icon(Icons.Filled.BarChart, null) }, label = { Text("Reports") })
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (tab == 2) showAccount = true else showVoucher = true },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text(if (tab == 2) "Account" else "Entry") }
            )
        }
    ) { pad ->
        when (tab) {
            0 -> HomeScreen(balances, vouchers, names, pad)
            1 -> VouchersScreen(vouchers, names, vm::deleteVoucher, pad)
            2 -> AccountsScreen(balances, vouchers, names, pad)
            else -> ReportsScreen(balances, pad)
        }
    }

    if (showVoucher) {
        AddVoucherDialog(accounts, { showVoucher = false }) { t, d, c, a, n ->
            vm.addVoucher(t, d, c, a, n); showVoucher = false
        }
    }
    if (showAccount) {
        AddAccountDialog({ showAccount = false }) { n, t, p, o ->
            vm.addAccount(n, t, p, o); showAccount = false
        }
    }
}
