package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.model.Collector
import com.example.data.model.CollectorDepositLog
import com.example.data.model.SubscriptionCustomer
import com.example.data.model.SubscriptionPayment
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class FirebaseSyncConfig(
    val isEnabled: Boolean = false,
    val projectId: String = "",
    val apiKey: String = "",
    val applicationId: String = "",
    val storeId: String = ""
)

object DynamicFirebaseManager {
    private const val TAG = "DynamicFirebaseManager"
    private const val PREFS_NAME = "firebase_sync_prefs"
    private const val KEY_ENABLED = "sync_enabled"
    private const val KEY_PROJECT_ID = "project_id"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_APP_ID = "app_id"
    private const val KEY_STORE_ID = "store_id"
    private const val KEY_ACTIVE_ROLE = "active_role" // "ADMIN" or "COLLECTOR"
    private const val KEY_COLLECTOR_ID = "collector_id"
    private const val KEY_COLLECTOR_NAME = "collector_name"

    private const val DYNAMIC_APP_NAME = "TokoPosDynamicFirebase"

    private var customerListener: ListenerRegistration? = null
    private var collectorListener: ListenerRegistration? = null

    fun getSyncConfig(context: Context): FirebaseSyncConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return FirebaseSyncConfig(
            isEnabled = prefs.getBoolean(KEY_ENABLED, false),
            projectId = prefs.getString(KEY_PROJECT_ID, "") ?: "",
            apiKey = prefs.getString(KEY_API_KEY, "") ?: "",
            applicationId = prefs.getString(KEY_APP_ID, "") ?: "",
            storeId = prefs.getString(KEY_STORE_ID, "") ?: ""
        )
    }

    fun saveSyncConfig(context: Context, config: FirebaseSyncConfig) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ENABLED, config.isEnabled)
            .putString(KEY_PROJECT_ID, config.projectId.trim())
            .putString(KEY_API_KEY, config.apiKey.trim())
            .putString(KEY_APP_ID, config.applicationId.trim())
            .putString(KEY_STORE_ID, config.storeId.trim().ifEmpty { config.projectId.trim() })
            .apply()
        
        // Reinitialize instance if enabled
        if (config.isEnabled && config.projectId.isNotEmpty() && config.apiKey.isNotEmpty()) {
            initFirebaseApp(context, config)
        }
    }

    fun getRoleInfo(context: Context): Pair<String, Pair<String, String>> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val role = prefs.getString(KEY_ACTIVE_ROLE, "ADMIN") ?: "ADMIN"
        val collectorId = prefs.getString(KEY_COLLECTOR_ID, "") ?: ""
        val collectorName = prefs.getString(KEY_COLLECTOR_NAME, "") ?: ""
        return Pair(role, Pair(collectorId, collectorName))
    }

    fun saveRoleCollector(context: Context, collectorId: String, collectorName: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_ACTIVE_ROLE, "COLLECTOR")
            .putString(KEY_COLLECTOR_ID, collectorId)
            .putString(KEY_COLLECTOR_NAME, collectorName)
            .apply()
    }

    fun resetToAdminRole(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_ACTIVE_ROLE, "ADMIN")
            .remove(KEY_COLLECTOR_ID)
            .remove(KEY_COLLECTOR_NAME)
            .apply()
    }

    @Synchronized
    fun getFirestore(context: Context): FirebaseFirestore? {
        val config = getSyncConfig(context)
        if (!config.isEnabled || config.projectId.isEmpty() || config.apiKey.isEmpty()) {
            return null
        }
        val app = initFirebaseApp(context, config) ?: return null
        return try {
            val firestore = FirebaseFirestore.getInstance(app)
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            firestore.firestoreSettings = settings
            firestore
        } catch (e: Exception) {
            Log.e(TAG, "Error getting firestore instance", e)
            null
        }
    }

    private fun initFirebaseApp(context: Context, config: FirebaseSyncConfig): FirebaseApp? {
        return try {
            val existing = FirebaseApp.getApps(context).find { it.name == DYNAMIC_APP_NAME }
            if (existing != null) {
                return existing
            }
            val options = FirebaseOptions.Builder()
                .setProjectId(config.projectId)
                .setApiKey(config.apiKey)
                .setApplicationId(if (config.applicationId.isNotEmpty()) config.applicationId else "1:100000000000:android:tokopos")
                .build()
            FirebaseApp.initializeApp(context, options, DYNAMIC_APP_NAME)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing dynamic FirebaseApp", e)
            null
        }
    }

    // --- Realtime Firestore Sync Methods ---

    /**
     * Upload or update a customer on Firestore
     */
    suspend fun syncCustomerToCloud(context: Context, customer: SubscriptionCustomer) {
        val firestore = getFirestore(context) ?: return
        val storeId = getSyncConfig(context).storeId
        try {
            val docRef = firestore.collection("stores").document(storeId)
                .collection("subscription_customers").document(customer.id.toString())
            
            val data = mapOf(
                "id" to customer.id,
                "customerCode" to customer.customerCode,
                "name" to customer.name,
                "phone" to customer.phone,
                "address" to customer.address,
                "packageId" to customer.packageId,
                "packageName" to customer.packageName,
                "monthlyPrice" to customer.monthlyPrice,
                "installationDate" to customer.installationDate,
                "dueDay" to customer.dueDay,
                "paidUntilTimestamp" to customer.paidUntilTimestamp,
                "status" to customer.status,
                "notes" to customer.notes,
                "updatedAt" to System.currentTimeMillis()
            )
            docRef.set(data, SetOptions.merge()).await()
            Log.d(TAG, "Successfully synced customer ${customer.name} to cloud")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync customer to cloud", e)
        }
    }

    /**
     * Delete customer from cloud
     */
    suspend fun deleteCustomerFromCloud(context: Context, customerId: Long) {
        val firestore = getFirestore(context) ?: return
        val storeId = getSyncConfig(context).storeId
        try {
            firestore.collection("stores").document(storeId)
                .collection("subscription_customers").document(customerId.toString())
                .delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete customer from cloud", e)
        }
    }

    /**
     * Sync collector to cloud
     */
    suspend fun syncCollectorToCloud(context: Context, collector: Collector) {
        val firestore = getFirestore(context) ?: return
        val storeId = getSyncConfig(context).storeId
        try {
            val docRef = firestore.collection("stores").document(storeId)
                .collection("collectors").document(collector.id)
            docRef.set(collector, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync collector to cloud", e)
        }
    }

    /**
     * Sync payment and deposit log to cloud
     */
    suspend fun syncDepositLogToCloud(context: Context, log: CollectorDepositLog, payment: SubscriptionPayment, newPaidUntil: Long) {
        val firestore = getFirestore(context) ?: return
        val storeId = getSyncConfig(context).storeId
        try {
            // 1. Add deposit log
            firestore.collection("stores").document(storeId)
                .collection("collector_deposit_logs").document(log.id)
                .set(log, SetOptions.merge()).await()

            // 2. Add payment record
            firestore.collection("stores").document(storeId)
                .collection("subscription_payments").document(payment.invoiceNumber)
                .set(payment, SetOptions.merge()).await()

            // 3. Update customer paidUntil
            firestore.collection("stores").document(storeId)
                .collection("subscription_customers").document(log.customerId.toString())
                .update(
                    mapOf(
                        "paidUntilTimestamp" to newPaidUntil,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            Log.d(TAG, "Successfully recorded cloud payment for customer ${log.customerName}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync deposit log to cloud", e)
        }
    }

    /**
     * Listen for real-time customer updates from cloud
     */
    fun startListeningCustomerUpdates(
        context: Context,
        onCustomerUpdated: (SubscriptionCustomer) -> Unit
    ) {
        stopListening()
        val firestore = getFirestore(context) ?: return
        val storeId = getSyncConfig(context).storeId
        if (storeId.isEmpty()) return

        customerListener = firestore.collection("stores").document(storeId)
            .collection("subscription_customers")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed for customers", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (doc in snapshot.documentChanges) {
                        try {
                            val data = doc.document.data
                            val id = (data["id"] as? Number)?.toLong() ?: doc.document.id.toLongOrNull() ?: continue
                            val cust = SubscriptionCustomer(
                                id = id,
                                customerCode = data["customerCode"] as? String ?: "",
                                name = data["name"] as? String ?: "",
                                phone = data["phone"] as? String ?: "",
                                address = data["address"] as? String ?: "",
                                packageId = (data["packageId"] as? Number)?.toLong() ?: 0L,
                                packageName = data["packageName"] as? String ?: "",
                                monthlyPrice = (data["monthlyPrice"] as? Number)?.toDouble() ?: 0.0,
                                installationDate = (data["installationDate"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                dueDay = (data["dueDay"] as? Number)?.toInt() ?: 10,
                                paidUntilTimestamp = (data["paidUntilTimestamp"] as? Number)?.toLong() ?: 0L,
                                status = data["status"] as? String ?: "ACTIVE",
                                notes = data["notes"] as? String ?: "",
                                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            )
                            onCustomerUpdated(cust)
                        } catch (err: Exception) {
                            Log.e(TAG, "Error parsing remote customer", err)
                        }
                    }
                }
            }
    }

    fun stopListening() {
        customerListener?.remove()
        customerListener = null
        collectorListener?.remove()
        collectorListener = null
    }
}
