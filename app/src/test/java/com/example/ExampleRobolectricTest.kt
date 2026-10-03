package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.CasaOgumDatabase
import com.example.data.CasaOgumRepository
import com.example.util.SecurityUtils
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun verifyAppNameAndOfficialAdminAccount() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Casa do Ogum - RJ", appName)

        val db = Room.inMemoryDatabaseBuilder(context, CasaOgumDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = CasaOgumRepository(db.dao())
        repo.ensureSeeded()

        val admin = repo.findByCpf("05692005719")
        assertNotNull("Conta oficial do Administrador deve existir", admin)
        assertEquals("ADMIN", admin?.perfilAcesso)
        assertFalse("Administrador oficial não deve estar em primeiro acesso pendente", admin!!.primeiroAcesso)
        assertTrue("Senha 8691 deve ser válida para o Administrador", SecurityUtils.verifyPassword("8691", admin.senhaHash))

        val tempPin = SecurityUtils.generateTemporaryPassword()
        assertTrue("Senha temporária deve ter exatamente 4 números", SecurityUtils.isValidPin4(tempPin))
        db.close()
    }

    @Test
    fun verifyLoginUiFlowWithAdminCredentials() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("CASA DO OGUM - RJ").assertIsDisplayed()

        composeTestRule.onNodeWithTag("cpf_input").performTextInput("05692005719")
        composeTestRule.onNodeWithTag("password_input").performTextInput("8691")
        composeTestRule.onNodeWithTag("login_button").performClick()

        composeTestRule.waitForIdle()
    }
}
