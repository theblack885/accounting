package com.accountbook.app.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/** type: ASSET, LIABILITY, INCOME, EXPENSE, CAPITAL */
@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val isParty: Boolean = false,   // customer / supplier
    val opening: Double = 0.0
)

/** Har voucher = ek debit account + ek credit account (double entry) */
@Entity(tableName = "vouchers")
data class Voucher(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val type: String,               // Receipt, Payment, Sales, Purchase, Journal, Contra
    val debitId: Long,
    val creditId: Long,
    val amount: Double,
    val narration: String = ""
)

data class AccountBalance(
    val id: Long,
    val name: String,
    val type: String,
    val isParty: Boolean,
    val opening: Double,
    val debit: Double,
    val credit: Double
) {
    val debitNatural: Boolean get() = type == "ASSET" || type == "EXPENSE"

    /** Account ka apni natural side par balance */
    val net: Double
        get() = if (debitNatural) opening + debit - credit else opening + credit - debit

    /** Positive = debit balance, negative = credit balance */
    val drBalance: Double get() = if (debitNatural) net else -net
}

@Dao
interface AppDao {
    @Insert suspend fun addAccount(a: Account): Long
    @Insert suspend fun addVoucher(v: Voucher)
    @Delete suspend fun deleteVoucher(v: Voucher)

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun accountCount(): Int

    @Query("SELECT * FROM accounts ORDER BY name")
    fun accounts(): Flow<List<Account>>

    @Query("SELECT * FROM vouchers ORDER BY date DESC, id DESC")
    fun vouchers(): Flow<List<Voucher>>

    @Query(
        """
        SELECT a.id AS id, a.name AS name, a.type AS type, a.isParty AS isParty,
               a.opening AS opening,
               COALESCE((SELECT SUM(amount) FROM vouchers WHERE debitId = a.id), 0) AS debit,
               COALESCE((SELECT SUM(amount) FROM vouchers WHERE creditId = a.id), 0) AS credit
        FROM accounts a ORDER BY a.name
        """
    )
    fun balances(): Flow<List<AccountBalance>>
}

@Database(entities = [Account::class, Voucher::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "accountbook.db"
                ).build().also { instance = it }
            }
    }
}
