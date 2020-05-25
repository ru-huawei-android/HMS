package com.huawei.agc.clouddb.myquickstart.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.huawei.agc.clouddb.myquickstart.R
import com.huawei.agc.clouddb.myquickstart.model.Book
import com.huawei.agc.clouddb.myquickstart.util.CloudDB
import com.huawei.agc.clouddb.myquickstart.util.Constants
import com.huawei.agc.clouddb.myquickstart.util.Constants.Companion.HUAWEIID_SIGNIN
import com.huawei.agc.clouddb.myquickstart.view.ItemAdapter
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.api.entity.hwid.HwIDConstant
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), CloudDB.UiCallBack {

    private var recyclerView: RecyclerView? = null
    private var itemAdapter: ItemAdapter? = null
    private var items: MutableList<Book>? = null

    //menu buttons
    private var logOutMenuItem: MenuItem? = null
    private var logInMenuItem: MenuItem? = null
    private var addMenuItem: MenuItem? = null

    //for Add/edit window
    private var builder: AlertDialog.Builder? = null
    private var alertDialog: AlertDialog? = null
    private var inflater: LayoutInflater? = null

    //db
    private val cloudDB = CloudDB()

    private val mHandler = MyHandler()

    private class MyHandler: Handler() {
        override fun handleMessage(msg: Message) {
            //TODO
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.itemList)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        items = ArrayList()

        //Just refresh data from CloudDB by "swipeRefreshLayout"
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            mHandler.post { cloudDB.getAll() }
        }
    }

    /**
     * 1. Obtain accessToken in order to logIn, you must to be authorized to use all functions "CRUD".
     *
     * 2. Initialization CloudDB, but I've done it globally before i.e. [CloudDBQuickStartApplication].
     *
     * 3. Add call backs. (not necessary, only for sample, you free to use MVVM, MWP etc)
     *
     * 4. Create Object Type. (Can be imported from the console [ObjectTypeInfoHelper])
     * You must implement zone/dataType on the console and download java files.
     *
     * 5. Open CloudDB Zone. (This will be closed [onDestroy])
     *
     * 6. Fetch all data from the CloudDB and push it to the recyclerView. (for my sample)
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Obtain accessToken in order to logIn and logIn
        if (requestCode == HUAWEIID_SIGNIN) {
            val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
            if (authHuaweiIdTask.isSuccessful) {
                val huaweiAccount = authHuaweiIdTask.result
                val accessToken = huaweiAccount.accessToken
                Log.i(Constants.MAIN_ACTIVITY, "accessToken: $accessToken")
                val credential = HwIdAuthProvider.credentialWithToken(accessToken)
                AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener {
                    val user = it.user
                    Toast.makeText(this@MainActivity, "Hello, ${user.displayName}", Toast.LENGTH_LONG)
                        .show()

                    mHandler.post {
                        cloudDB.addCallBacks(this)
                        // Get AGConnectCloudDB ObjectTypeInfo
                        cloudDB.createObjectType()
                        //Create the Cloud DB zone, And open CloudDB
                        cloudDB.openCloudDBZone()
                        //fetchDataFromDb()
                        cloudDB.getAll()
                    }
                }
                    .addOnFailureListener {
                        Log.e(Constants.MAIN_ACTIVITY, "onFailure: " + it.message)
                        Toast.makeText(this@MainActivity, "Failed", Toast.LENGTH_LONG).show()
                    }
            }
        } else {
            Toast.makeText(this@MainActivity, "HwID signIn failed", Toast.LENGTH_LONG).show()
            Log.e(Constants.MAIN_ACTIVITY, "HwID signIn failed")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        logOutMenuItem = menu?.findItem(R.id.menu_logout_button)
        logInMenuItem = menu?.findItem(R.id.menu_login_button)
        addMenuItem = menu?.findItem(R.id.menu_add_button)
        logIn()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_login_button -> {
                logIn()
                true
            }
            R.id.menu_logout_button -> {
                logOut()
                true
            }
            R.id.menu_add_button -> {
                addItem()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Add an new Item
     */
    private fun addItem() {
        //Call menu to add an item
        builder = AlertDialog.Builder(this)
        inflater = LayoutInflater.from(this)
        val view = inflater?.inflate(R.layout.popup, null)

        val saveButton: Button? = view?.findViewById(R.id.saveButton)
        val title: EditText? = view?.findViewById(R.id.titleBook)
        val description: EditText? = view?.findViewById(R.id.descriptionBook)
        val titlePage: TextView? = view?.findViewById(R.id.titlePage)

        titlePage?.text = "Enter book"
        saveButton?.text = "Save"

        builder?.setView(view)
        alertDialog = builder?.create()
        alertDialog?.show()

        saveButton?.setOnClickListener {
            val item = Book()
            item.id = cloudDB.getBookIndex() + 1
            item.bookName = title?.text.toString().trim()
            item.description = description?.text.toString().trim()

            //save the new item to CloudDB
            mHandler.post { cloudDB.insertBook(item) }

            alertDialog?.dismiss()
        }
    }

    /**
     * LogOut
     */
    private fun logOut() {
        val auth = AGConnectAuth.getInstance()
        auth.signOut()

        logOutMenuItem?.isVisible = false
        logInMenuItem?.isVisible = true
    }

    /**
     * LogIn, next step is to obtain info [onActivityResult]
     */
    private fun logIn() {
        logOut()

        val huaweiIdAuthParamsHelper = HuaweiIdAuthParamsHelper(
            HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM
        )
        val scopeList: MutableList<Scope> = ArrayList()
        scopeList.add(Scope(HwIDConstant.SCOPE.ACCOUNT_BASEPROFILE))
        huaweiIdAuthParamsHelper.setScopeList(scopeList)
        val authParams = huaweiIdAuthParamsHelper.setAccessToken().createParams()
        val service = HuaweiIdAuthManager.getService(this@MainActivity, authParams)
        startActivityForResult(service.signInIntent, HUAWEIID_SIGNIN)

        logInMenuItem?.isVisible = false
        logOutMenuItem?.isVisible = true
    }

    /**
     * Close the CloudDBZone
     */
    override fun onDestroy() {
        cloudDB.closeCloudDBZone()
        super.onDestroy()
    }

    /**
     * Call back to add an item
     */
    override fun onAddOrQuery(books: List<Book>?) {
        items = books as MutableList<Book>?
        itemAdapter = items?.let { ItemAdapter(this, it as ArrayList<Book>) }
        recyclerView?.adapter = itemAdapter
        itemAdapter?.notifyDataSetChanged()
    }
}
