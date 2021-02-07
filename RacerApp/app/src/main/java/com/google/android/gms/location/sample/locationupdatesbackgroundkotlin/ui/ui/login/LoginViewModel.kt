package com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.ui.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.example.bupazar.User
import com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.*
import com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.data.SocketPacket
import com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.ui.LocationUpdateFragment
import com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.ui.data.LoginRepository
import com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.ui.data.Result
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.util.concurrent.Executors


class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        Executors.newSingleThreadExecutor().execute {

            val socket = Socket(IP_ADDRESS, PORT)

            val send = SocketPacket(
                operation = O_LOGIN,
                username = username,
                password = password
            )

            val json = Gson().toJson(send)
            socket.outputStream.write(json.toByteArray())
            socket.outputStream.write("\n".toByteArray())
            socket.outputStream.flush()

            var packet : SocketPacket? = null
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            var inputLine: String?
            if(reader.readLine().also { inputLine = it } != null) {
                packet = Gson().fromJson(
                    inputLine,
                    SocketPacket::class.java
                )
            }
            socket.close()

            val result = loginRepository.login(username, password)

            if (packet?.username !=null){
                    User.username = username
                    User.password = password
                _loginResult.postValue(LoginResult(success = LoggedInUserView(displayName = packet.username!!)))
            } else {
                _loginResult.postValue(LoginResult(error = R.string.login_failed))
            }

        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}