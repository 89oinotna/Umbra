/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oinotna.umbra

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.oinotna.umbra.input.MySocket
import java.io.IOException
import java.security.GeneralSecurityException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * An activity that inflates a layout that has a [BottomNavigationView].
 */
class MainActivity : AppCompatActivity() {

    private var currentNavController: LiveData<NavController>? = null

    private lateinit var secretKeyViewModel: SecretKeyViewModel

    private lateinit var br: BroadcastReceiver

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Umbra" //getString(R.string.channel_name)
            val descriptionText = "Umbra connection"//getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("com.oinotna.umbra.NOTIFICATION", name, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState
        secretKeyViewModel=ViewModelProvider(this).get(SecretKeyViewModel::class.java)
        secretKeyViewModel.aesKey=getAESKey()
        createNotificationChannel()
        setBroadcastReceiver()
    }

    /**
     * Set MyBroadcastReceiver that wait for the notification broadcast
     * Disconnect when broadcast is received
     */
    private fun setBroadcastReceiver() {
        br = MyBroadcastReceiver { intent: Intent? ->
            //todo controllare intent
            if (intent != null && MyBroadcastReceiver.ACTION_DISCONNECT == intent.action) {
                MySocket.getInstance()?.disconnect()
            }
        }

        //registro l'intent filter
        val filter = IntentFilter(MyBroadcastReceiver.ACTION_DISCONNECT)

        //registro il broadcast receiver
        /* From doc:   Context-registered receivers receive broadcasts as long as their registering
                        context is valid. For an example, if you register within an Activity context,
                        you receive broadcasts as long as the activity is not destroyed. If you register
                        with the Application context, you receive broadcasts as long as the app is running.*/
        applicationContext.registerReceiver(br, filter)

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)

        val navGraphIds = listOf(R.navigation.home, R.navigation.mouse, R.navigation.keyboard, R.navigation.settings)

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
                navGraphIds = navGraphIds,
                fragmentManager = supportFragmentManager,
                containerId = R.id.nav_host_container,
                intent = intent
        )

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, Observer { navController ->
            setupActionBarWithNavController(navController)
        })
        currentNavController = controller
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    fun getCurrentNavController(): NavController? {
        return currentNavController?.value
    }

    /**
     * retrieve the aes key used for getEncryptedBytes/decrypt stored password
     * @return
     */
    private fun getAESKey(): SecretKey? {
        //TODO store da qualche parte
        return try {
            val mk = MasterKey.Builder(application).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            val sharedPreferences = EncryptedSharedPreferences.create(
                    this,
                    "secret_shared_prefs",
                    mk,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            // use the shared preferences and editor as you normally would
            val editor = sharedPreferences.edit()
            //first time create the key
            if (sharedPreferences.getString("k", null) == null) {
                val keygen = KeyGenerator.getInstance("AES")
                keygen.init(256)
                val key = keygen.generateKey()
                val encoded: String
                // get base64 encoded version of the key
                encoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    java.util.Base64.getEncoder().encodeToString(key.encoded)
                } else {
                    Base64.encodeToString(key.encoded, Base64.DEFAULT)
                }
                editor.putString("k", encoded)
                editor.apply()
                key
            } else {
                // decode the base64 encoded string
                var decoded = ByteArray(0)
                decoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    java.util.Base64.getDecoder().decode(sharedPreferences.getString("k", null))
                } else {
                    Base64.decode(sharedPreferences.getString("k", null), Base64.DEFAULT)
                }
                // rebuild key using SecretKeySpec
                SecretKeySpec(decoded, 0, decoded.size, "AES")
            }
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
