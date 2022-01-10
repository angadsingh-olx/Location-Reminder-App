package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.google.firebase.auth.FirebaseAuth




/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityAuthenticationBinding

    companion object {
        const val TAG = "AuthenticationActivity"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAuthenticationBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startActivity(Intent(this, RemindersActivity::class.java))
            finish()
        } else {
            viewBinding.welcomeText.visibility = View.VISIBLE
            viewBinding.actionLogin.visibility = View.VISIBLE
            viewBinding.progressLoader.visibility = View.GONE
        }

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        viewBinding.actionLogin.setOnClickListener {
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(providers)
                    .setLogo(R.drawable.map)
                    .setTheme(R.style.LoginScreenTheme)
                    .build(), SIGN_IN_RESULT_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                startActivity(Intent(this, RemindersActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Login Failed, Please try again!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Login Failed, Please try again!", Toast.LENGTH_SHORT).show()
        }
    }
}
