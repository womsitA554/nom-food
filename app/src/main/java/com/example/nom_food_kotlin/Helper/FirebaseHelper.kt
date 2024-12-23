package com.example.nom_food_kotlin.Helper

import android.util.Log
import com.example.nom_food_kotlin.Model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseHelper {
    private val dbPopularItems = FirebaseDatabase.getInstance().getReference("PopularItems")
    private val dbCategories = FirebaseDatabase.getInstance().getReference("Categorys")
    private val dbCategoriesOfItem = FirebaseDatabase.getInstance().getReference("AllItems")
    private val dbDiscount = FirebaseDatabase.getInstance().getReference("Discounts")
    private val dbPayment = FirebaseDatabase.getInstance().getReference("Payments")
    private val dbBill = FirebaseDatabase.getInstance().getReference("Bills")
    val dbUsers = FirebaseDatabase.getInstance().reference

    fun currentUserId(): String? {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid
    }

    fun randomPassword(): String {
        return UUID.randomUUID().toString()
    }

    fun getPhoneNumberAndEmail(userId: String): Task<DataSnapshot> {
        return dbUsers.child("Users").child(userId).get()
    }

    fun getAllUserId(callback: (List<String>) -> Unit) {
        dbUsers.child("Users").get()
            .addOnSuccessListener { snapshot ->
                val userIds = snapshot.children.mapNotNull { it.key }
                callback(userIds)
            }
            .addOnFailureListener { exception ->
                println("Error getting user IDs: ${exception.message}")
                callback(emptyList())
            }
    }

    fun addUser(user: User) {
        val userId = currentUserId()
        if (userId != null) {
            dbUsers.child("Users").child(userId).setValue(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("User added successfully")
                    } else {
                        println("Failed to add user: ${task.exception?.message}")
                    }
                }
        } else {
            println("User ID is null")
        }
    }

    suspend fun getAllCategories(): List<Category> {
        val listCategory = mutableListOf<Category>()
        try {
            val dataSnapshot1 = dbCategories.get().await()
            for (snapshot in dataSnapshot1.children) {
                val category = snapshot.getValue(Category::class.java)
                category?.let {
                    listCategory.add(it)
                }
            }
        } catch (e: Exception) {
            Log.d("FirebaseHelper1", "Error getting categories: ${e.message}")
        }
        return listCategory
    }

    suspend fun getAllPopularItems(): List<Menu> {
        val listMenu = mutableListOf<Menu>()
        try {
            val dataSnapshot = dbPopularItems.get().await()
            for (itemSnapshot in dataSnapshot.children) {
                val menu = itemSnapshot.getValue(Menu::class.java)
                menu?.let {
                    listMenu.add(it)
                }
            }
        } catch (e: Exception) {
            Log.d("FirebaseHelper", "Error getting popular items: ${e.message}")
        }
        return listMenu
    }

    suspend fun getAllCategoryOfItems(title: String): List<Menu> {
        val listCategoryItem = mutableListOf<Menu>()
        try {
            val dataSnapshot = dbCategoriesOfItem.child(title).get().await()
            Log.d("data1234", "DataSnapshot: $dataSnapshot")
            for (itemSnapshot in dataSnapshot.children) {
                val menu = itemSnapshot.getValue(Menu::class.java)
                menu?.let {
                    listCategoryItem.add(it)
                }
            }
        } catch (e: Exception) {
            Log.d("FirebaseHelper2", "Error getting category items: ${e.message}")
        }
        return listCategoryItem
    }

    suspend fun getAllDiscount(): List<Discount> {
        val listDiscount = mutableListOf<Discount>()
        try {
            val dataSnapshot = dbDiscount.get().await()
            for (discountItem in dataSnapshot.children) {
                val discount = discountItem.getValue(Discount::class.java)
                discount?.let {
                    listDiscount.add(it)
                }
            }
        } catch (e: Exception) {
            Log.d("FirebaseHelper4", "Error getting discounts: ${e.message}")
        }
        return listDiscount
    }

    suspend fun getAllPayment(): List<Payment> {
        val listPayment = mutableListOf<Payment>()
        try {
            val dataSnapshot = dbPayment.get().await()
            for (paymentItem in dataSnapshot.children) {
                val payment = paymentItem.getValue(Payment::class.java)
                payment?.let {
                    listPayment.add(it)
                }
            }
        } catch (e: Exception) {
            Log.d("FirebaseHelper5", "Error getting payments: ${e.message}")
        }
        return listPayment
    }

    fun addBill(bill: Bill) {
        bill.billId?.let {
            dbBill.child(it).setValue(bill)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("Bill added successfully")
                    } else {
                        println("Failed to add bill: ${task.exception?.message}")
                    }
                }
        }
    }

    suspend fun getBillsForCurrentUser(userId: String): List<Bill> {
        val listBill = mutableListOf<Bill>()
        try {
            val dataSnapshot = dbBill.get().await()
            for (billItem in dataSnapshot.children) {
                val bill = billItem.getValue(Bill::class.java)
                bill?.let {
                    if (it.userId == userId) {
                        listBill.add(it)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("FirebaseHelper", "Error getting bills: ${e.message}")
        }
        return listBill
    }

    suspend fun getAllItems(): List<Menu> {
        val listItems = mutableListOf<Menu>()
        try {
            val dataSnapshot = dbCategoriesOfItem.get().await()
            for (categorySnapshot in dataSnapshot.children) {
                for (itemSnapshot in categorySnapshot.children) {
                    val menu = itemSnapshot.getValue(Menu::class.java)
                    menu?.let {
                        listItems.add(it)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("FirebaseHelper3", "Error getting items: ${e.message}")
        }
        return listItems
    }

    suspend fun getAllBills(): List<Bill> {
        val listBill = mutableListOf<Bill>()
        try {
            val dataSnapshot = dbBill.get().await()
            for (billItem in dataSnapshot.children) {
                val bill = billItem.getValue(Bill::class.java)
                bill?.let {
                    listBill.add(it)
                }
            }
        } catch (e: Exception) {
            Log.d("FirebaseHelper7", "Error getting bills: ${e.message}")
        }
        return listBill
    }

    fun addMenu(category: String, menu: Menu) {
        menu.itemId?.let {
            dbCategoriesOfItem.child(category).child(it).setValue(menu)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("Menu added successfully")
                    } else {
                        println("Failed to add menu: ${task.exception?.message}")
                    }
                }
        }
    }

    fun getAllCategory(callback: (List<Category>) -> Unit) {
        dbCategories.get()
            .addOnSuccessListener { dataSnapshot ->
                val listCategory = mutableListOf<Category>()
                for (snapshot in dataSnapshot.children) {
                    val category = snapshot.getValue(Category::class.java)
                    category?.let {
                        listCategory.add(it)
                    }
                }
                callback(listCategory)
            }
            .addOnFailureListener { exception ->
                Log.d("FirebaseHelper", "Error getting categories: ${exception.message}")
                callback(emptyList())
            }
    }

    fun updateMenu(category: String, menu: Menu) {
        try {
            menu.itemId?.let {
                dbCategoriesOfItem.child(category).child(it).setValue(menu)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            println("Menu updated successfully")
                        } else {
                            println("Failed to update menu: ${task.exception?.message}")
                        }
                    }
            }
        } catch (e: Exception) {
            Log.d("FirebaseHelper", "Error updating menu: ${e.message}")
        }
    }

    fun deleteMenu(category: String, itemId: String) {
        try {
            dbCategoriesOfItem.child(category).child(itemId).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("Menu deleted successfully")
                    } else {
                        println("Failed to delete menu: ${task.exception?.message}")
                    }
                }
        } catch (e: Exception) {
            Log.d("FirebaseHelper", "Error deleting menu: ${e.message}")
        }
    }

    fun updateStatus(billId: String, status: String) {
        try {
            dbBill.child(billId).child("status").setValue(status)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("Status updated successfully")
                    } else {
                        println("Failed to update status: ${task.exception?.message}")
                    }
                }
        } catch (e: Exception) {
            Log.d("FirebaseHelper", "Error updating status: ${e.message}")
        }
    }
    fun updateReason(billId: String, reason: String) {
        try {
            dbBill.child(billId).child("reason").setValue(reason)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("Reason updated successfully")
                    } else {
                        println("Failed to update reason: ${task.exception?.message}")
                    }
                }
        } catch (e: Exception) {
            Log.d("FirebaseHelper", "Error updating reason: ${e.message}")
        }
    }

    suspend fun getAllBillOfStatus(): List<Bill> {
        val listBill = mutableListOf<Bill>()
        try {
            val dataSnapshot = dbBill.get().await()
            for (billItem in dataSnapshot.children) {
                val bill = billItem.getValue(Bill::class.java)
                bill?.let {
                    if (it.status == "Pending") {
                        listBill.add(it)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("FirebaseHelper7", "Error getting bills of status: ${e.message}")
        }
        return listBill
    }

    suspend fun getQuantityOfOrder(): Int {
        val data = getAllBillOfStatus()
        return data.size
    }

    fun sendNewOrderNotification(orderId: String) {
        dbUsers.child("Users").get().addOnSuccessListener { snapshot ->
            for (userSnapshot in snapshot.children) {
                val user = userSnapshot.getValue(User::class.java)
                if (user?.role == "Admin") {
                    val adminToken = user.fcmToken
                    adminToken?.let {
                        val message = RemoteMessage.Builder("$it@fcm.googleapis.com")
                            .setMessageId(orderId)
                            .addData("title", "New Order")
                            .addData("body", "A new order has been placed with ID: $orderId")
                            .build()
                        FirebaseMessaging.getInstance().send(message)
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseHelper", "Error fetching users: ${exception.message}")
        }
    }
}