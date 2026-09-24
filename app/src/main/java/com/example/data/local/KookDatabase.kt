package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [InvoiceEntity::class, SavedAddressEntity::class, AuditEventEntity::class, CartItemEntity::class],
    version = 2,
    exportSchema = false
)
abstract class KookDatabase : RoomDatabase() {
    abstract fun invoiceDao(): InvoiceDao
    abstract fun savedAddressDao(): SavedAddressDao
    abstract fun auditEventDao(): AuditEventDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var INSTANCE: KookDatabase? = null

        fun getDatabase(context: Context): KookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KookDatabase::class.java,
                    "kook_market_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
