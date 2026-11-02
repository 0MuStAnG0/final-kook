package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        InvoiceEntity::class,
        SavedAddressEntity::class,
        AuditEventEntity::class,
        CartItemEntity::class,
        TryOnBookingEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class KookDatabase : RoomDatabase() {
    abstract fun invoiceDao(): InvoiceDao
    abstract fun savedAddressDao(): SavedAddressDao
    abstract fun auditEventDao(): AuditEventDao
    abstract fun cartDao(): CartDao
    abstract fun tryOnBookingDao(): TryOnBookingDao

    companion object {
        @Volatile
        private var INSTANCE: KookDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `try_on_bookings` (
                        `id` TEXT NOT NULL,
                        `storeName` TEXT NOT NULL,
                        `storeAvatar` TEXT NOT NULL,
                        `productTitle` TEXT NOT NULL,
                        `productImage` TEXT NOT NULL,
                        `scheduledDate` TEXT NOT NULL,
                        `scheduledTime` TEXT NOT NULL,
                        `bookingCode` TEXT NOT NULL,
                        `storeAddress` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `createdAtTimestamp` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): KookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KookDatabase::class.java,
                    "kook_market_database"
                )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

