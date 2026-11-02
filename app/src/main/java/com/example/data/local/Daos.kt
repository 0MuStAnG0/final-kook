package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY createdAtTimestamp DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: String): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Query("UPDATE invoices SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: com.example.data.InvoiceStatus)

    @Query("UPDATE invoices SET trackingCode = :code, courierNote = :note, status = :status WHERE id = :id")
    suspend fun updateTracking(id: String, code: String, note: String?, status: com.example.data.InvoiceStatus)
}

@Dao
interface SavedAddressDao {
    @Query("SELECT * FROM saved_addresses ORDER BY isDefault DESC, id DESC")
    fun getAllAddresses(): Flow<List<SavedAddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: SavedAddressEntity)

    @Query("DELETE FROM saved_addresses WHERE id = :id")
    suspend fun deleteAddress(id: Long)
}

@Dao
interface AuditEventDao {
    @Query("SELECT * FROM audit_events WHERE entityId = :entityId ORDER BY timestamp DESC")
    fun getEventsForEntity(entityId: String): Flow<List<AuditEventEntity>>

    @Insert
    suspend fun logEvent(event: AuditEventEntity)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY addedAtTimestamp DESC")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :qty WHERE productId = :productId")
    suspend fun updateQuantity(productId: String, qty: Int)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItem(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface TryOnBookingDao {
    @Query("SELECT * FROM try_on_bookings ORDER BY createdAtTimestamp DESC")
    fun getAllBookings(): Flow<List<TryOnBookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: TryOnBookingEntity)

    @Query("DELETE FROM try_on_bookings WHERE id = :id")
    suspend fun deleteBooking(id: String)
}


