package com.accountbook.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.accountbook.app.data.Account
import com.accountbook.app.data.AccountBalance
import com.accountbook.app.data.Voucher
import com.accountbook.app.money

/* ---------------------------- HOME ---------------------------- */

@Composable
fun HomeScreen(b: List<AccountBalance>, vouchers: List<Voucher>, names: Map<Long, String>, pad: PaddingValues) {
    val cashBank = b.filter {
        it.type == "ASSET" && !it.isParty &&
            (it.name.contains("cash", true) || it.name.contains("bank", true))
    }.sumOf { it.net }
    val receivable = b.filter { it.type == "ASSET" && it.isParty }.sumOf { it.net }
    val payable = b.filter { it.type == "LIABILITY" && it.isParty }.sumOf { it.net }
    val income = b.filter { it.type == "INCOME" }.sumOf { it.net }
    val expense = b.filter { it.type == "EXPENSE" }.sumOf { it.net }
    val profit = income - expense

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(pad),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(
                    Brush.linearGradient(listOf(Color(0xFF4F6BFF), Color(0xFF8A5CFF), Color(0xFF19C3A6)))
                ).padding(24.dp)
            ) {
                Column {
                    Text("Cash + Bank", color = Color.White.copy(alpha = 0.8f))
                    Spacer(Modifier.height(6.dp))
                    Text(cashBank.money(), color = Color.White,
                        style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text("Profit: ${profit.money()}", color = Color.White.copy(alpha = 0.9f))
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Stat("Lena baki", receivable, Green, Modifier.weight(1f))
                Stat("Dena baki", payable, Red, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Stat("Income", income, Green, Modifier.weight(1f))
                Stat("Expense", expense, Red, Modifier.weight(1f))
            }
        }
        item { Text("Recent entries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        if (vouchers.isEmpty()) {
            item { Text("Abhi koi entry nahi. + dabakar pehli entry daalo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(vouchers.take(8), key = { it.id }) { VoucherCard(it, names, null) }
    }
}

/* -------------------------- VOUCHERS -------------------------- */

@Composable
fun VouchersScreen(vouchers: List<Voucher>, names: Map<Long, String>, onDelete: (Voucher) -> Unit, pad: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(pad),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Day Book", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        if (vouchers.isEmpty()) {
            item { Text("Koi voucher nahi hai.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(vouchers, key = { it.id }) { v -> VoucherCard(v, names) { onDelete(v) } }
    }
}

/* -------------------------- ACCOUNTS -------------------------- */

@Composable
fun AccountsScreen(b: List<AccountBalance>, vouchers: List<Voucher>, names: Map<Long, String>, pad: PaddingValues) {
    var ledger by remember { mutableStateOf<AccountBalance?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(pad),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("Accounts & Parties", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        items(b, key = { it.id }) { a ->
            Card(
                onClick = { ledger = a },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(a.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            a.type + if (a.isParty) " • Party" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(a.net.money(), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    ledger?.let { acc ->
        val entries = vouchers.filter { it.debitId == acc.id || it.creditId == acc.id }.sortedBy { it.date }
        AlertDialog(
            onDismissRequest = { ledger = null },
            confirmButton = { TextButton(onClick = { ledger = null }) { Text("Close") } },
            title = { Text("Ledger: ${acc.name}") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Line("Opening", acc.opening.money()) }
                    items(entries, key = { it.id }) { v ->
                        val isDebit = v.debitId == acc.id
                        val other = names[if (isDebit) v.creditId else v.debitId] ?: "?"
                        Line("${if (isDebit) "Dr" else "Cr"} • $other", v.amount.money())
                    }
                    item { HorizontalDivider() }
                    item { Line("Closing", acc.net.money(), bold = true) }
                }
            }
        )
    }
}

/* --------------------------- REPORTS -------------------------- */

@Composable
fun ReportsScreen(b: List<AccountBalance>, pad: PaddingValues) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Trial Balance", "Profit & Loss", "Balance Sheet")

    Column(Modifier.fillMaxSize().padding(pad)) {
        TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.background) {
            tabs.forEachIndexed { i, t ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) })
            }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            when (tab) {
                0 -> trialBalance(b)
                1 -> profitLoss(b)
                else -> balanceSheet(b)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.trialBalance(b: List<AccountBalance>) {
    var dr = 0.0
    var cr = 0.0
    item { Line("Account", "Debit  |  Credit", bold = true) }
    items(b, key = { it.id }) { a ->
        val d = a.drBalance
        Line(a.name, if (d >= 0) "${d.money()}  |  -" else "-  |  ${(-d).money()}")
    }
    b.forEach { if (it.drBalance >= 0) dr += it.drBalance else cr += -it.drBalance }
    item { HorizontalDivider() }
    item { Line("Total", "${dr.money()}  |  ${cr.money()}", bold = true) }
}

private fun androidx.compose.foundation.lazy.LazyListScope.profitLoss(b: List<AccountBalance>) {
    val inc = b.filter { it.type == "INCOME" }
    val exp = b.filter { it.type == "EXPENSE" }
    val totalInc = inc.sumOf { it.net }
    val totalExp = exp.sumOf { it.net }
    item { Text("Income", fontWeight = FontWeight.Bold) }
    items(inc, key = { it.id }) { Line(it.name, it.net.money()) }
    item { Line("Total Income", totalInc.money(), bold = true) }
    item { Spacer(Modifier.height(12.dp)); Text("Expenses", fontWeight = FontWeight.Bold) }
    items(exp, key = { it.id }) { Line(it.name, it.net.money()) }
    item { Line("Total Expenses", totalExp.money(), bold = true) }
    item { HorizontalDivider() }
    val p = totalInc - totalExp
    item { Line(if (p >= 0) "Net Profit" else "Net Loss", p.money(), bold = true) }
}

private fun androidx.compose.foundation.lazy.LazyListScope.balanceSheet(b: List<AccountBalance>) {
    val assets = b.filter { it.type == "ASSET" }
    val liab = b.filter { it.type == "LIABILITY" }
    val cap = b.filter { it.type == "CAPITAL" }
    val profit = b.filter { it.type == "INCOME" }.sumOf { it.net } - b.filter { it.type == "EXPENSE" }.sumOf { it.net }
    val totalAssets = assets.sumOf { it.net }
    val totalLeft = liab.sumOf { it.net } + cap.sumOf { it.net } + profit

    item { Text("Assets", fontWeight = FontWeight.Bold) }
    items(assets, key = { it.id }) { Line(it.name, it.net.money()) }
    item { Line("Total Assets", totalAssets.money(), bold = true) }
    item { Spacer(Modifier.height(12.dp)); Text("Liabilities", fontWeight = FontWeight.Bold) }
    items(liab, key = { it.id }) { Line(it.name, it.net.money()) }
    item { Spacer(Modifier.height(12.dp)); Text("Capital", fontWeight = FontWeight.Bold) }
    items(cap, key = { it.id }) { Line(it.name, it.net.money()) }
    item { Line(if (profit >= 0) "Current Profit" else "Current Loss", profit.money()) }
    item { HorizontalDivider() }
    item { Line("Total Liabilities + Capital", totalLeft.money(), bold = true) }
}

/* ---------------------------- DIALOGS ---------------------------- */

@Composable
fun AddAccountDialog(onDismiss: () -> Unit, onSave: (String, String, Boolean, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("ASSET") }
    var party by remember { mutableStateOf(false) }
    var opening by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Naya Account / Party") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Naam") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                Dropdown("Type", listOf("ASSET", "LIABILITY", "INCOME", "EXPENSE", "CAPITAL"), type, { it }) { type = it }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(party, { party = it })
                    Text("Customer / Supplier (Party)")
                }
                OutlinedTextField(opening, { opening = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Opening balance") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onSave(name, type, party, opening.toDoubleOrNull() ?: 0.0)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private val voucherTypes = listOf("Receipt", "Payment", "Sales", "Purchase", "Journal", "Contra")

private fun labelsFor(type: String): Pair<String, String> = when (type) {
    "Receipt" -> "Paisa kahan aaya (Debit)" to "Kisse mila (Credit)"
    "Payment" -> "Kisko diya (Debit)" to "Kahan se gaya (Credit)"
    "Sales" -> "Customer (Debit)" to "Sales account (Credit)"
    "Purchase" -> "Purchase account (Debit)" to "Supplier (Credit)"
    "Contra" -> "Cash/Bank me (Debit)" to "Cash/Bank se (Credit)"
    else -> "Debit account" to "Credit account"
}

@Composable
fun AddVoucherDialog(
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onSave: (type: String, debitId: Long, creditId: Long, amount: Double, narration: String) -> Unit
) {
    var type by remember { mutableStateOf("Receipt") }
    var debit by remember { mutableStateOf<Account?>(null) }
    var credit by remember { mutableStateOf<Account?>(null) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val (dLabel, cLabel) = labelsFor(type)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nayi Entry") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Dropdown("Voucher type", voucherTypes, type, { it }) {
                        type = it; debit = null; credit = null
                    }
                }
                item { Dropdown(dLabel, accounts, debit, { it.name }) { debit = it } }
                item { Dropdown(cLabel, accounts, credit, { it.name }) { credit = it } }
                item {
                    OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Amount") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(note, { note = it }, label = { Text("Narration (optional)") },
                        modifier = Modifier.fillMaxWidth())
                }
                if (error.isNotEmpty()) item { Text(error, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                when {
                    debit == null || credit == null -> error = "Dono account chuno"
                    debit!!.id == credit!!.id -> error = "Debit aur Credit alag hone chahiye"
                    amt <= 0 -> error = "Amount sahi daalo"
                    else -> onSave(type, debit!!.id, credit!!.id, amt, note)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
