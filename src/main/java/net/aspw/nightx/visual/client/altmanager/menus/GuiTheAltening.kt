package net.aspw.nightx.visual.client.altmanager.menus

import com.mojang.authlib.Agent.MINECRAFT
import com.mojang.authlib.exceptions.AuthenticationException
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication
import com.thealtening.AltService
import com.thealtening.api.TheAltening
import net.aspw.nightx.NightX
import net.aspw.nightx.event.SessionEvent
import net.aspw.nightx.features.module.modules.client.Hud
import net.aspw.nightx.utils.ClientUtils
import net.aspw.nightx.utils.misc.MiscUtils
import net.aspw.nightx.utils.render.RenderUtils
import net.aspw.nightx.visual.client.altmanager.GuiAltManager
import net.aspw.nightx.visual.elements.GuiPasswordField
import net.aspw.nightx.visual.font.Fonts
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Session
import org.lwjgl.input.Keyboard
import java.net.Proxy.NO_PROXY

class GuiTheAltening(private val prevGui: GuiAltManager) : GuiScreen() {

    val alteningLogo = ResourceLocation("client/altening.png")

    // Data Storage
    companion object {
        var apiKey: String = ""
    }

    // Buttons
    private lateinit var loginButton: GuiButton
    private lateinit var generateButton: GuiButton

    // User Input Fields
    private lateinit var apiKeyField: GuiTextField
    private lateinit var tokenField: GuiTextField

    // Status
    private var status = ""

    /**
     * Initialize The Altening Generator GUI
     */
    override fun initGui() {
        // Enable keyboard repeat events
        Keyboard.enableRepeatEvents(true)

        // Login button
        loginButton = GuiButton(2, width / 2 - 100, 75, "Login")
        buttonList.add(loginButton)

        // Generate button
        generateButton = GuiButton(1, width / 2 - 100, 140, "Generate")
        buttonList.add(generateButton)

        // Buy & Back buttons
        buttonList.add(GuiButton(3, width / 2 - 100, height - 54, 98, 20, "Free Gen"))
        buttonList.add(GuiButton(0, width / 2 + 2, height - 54, 98, 20, "Back"))

        // Token text field
        tokenField = GuiTextField(666, Fonts.fontSFUI40, width / 2 - 100, 50, 200, 20)
        tokenField.isFocused = true
        tokenField.maxStringLength = Integer.MAX_VALUE

        // Api key password field
        apiKeyField = GuiPasswordField(1337, Fonts.fontSFUI40, width / 2 - 100, 115, 200, 20)
        apiKeyField.maxStringLength = 18
        apiKeyField.text = apiKey
        super.initGui()
    }

    /**
     * Draw screen
     */
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        // Draw background to screen
        drawBackground(0)
        RenderUtils.drawRect(30.0f, 30.0f, width - 30.0f, height - 30.0f, Integer.MIN_VALUE)

        // Draw title and status and logo
        RenderUtils.drawImage2(alteningLogo, width / 2F - 50F, height / 2F - 90F, 100, 100)
        Fonts.fontLarge.drawCenteredString("The Altening", width / 2.0f, 12.0f, 0xffffff)
        Fonts.fontSFUI40.drawCenteredString(status, width / 2.0f, 30.0f, 0xffffff)

        // Draw fields
        apiKeyField.drawTextBox()
        tokenField.drawTextBox()

        // Draw text
        Fonts.fontSFUI40.drawCenteredString("§7Token:", width / 2.0f - 84, 38.0f, 0xffffff)
        Fonts.fontSFUI40.drawCenteredString("§7API-Key:", width / 2.0f - 78, 103.0f, 0xffffff)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    /**
     * Handle button actions
     */
    override fun actionPerformed(button: GuiButton) {
        if (!button.enabled) return

        when (button.id) {
            0 -> mc.displayGuiScreen(prevGui)
            1 -> {
                loginButton.enabled = false
                generateButton.enabled = false
                apiKey = apiKeyField.text

                val altening = TheAltening(apiKey)
                val asynchronous = TheAltening.Asynchronous(altening)
                if (NightX.moduleManager.getModule(Hud::class.java)?.flagSoundValue!!.get()) {
                    NightX.tipSoundManager.popSound.asyncPlay(90f)
                }
                status = "§cGenerating account..."

                asynchronous.accountData.thenAccept { account ->
                    if (NightX.moduleManager.getModule(Hud::class.java)?.flagSoundValue!!.get()) {
                        NightX.tipSoundManager.popSound.asyncPlay(90f)
                    }
                    status = "§aGenerated account: §b§l${account.username}"

                    try {
                        status = "§cSwitching Alt Service..."

                        // Change Alt Service
                        GuiAltManager.altService.switchService(AltService.EnumAltService.THEALTENING)

                        if (NightX.moduleManager.getModule(Hud::class.java)?.flagSoundValue!!.get()) {
                            NightX.tipSoundManager.popSound.asyncPlay(90f)
                        }
                        status = "§cLogging in..."

                        // Set token as username
                        val yggdrasilUserAuthentication =
                            YggdrasilUserAuthentication(YggdrasilAuthenticationService(NO_PROXY, ""), MINECRAFT)
                        yggdrasilUserAuthentication.setUsername(account.token)
                        yggdrasilUserAuthentication.setPassword(NightX.CLIENT_BEST)

                        status = try {
                            yggdrasilUserAuthentication.logIn()

                            mc.session = Session(
                                yggdrasilUserAuthentication.selectedProfile.name, yggdrasilUserAuthentication
                                    .selectedProfile.id.toString(),
                                yggdrasilUserAuthentication.authenticatedToken, "mojang"
                            )
                            NightX.eventManager.callEvent(SessionEvent())

                            prevGui.status =
                                "§aYour name is now §b§l${yggdrasilUserAuthentication.selectedProfile.name}§c."
                            mc.displayGuiScreen(prevGui)
                            ""
                        } catch (e: AuthenticationException) {
                            GuiAltManager.altService.switchService(AltService.EnumAltService.MOJANG)

                            ClientUtils.getLogger().error("Failed to login.", e)
                            "§cFailed to login: ${e.message}"
                        }
                    } catch (throwable: Throwable) {
                        if (NightX.moduleManager.getModule(Hud::class.java)?.flagSoundValue!!.get()) {
                            NightX.tipSoundManager.popSound.asyncPlay(90f)
                        }
                        status = "§cFailed to login. Unknown error."
                        ClientUtils.getLogger().error("Failed to login.", throwable)
                    }

                    loginButton.enabled = true
                    generateButton.enabled = true
                }.handle { _, err ->
                    status = "§cFailed to generate account."
                    ClientUtils.getLogger().error("Failed to generate account.", err)
                }.whenComplete { _, _ ->
                    loginButton.enabled = true
                    generateButton.enabled = true
                }
            }

            2 -> {
                loginButton.enabled = false
                generateButton.enabled = false

                Thread(Runnable {
                    try {
                        status = "§cSwitching Alt Service..."

                        // Change Alt Service
                        GuiAltManager.altService.switchService(AltService.EnumAltService.THEALTENING)
                        if (NightX.moduleManager.getModule(Hud::class.java)?.flagSoundValue!!.get()) {
                            NightX.tipSoundManager.popSound.asyncPlay(90f)
                        }
                        status = "§cLogging in..."

                        // Set token as username
                        val yggdrasilUserAuthentication =
                            YggdrasilUserAuthentication(YggdrasilAuthenticationService(NO_PROXY, ""), MINECRAFT)
                        yggdrasilUserAuthentication.setUsername(tokenField.text)
                        yggdrasilUserAuthentication.setPassword(NightX.CLIENT_BEST)

                        status = try {
                            yggdrasilUserAuthentication.logIn()

                            mc.session = Session(
                                yggdrasilUserAuthentication.selectedProfile.name, yggdrasilUserAuthentication
                                    .selectedProfile.id.toString(),
                                yggdrasilUserAuthentication.authenticatedToken, "mojang"
                            )
                            NightX.eventManager.callEvent(SessionEvent())

                            prevGui.status =
                                "§aYour name is now §b§l${yggdrasilUserAuthentication.selectedProfile.name}§c."
                            mc.displayGuiScreen(prevGui)
                            "§aYour name is now §b§l${yggdrasilUserAuthentication.selectedProfile.name}§c."
                        } catch (e: AuthenticationException) {
                            GuiAltManager.altService.switchService(AltService.EnumAltService.MOJANG)

                            ClientUtils.getLogger().error("Failed to login.", e)
                            "§cFailed to login: ${e.message}"
                        }
                    } catch (throwable: Throwable) {
                        if (NightX.moduleManager.getModule(Hud::class.java)?.flagSoundValue!!.get()) {
                            NightX.tipSoundManager.popSound.asyncPlay(90f)
                        }
                        ClientUtils.getLogger().error("Failed to login.", throwable)
                        status = "§cFailed to login. Unknown error."
                    }

                    loginButton.enabled = true
                    generateButton.enabled = true
                }).start()
            }

            3 -> MiscUtils.showURL("https://thealtening.com/free/free-minecraft-alts")
        }
    }

    /**
     * Handle key typed
     */
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        // Check if user want to escape from screen
        if (Keyboard.KEY_ESCAPE == keyCode) {
            // Send back to prev screen
            mc.displayGuiScreen(prevGui)
            return
        }

        // Check if field is focused, then call key typed
        if (apiKeyField.isFocused) apiKeyField.textboxKeyTyped(typedChar, keyCode)
        if (tokenField.isFocused) tokenField.textboxKeyTyped(typedChar, keyCode)
        super.keyTyped(typedChar, keyCode)
    }

    /**
     * Handle mouse clicked
     */
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        // Call mouse clicked to field
        apiKeyField.mouseClicked(mouseX, mouseY, mouseButton)
        tokenField.mouseClicked(mouseX, mouseY, mouseButton)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Handle screen update
     */
    override fun updateScreen() {
        apiKeyField.updateCursorCounter()
        tokenField.updateCursorCounter()
        super.updateScreen()
    }

    /**
     * Handle gui closed
     */
    override fun onGuiClosed() {
        // Disable keyboard repeat events
        Keyboard.enableRepeatEvents(false)

        // Set API key
        apiKey = apiKeyField.text
        super.onGuiClosed()
    }
}