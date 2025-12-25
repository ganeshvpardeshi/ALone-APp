package com.example.alone.signIn_singUp.botChatInterface

import android.content.Intent
import com.example.alone.R
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.alone.MainActivity
import com.example.alone.beConnection.ApiClient
import com.example.alone.beConnection.UserApi
import com.example.alone.error.ErrorMapper
import com.example.alone.model.LoginWithEmailRequest
import com.example.alone.model.LoginWithUsernameRequest
import com.example.alone.model.User
import com.example.alone.model.UserEmailUsername
import com.example.alone.signIn_singUp.RegisterActivity
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.UUID
enum class AuthMode { LOGIN, SIGNUP }
enum class LoginStep { WELCOME, ASK_EMAIL, ASK_PASSWORD, VERIFY }
enum class SignupStep {
    ASK_USERNAME,
    ASK_EMAIL,
    ASK_PASSWORD,
    ASK_PROFILE_NAME,
    ASK_AGE,
    ASK_GENDER,
    CONFIRM
}

class BotChatActivity : AppCompatActivity() {
    // At class level
    private var signupInvalidCount = 0
    private var loginInvalidUserCount = 0       // wrong username/email
    private var loginInvalidPasswordCount = 0   // wrong password
    private val maxInvalidAttempts = 3

    private lateinit var rvChat: RecyclerView
    private lateinit var etInput: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var inputLayout : TextInputLayout
    private lateinit var ivGifBackground: ImageView
    private lateinit var btnBackHome: Button
    private lateinit var inputRow: LinearLayout

    private val botQueue: MutableList<String> = mutableListOf()
    private var isBotPlaying: Boolean = false
    private var isBotTyping: Boolean = false
    private val botCharDelay = 40L

    private val registerActivity: RegisterActivity = RegisterActivity();

    private var authMode = AuthMode.LOGIN
    private var loginStep = LoginStep.WELCOME
    private var signupStep = SignupStep.ASK_USERNAME

    private var tempEmail: String = ""
    private var tempPassword: String = ""
    private var tempUsername: String = ""
    private var tempProfileName: String = ""
    private var tempAge: Int = 0
    private var tempGender: String = ""

    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_bot)

        loadSingleGifBackground(); // initializing the glider and imageview

        etInput = findViewById(R.id.editMessage)
        btnSend = findViewById(R.id.buttonSend)
        rvChat = findViewById(R.id.recyclerMessages)
        inputLayout = findViewById(R.id.inputLayout)
        val layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
            reverseLayout = false
        }
        rvChat.layoutManager = layoutManager

        chatAdapter = ChatAdapter(mutableListOf())
        rvChat.adapter = chatAdapter

        inputRow = findViewById(R.id.inputRow)
        btnBackHome = findViewById(R.id.btnBackHome)

        btnBackHome.setOnClickListener {
            // Go to home (Splash/Main)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


        setupInput()
        val mode = intent.getStringExtra("AUTH_MODE") ?: "LOGIN"
        authMode = if (mode == "SIGNUP") AuthMode.SIGNUP else AuthMode.LOGIN

        if (authMode == AuthMode.LOGIN) {
            startLoginFlow()
        } else {
            startSignupFlow()
        }
    }


    // ✅ SINGLE GIF - No more switching!
    private fun loadSingleGifBackground() {
        ivGifBackground = findViewById(R.id.ivGifBackground)

        Glide.with(this)
            .asGif()
            .load(R.raw.new_gif)        // fill entire screen area
            .into(ivGifBackground)
    }

    // ---------- USER INPUT ----------

    private fun setupInput() {
        btnSend.setOnClickListener {
            if (isBotTyping) return@setOnClickListener

            val text = etInput.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            addUserMessage(text)
            etInput.text?.clear()

            // ✅ WRAP with uiScope.launch!
            uiScope.launch {
                handleUserInput(text)
            }
        }
    }


    private fun addUserMessage(text: String) {
        chatAdapter.addMessage(BotChatMessage(text, isBot = false))
        rvChat.scrollToPosition(chatAdapter.itemCount - 1)
    }

    private suspend fun handleUserInput(input: String) {
        when (authMode) {
            AuthMode.LOGIN -> handleLoginInput(input)
            AuthMode.SIGNUP -> handleSignupInput(input)
        }
    }

    private suspend fun handleLoginInput(input: String) {
        var isAvailable: Boolean = false
        when (loginStep) {
            LoginStep.ASK_EMAIL -> {
                tempUsername = input
                // ✅ NOW WAITS CORRECTLY!
                isAvailable = if(tempUsername.trim().contains('@')) (!checkUserAvailabilityWithApi(email = tempUsername))
                                else (!checkUserAvailabilityWithApi(username = tempUsername))

                if (isAvailable) {
                    queueBotMessage("There is no account of this '$tempUsername'. Please register first.")
                    return
                }
                queueBotMessage("Great! Now enter your password.")
                inputLayout.hint = "Enter password"
                loginStep = LoginStep.ASK_PASSWORD
            }
            LoginStep.ASK_PASSWORD -> {
                tempPassword = input
                loginStep = LoginStep.VERIFY

                val identifier = tempUsername?.trim().orEmpty()
                val at = identifier.indexOf('@')
                val isEmail = at > 0 && at < identifier.lastIndex

                verifyLoginWithApi(if (isEmail) "email" else "username")
            }
            else -> {}
        }
    }

    private suspend fun handleSignupInput(input: String) {
        when (signupStep) {
            SignupStep.ASK_USERNAME -> {
                tempUsername = input
                if (!registerActivity.isUsernameValid(tempUsername)) {
                    signupInvalidCount++
                    checkSignupAttempts()
                    queueBotMessage("Username can only contain letters, numbers, and _ (no spaces/special chars)")
                    return
                }

                val isAvailable = !checkUserAvailabilityWithApi(username = tempUsername)
                if (!isAvailable) {
                    signupInvalidCount++
                    checkSignupAttempts()
                    queueBotMessage("Username '$tempUsername' is taken. Choose another.")
                    return
                }

                queueBotMessage("Nice name, $tempUsername! Now enter your email.")
                inputLayout.hint = "Enter email"
                etInput.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                signupStep = SignupStep.ASK_EMAIL
            }

            SignupStep.ASK_EMAIL -> {
                tempEmail = input
                if (!registerActivity.isEmailValid(tempEmail)) {
                    signupInvalidCount++
                    checkSignupAttempts()
                    queueBotMessage("Please enter a valid Gmail address only")
                    return
                }

                val isAvailable = !checkUserAvailabilityWithApi(email = tempEmail)
                if (!isAvailable) {
                    signupInvalidCount++
                    checkSignupAttempts()
                    queueBotMessage("Email '$tempEmail' is already registered. So, You can login by clicking on 'Login'")
                    return
                }

                queueBotMessage("Great! Now choose a password.")
                inputLayout.hint = "Enter password"
                etInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                signupStep = SignupStep.ASK_PASSWORD
            }

            SignupStep.ASK_PASSWORD -> {
                tempPassword = input
                if (!registerActivity.isPasswordValid(tempPassword)) {
                    signupInvalidCount++
                    checkSignupAttempts()
                    queueBotMessage("Password must be 9+ chars with 1 uppercase, 1 lowercase, 1 number, 1 special char")
                    return
                }
                queueBotMessage("Cool. What profile name should others see for you?")
                etInput.inputType = InputType.TYPE_CLASS_TEXT
                inputLayout.hint = "Enter profile name"
                signupStep = SignupStep.ASK_PROFILE_NAME
            }

            // ✅ PROFILE NAME
            SignupStep.ASK_PROFILE_NAME -> {
                tempProfileName = input
                if (!registerActivity.isProfileNameValid(tempProfileName, tempUsername)) {
                    signupInvalidCount++
                    checkSignupAttempts()
                    queueBotMessage("Profile name must be different from username ($tempUsername)")
                    return
                }
                queueBotMessage("Got it! Now enter your age (number).")
                etInput.inputType = InputType.TYPE_CLASS_NUMBER
                inputLayout.hint = "Enter age"
                signupStep = SignupStep.ASK_AGE
            }

            // ✅ AGE
            SignupStep.ASK_AGE -> {
                val ageInt = input.toIntOrNull()
                if (ageInt == null || !registerActivity.isAgeValid(ageInt)) {
                    signupInvalidCount++
                    checkSignupAttempts()
                    queueBotMessage("Age must be a number greater than 19")
                    signupStep = SignupStep.ASK_AGE
                    return
                }

                tempAge = ageInt
                queueBotMessage("Thanks! Finally, enter your gender (e.g. M / F / O).")
                etInput.inputType = InputType.TYPE_CLASS_TEXT
                inputLayout.hint = "Enter gender"
                signupStep = SignupStep.ASK_GENDER
            }

            // ✅ GENDER
            SignupStep.ASK_GENDER -> {
                tempGender = input.uppercase().trim()
                if (!registerActivity.isGenderValid(tempGender)) {
                    signupInvalidCount++
                    checkSignupAttempts()
                    queueBotMessage("Gender must be M, F, or O")
                    return
                }
                signupInvalidCount = 0  // all good, reset counter
                signupStep = SignupStep.CONFIRM
                registerUserWithApi()
            }

            else -> {}
        }
    }

    // ---------- BOT FLOW / QUEUE ----------

    private fun startLoginFlow() {
        botQueue.clear()
        isBotPlaying = false

        queueBotMessage("Hey Buddie, Welcome Back Again!\nEnter your login credentials to get into the app.")
        queueBotMessage("First, enter your username / email.")
        inputLayout.hint = "Enter email / username"
        loginStep = LoginStep.ASK_EMAIL
    }

    private fun startSignupFlow() {
        botQueue.clear()
        isBotPlaying = false

        queueBotMessage("Hey User, welcome! Let's create your account.")
        queueBotMessage("First, enter your desired username.")
        inputLayout.hint = "Enter username"
        signupStep = SignupStep.ASK_USERNAME
    }

    private fun queueBotMessage(text: String) {
        botQueue.add(text)
        if (!isBotPlaying) {
            playNextBotMessage()
        }
    }

    private fun playNextBotMessage() {
        if (botQueue.isEmpty()) {
            isBotPlaying = false
            isBotTyping = false
            btnSend.isEnabled = true
            return
        }
        isBotPlaying = true
        isBotTyping = true
        btnSend.isEnabled = false

        val nextText = botQueue.removeAt(0)

        showTyping()
        rvChat.postDelayed({
            hideTyping()
            typeWriterBotMessage(nextText) {
                if (botQueue.isEmpty()) {
                    isBotTyping = false
                    btnSend.isEnabled = true
                }
                playNextBotMessage()
            }
        }, 500)
    }

    private fun showTyping() {
        chatAdapter.addMessage(BotChatMessage(text = "", isBot = true, isTyping = true))
        rvChat.scrollToPosition(chatAdapter.itemCount - 1)
    }

    private fun hideTyping() {
        chatAdapter.removeTypingIndicator()
    }

    private fun typeWriterBotMessage(
        fullText: String,
        delayPerChar: Long = 40L,
        onFinished: (() -> Unit)? = null
    ) {
        val msg = BotChatMessage(text = "", isBot = true)
        chatAdapter.addMessage(msg)
        rvChat.scrollToPosition(chatAdapter.itemCount - 1)

        val index = chatAdapter.itemCount - 1
        var currentIndex = 0

        rvChat.post(object : Runnable {
            override fun run() {
                if (currentIndex <= fullText.length) {
                    val sub = fullText.substring(0, currentIndex)
                    chatAdapter.updateMessageText(index, sub)
                    currentIndex++
                    rvChat.postDelayed(this, delayPerChar)
                } else {
                    onFinished?.invoke()
                }
            }
        })
    }

    // ---------- API: LOGIN ----------

    private fun verifyLoginWithApi(way: String) {
        uiScope.launch {
            showTyping()
            try {
                val user = withContext(Dispatchers.IO) {
                    when (way) {
                        "email" -> ApiClient.userApi.login_with_email(
                            LoginWithEmailRequest(
                                email = tempUsername,
                                password = tempPassword
                            )
                        )
                        else -> ApiClient.userApi.login_with_username(
                            LoginWithUsernameRequest(
                                username = tempUsername,
                                password = tempPassword
                            )
                        )
                    }
                }
                hideTyping()
                // success: reset counters
                loginInvalidUserCount = 0
                loginInvalidPasswordCount = 0

                queueBotMessage("Welcome back ${user.username}!! Loading the app right away...")
                openDashboard()

            } catch (e: HttpException) {
                hideTyping()
                if (e.code() == 401) {
                    // treat as wrong password (identifier was valid)
                    loginInvalidPasswordCount++

                    if (loginInvalidPasswordCount >= maxInvalidAttempts) {
                        val msg = ErrorMapper.mapErrorToUserMessage(httpStatusCode = 429)
                        queueBotMessage("❌ $msg")
                        queueBotMessage("Forgot password? Tap here to reset.")
                        // stay on password step
                        inputRow.visibility = View.GONE
                        btnBackHome.visibility = View.VISIBLE

                        loginStep = LoginStep.ASK_PASSWORD
                        loginInvalidPasswordCount = 0
                    } else {
                        val msg = ErrorMapper.mapErrorToUserMessage(
                            httpStatusCode = 401,
                            errorMessage = e.response()?.errorBody()?.string()
                        )
                        queueBotMessage("❌ $msg")
                        queueBotMessage("Please enter your password again.")
                        loginStep = LoginStep.ASK_PASSWORD      // ← stay on password
                    }

                } else {
                    val msg = ErrorMapper.mapErrorToUserMessage(
                        httpStatusCode = e.code(),
                        errorMessage = e.response()?.errorBody()?.string()
                    )
                    queueBotMessage("❌ $msg")
                    // For non‑401 errors you can decide whether to go back or stay.
                    loginStep = LoginStep.ASK_EMAIL
                    queueBotMessage("Enter your username or email.")
                }

            } catch (e: Exception) {
                hideTyping()
                val msg = ErrorMapper.mapErrorToUserMessage(
                    errorCode = "NETWORK_ERROR",
                    errorMessage = e.message
                )
                queueBotMessage("❌ $msg")
                loginStep = LoginStep.ASK_EMAIL
                queueBotMessage("Enter your username or email.")
            }
        }
    }

    // ---------- API: Checking availability of email and username----------
    private suspend fun checkUserAvailabilityWithApi(email: String? = null, username: String? = null): Boolean {
        return try {
            // ✅ NO launch{} inside suspend function!
            withContext(Dispatchers.IO) {
                ApiClient.userApi.check_user_exists(email = email, username = username)
            }
            true  // ✅ Exists (duplicate - block)
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> false   // available
                else -> {
                    val msg = ErrorMapper.mapErrorToUserMessage(
                        httpStatusCode = e.code(),
                        errorMessage = e.response()?.errorBody()?.string()
                    )
                    // Optionally log or show once:
                    Log.w("BotChatActivity", "check_user_exists error: $msg")
                    false
                }
            }
        } catch (e: Exception) {
            val msg = ErrorMapper.mapErrorToUserMessage(
                errorCode = "NETWORK_ERROR",
                errorMessage = e.message
            )
            Log.w("BotChatActivity", "check_user_exists network error: $msg")
            false
        }
    }

    // ---------- API: SIGNUP ----------

    private fun registerUserWithApi() {
        uiScope.launch {
            showTyping()
            try {
                val newUser = User(
                    unique_id = UUID.randomUUID().toString(),
                    username = tempUsername,
                    email = tempEmail,
                    password = tempPassword,
                    profile_name = tempProfileName,
                    profile_photo = null,
                    gender = tempGender,
                    age = tempAge
                )

                val created = withContext(Dispatchers.IO) {
                    ApiClient.userApi.createUser(newUser)
                }
                hideTyping()
                queueBotMessage("Awesome, ${created.username}! Your account is created. You can now log in.")
                openDashboard()
            } catch (e: HttpException) {
                hideTyping()
                val msg = ErrorMapper.mapErrorToUserMessage(
                    httpStatusCode = e.code(),
                    errorMessage = e.response()?.errorBody()?.string(),
                    username = tempUsername,
                    profileName = tempProfileName
                )
                queueBotMessage("❌ $msg")
                queueBotMessage("Let's try again. Enter your desired username.")
                signupStep = SignupStep.ASK_USERNAME

            } catch (e: Exception) {
                hideTyping()
                val msg = ErrorMapper.mapErrorToUserMessage(
                    errorCode = "NETWORK_ERROR",
                    errorMessage = e.message
                )
                queueBotMessage("❌ $msg")
                queueBotMessage("Let's try again. Enter your desired username.")
                signupStep = SignupStep.ASK_USERNAME
            }
        }
    }


    private fun checkSignupAttempts() {
        if (signupInvalidCount >= maxInvalidAttempts) {
            val msg = ErrorMapper.mapErrorToUserMessage(httpStatusCode = 429)
            queueBotMessage("❌ $msg")
            // Optional: reset or lock
            signupInvalidCount = 0
        }
    }


    private fun openDashboard() {
         startActivity(Intent(this, MainActivity::class.java))
         finish()
    }
}
