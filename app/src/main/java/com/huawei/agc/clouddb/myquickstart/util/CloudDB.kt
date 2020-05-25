package com.huawei.agc.clouddb.myquickstart.util

import android.content.Context
import android.util.Log
import com.huawei.agc.clouddb.myquickstart.model.Book
import com.huawei.agc.clouddb.myquickstart.model.ObjectTypeInfoHelper
import com.huawei.agc.clouddb.myquickstart.util.Constants.Companion.ZONE_NAME
import com.huawei.agconnect.cloud.database.AGConnectCloudDB
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot
import com.huawei.agconnect.cloud.database.CloudDBZoneTask
import com.huawei.agconnect.cloud.database.ListenerHandler
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

class CloudDB {

    private var mCloudDB: AGConnectCloudDB? = AGConnectCloudDB.getInstance()
    private var mCloudDBZone: CloudDBZone? = null
    private var mRegister: ListenerHandler? = null
    private var mConfig: CloudDBZoneConfig? = null

    private var mUiCallBack: UiCallBack? = null

    private var mBookIndex = 0

    private val mReadWriteLock: ReadWriteLock = ReentrantReadWriteLock()

    /**
     * Init AGConnectCloudDB in Application
     *
     * @param context application context
     */
    companion object {
        fun initAGConnectCloudDB(context: Context) {
            AGConnectCloudDB.initialize(context)
            Log.d(Constants.DB_ZONE_WRAPPER, "initAGConnectCloudDB")
        }
    }

    /**
     * Call AGConnectCloudDB.createObjectType to init schema
     */
    fun createObjectType() {
        try {
            mCloudDB?.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo())
        } catch (e: AGConnectCloudDBException) {
            Log.w(Constants.DB_ZONE_WRAPPER, "createObjectType: " + e.message)
        }
    }

    /**
     * Call AGConnectCloudDB.openCloudDBZone to open a cloudDBZone.
     * We set it with cloud cache mode, and data can be store in local storage
     */
    fun openCloudDBZone() {
        mConfig = CloudDBZoneConfig(
            ZONE_NAME,
            CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
            CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC
        )

        mConfig?.persistenceEnabled = true
        try {
            mCloudDBZone = mCloudDB?.openCloudDBZone(mConfig!!, true)
        } catch (e: AGConnectCloudDBException) {
            Log.w(Constants.DB_ZONE_WRAPPER, "openCloudDBZone: " + e.message)
        }
    }

    /**
     * Call AGConnectCloudDB.closeCloudDBZone
     */
    fun closeCloudDBZone() {
        try {
            mRegister?.remove()
            mCloudDB?.closeCloudDBZone(mCloudDBZone)
        } catch (e: AGConnectCloudDBException) {
            Log.w(Constants.DB_ZONE_WRAPPER, "closeCloudDBZone: " + e.message)
        }
    }

    /**
     * Query all books in storage from cloud side with CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
     */
    fun getAll()  {
        if (mCloudDBZone == null) {
            Log.w(Constants.DB_ZONE_WRAPPER, "CloudDBZone is null, try re-open it")
            return
        }
        val queryTask: CloudDBZoneTask<CloudDBZoneSnapshot<Book>> = mCloudDBZone!!.executeQuery(
            CloudDBZoneQuery.where(Book::class.java),
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )
        queryTask
            .addOnSuccessListener {
                processQueryResult(it)
            }
            .addOnFailureListener {
                Log.e(Constants.DB_ZONE_WRAPPER, it.message.toString())
            }

    }

    private fun processQueryResult(snapshot: CloudDBZoneSnapshot<Book>) {
        val bookInfoCursor: CloudDBZoneObjectList<Book> = snapshot.snapshotObjects
        val bookInfoList: MutableList<Book> = ArrayList()
        try {
            while (bookInfoCursor.hasNext()) {
                val book: Book = bookInfoCursor.next()
                bookInfoList.add(book)
                updateBookIndex(book)
            }
        } catch (e: AGConnectCloudDBException) {
            Log.w(Constants.DB_ZONE_WRAPPER, "processQueryResult: " + e.message)
        }
        snapshot.release()
        if (mUiCallBack != null) {
            mUiCallBack?.onAddOrQuery(bookInfoList)
        }
    }

    /**
     * Delete book
     *
     * @param bookList books selected by user
     */
    fun deleteBook(bookList: List<Book>) {
        if (mCloudDBZone == null) {
            Log.w(Constants.DB_ZONE_WRAPPER, "CloudDBZone is null, try re-open it")
            return
        }
        val deleteTask = mCloudDBZone?.executeDelete(bookList)
        if (mUiCallBack == null) {
            return
        }
        if (deleteTask?.exception != null) {
            Log.e(Constants.DB_ZONE_WRAPPER, "Delete book is failed")
            return
        }
    }

    /**
     * Insert book
     *
     * @param book book added or modified from local
     */
    fun insertBook(book: Book) {
        if (mCloudDBZone == null) {
            Log.w(Constants.DB_ZONE_WRAPPER, "CloudDBZone is null, try re-open it")
            return
        }
        val insertTask: CloudDBZoneTask<Int> = mCloudDBZone!!.executeUpsert(book)
        insertTask.addOnSuccessListener { cloudDBZoneResult ->
            Log.w(Constants.DB_ZONE_WRAPPER, "upsert $cloudDBZoneResult records")
        }.addOnFailureListener {
            Log.e(Constants.DB_ZONE_WRAPPER, "onFailure: " + it.message)
        }
    }

    /**
     * Get max id of books
     *
     * @return max book id
     */
    fun getBookIndex(): Int {
        return try {
            mReadWriteLock.readLock().lock()
            mBookIndex
        } finally {
            mReadWriteLock.readLock().unlock()
        }
    }

    private fun updateBookIndex(book: Book) {
        try {
            mReadWriteLock.writeLock().lock()
            if (mBookIndex < book.id) {
                mBookIndex = book.id
            }
        } finally {
            mReadWriteLock.writeLock().unlock()
        }
    }

    /**
     * Add a callback to update book info list
     *
     * @param uiCallBack callback to update book list
     */
    fun addCallBacks(uiCallBack: UiCallBack) {
        mUiCallBack = uiCallBack
    }

    /**
     * Call back to update ui in HomePageFragment
     */
    interface UiCallBack {
        fun onAddOrQuery(books: List<Book>?)
    }
}