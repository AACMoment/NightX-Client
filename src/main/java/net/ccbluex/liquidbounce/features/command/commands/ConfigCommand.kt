package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.utils.SettingsUtils
import net.ccbluex.liquidbounce.utils.misc.StringUtils
import java.io.File
import java.io.IOException
import java.util.*

class ConfigCommand : Command("config", emptyArray()) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            when {
                args[1].equals("load", ignoreCase = true) -> {
                    if (args.size > 2) {
                        val scriptFile = File(LiquidBounce.fileManager.settingsDir, args[2])
                        if (scriptFile.exists()) {
                            try {
                                val settings = scriptFile.readText()
                                SettingsUtils.executeScript(settings)
                                chat("§6Config updated successfully!")
                                LiquidBounce.hud.addNotification(
                                    Notification(
                                        "Config updated successfully!",
                                        Notification.Type.SUCCESS
                                    )
                                )
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            return
                        }
                        return
                    }
                    chatSyntax("config load <name>")
                    return
                }

                args[1].equals("save", ignoreCase = true) -> {
                    if (args.size > 2) {
                        val scriptFile = File(LiquidBounce.fileManager.settingsDir, args[2])

                        try {
                            if (scriptFile.exists())
                                if (scriptFile.delete()) {
                                    scriptFile.createNewFile()
                                    chat("§aSuccessfully saved new config!")
                                } else {
                                    return
                                }

                            val option =
                                if (args.size > 3) StringUtils.toCompleteString(args, 3).lowercase(Locale.getDefault()) else "all"
                            val values = option.contains("all") || option.contains("values")
                            val binds = option.contains("all") || option.contains("binds")
                            val states = option.contains("all") || option.contains("states")
                            if (!values && !binds && !states) {
                                return
                            }
                            val settingsScript = SettingsUtils.generateScript(values, binds, states)
                            scriptFile.writeText(settingsScript)
                            chat("§aSuccessfully saved new config!")
                        } catch (throwable: Throwable) {
                        }
                        return
                    }

                    chatSyntax("config save <name>")
                    return
                }

                args[1].equals("delete", ignoreCase = true) -> {
                    if (args.size > 2) {
                        val scriptFile = File(LiquidBounce.fileManager.settingsDir, args[2])

                        if (scriptFile.exists()) {
                            scriptFile.delete()
                            chat("§6Config deleted successfully!")
                            return
                        }
                        return
                    }
                    chatSyntax("config delete <name>")
                    return
                }

                args[1].equals("list", ignoreCase = true) -> {
                    chat("§cConfigs:")

                    val settings = this.getLocalSettings() ?: return

                    for (file in settings)
                        chat("" + file.name)
                    return
                }
            }
        }
        chatSyntax("config <load/save/list/delete>")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("delete", "list", "load", "save").filter { it.startsWith(args[0], true) }
            2 -> {
                when (args[0].lowercase(Locale.getDefault())) {
                    "delete", "load" -> {
                        val settings = this.getLocalSettings() ?: return emptyList()

                        return settings
                            .map { it.name }
                            .filter { it.startsWith(args[1], true) }
                    }
                }
                return emptyList()
            }

            3 -> {
                if (args[0].equals("save", true)) {
                    return listOf("all", "states", "binds", "values").filter { it.startsWith(args[2], true) }
                }
                return emptyList()
            }

            else -> emptyList()
        }
    }

    private fun getLocalSettings(): Array<File>? = LiquidBounce.fileManager.settingsDir.listFiles()
}