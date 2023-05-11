package database.tables

import client.MapleCharacter
import client.MapleClient
import database.DatabaseConnection.Companion.getConnection
import database.DatabaseStatements.Update
import tools.FilePrinter
import java.sql.SQLException
import java.util.*

class AccountsTbl {
    companion object {

        /**
         * This specific method is used to set the entire accounts table to "loggedin" = 0
         * on server init
         *
         * @param status
         */
        @JvmStatic
        fun updateLoggedInStatus(status: Int) {
            try {
                getConnection().use { con ->
                    Update("accounts").set("loggedin", status).execute(
                        con!!
                    )
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        /**
         * update the "loggedin" status of certain accounts
         */
        @JvmStatic
        fun updateLoggedInStatus(status: Int, accountId: Int) {
            try {
                getConnection().use { con ->
                    Update("accounts")
                        .set("loggedin", status)
                        .set("id", accountId)
                        .execute(
                        con!!
                    )
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun updateBanStatus(accountId: Int, isBanned: Boolean, reason: String) {
            try {
                getConnection().use { con ->
                    Update("accounts").set("banned", isBanned).set("banreason", reason).where("id", accountId).execute(
                        con!!
                    )
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun loadGriefReason(accountId: Int): Byte {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT greason FROM accounts WHERE id = ?").use { ps ->
                        ps.setInt(1, accountId)
                        ps.executeQuery().use { rs ->
                            if (rs.next()) {
                                return rs.getByte("greason")
                            }
                        }
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return 0
        }

        @JvmStatic
        fun updateCheaterStatus(user: MapleCharacter, reason: String, isCheater: Boolean) {
            if (!isCheater) {
                try {
                    getConnection().use { con ->
                        Update("accounts").set("cheater", true).set("banreason", reason).where("id", user.accountID)
                            .execute(
                                con!!
                            )
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            } else {
                try {
                    getConnection().use { con ->
                        Update("accounts").set("cheater", false).where("id", user.accountID).execute(
                            con!!
                        )
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }

        @JvmStatic
        fun updateHardwareId(accountId: Int, hwid: String) {
            try {
                getConnection().use { con ->
                    Update("accounts").set("hwid", hwid).where("id", accountId).execute(
                        con!!
                    )
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun loadHardwareId(accountId: Int): String {
            getConnection().use { con ->
                con!!.prepareStatement("SELECT hwid FROM accounts WHERE id = ?").use { ps ->
                    ps.setInt(1, accountId)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            return rs.getString("hwid")
                        }
                    }
                }
            }
            return ""
        }

        @JvmStatic
        fun updateMacAddress(accountId: Int, macAddress: String) {
            try {
                getConnection().use { con ->
                    Update("accounts").set("macs", macAddress).where("id", accountId).execute(
                        con!!
                    )
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun updateGender(gender: Byte, accountId: Int) {
            try {
                getConnection().use { con ->
                    Update("accounts").set("gender", gender).where("id", accountId).execute(
                        con!!
                    )
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun updatePin(pin: String, accountId: Int) {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("UPDATE accounts SET pin = ? WHERE id = ?").use { ps ->
                        ps.setString(1, pin)
                        ps.setInt(2, accountId)
                        ps.executeUpdate()
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun updatePic(pic: String, accountId: Int) {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("UPDATE accounts SET pic = ? WHERE id = ?").use { ps ->
                        ps.setString(1, pic)
                        ps.setInt(2, accountId)
                        ps.executeUpdate()
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun updateLoginState(state: Int, accountId: Int) {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("UPDATE accounts SET loggedin = ?, lastlogin = now() WHERE id = ?")
                        .use { ps ->
                            ps.setInt(1, state)
                            ps.setInt(2, accountId)
                            ps.executeUpdate()
                        }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun updateCharacterSlots(newAmount: Int, accountId: Int) {
            try {
                getConnection().use { con ->
                    Update("accounts").set("characterslots", newAmount).where("id", accountId).execute(con!!)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun updateLastKnownIP(accountName: String, client: MapleClient) {
            try {
                getConnection().use { con ->
                    val statement = Update("accounts")
                    statement.cond("name", accountName.lowercase(Locale.getDefault()))
                    statement.set("lastknownip", client.session.remoteAddress.toString())
                    statement.execute(con!!)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                FilePrinter.print(
                    FilePrinter.LOGIN_ATTEMPTS,
                    "Updating the lastknownip " + client.session.remoteAddress.toString() + " has failed for player " + accountName.lowercase(
                        Locale.getDefault()
                    )
                )
            }
        }

        @JvmStatic
        fun updateNXCash(accountId: Int, nxCredit: Int, maplePoint: Int, nxPrepaid: Int) {
            try {
                getConnection().use { con ->
                    Update("accounts")
                        .set("nxcredit", nxCredit)
                        .set("maplepoint", maplePoint)
                        .set("nxprepaid", nxPrepaid)
                        .where("id", accountId).execute(
                            con!!
                        )
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun loadNXCash(accountId: Int): Triple<Int, Int, Int> {
            getConnection().use { con ->
                con!!.prepareStatement("SELECT nxCredit, maplePoint, nxPrepaid FROM accounts WHERE id = ?").use { ps ->
                        ps.setInt(1, accountId)
                        ps.executeQuery().use { rs ->
                            if (rs.next()) {
                                val credit = rs.getInt("nxCredit")
                                val point = rs.getInt("maplePoint")
                                val prepaid = rs.getInt("nxPrepaid")

                                return Triple(credit, point, prepaid)
                            }
                        }
                    }
                return throw UnsupportedOperationException()
            }
        }

        @JvmStatic
        fun updatePassword(accountName: String, password: String) {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("UPDATE accounts SET password = ? WHERE name = ?;").use { ps ->
                        ps.setString(1, password)
                        ps.setString(2, accountName)
                        ps.executeUpdate()
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun hasAcceptedTOS(accountId: Int): Boolean {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT tos FROM accounts WHERE id = ?").use { ps ->
                        ps.setInt(1, accountId)
                        ps.executeQuery().use { rs ->
                            if (rs.next()) {
                                if (rs.getBoolean("tos")) {
                                    return true
                                }
                            }
                        }
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return false
        }

        @JvmStatic
        fun updateTOS(accept: Boolean, accountId: Int) {
            try {
                getConnection().use { con ->
                    Update("accounts").set("tos", accept).where("id", accountId).execute(
                            con!!
                        )
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun updateLanguage(language: Int, accountId: Int) {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("UPDATE accounts SET language = ? WHERE id = ?").use { ps ->
                        ps.setInt(1, language)
                        ps.setInt(2, accountId)
                        ps.executeUpdate()
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }
}
