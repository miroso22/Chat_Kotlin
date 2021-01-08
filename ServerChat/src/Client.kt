import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketException
import java.sql.ResultSet
import java.util.Date

class Client(client: Socket) : Thread() {
    private val reader = BufferedReader(InputStreamReader(client.getInputStream()))
    private val writer = BufferedWriter(OutputStreamWriter(client.getOutputStream()))
    private var login = ""
    init { start() }

    override fun run() {
        var message: String
        try {
            val loginChecked = checkLogin()
            if (loginChecked) {
                while (!checkPassword()) {
                    send("Wrong password. Try again")
                }
            } else if (!register()) {
                send("Disconnecting...")
                return
            }

            println("User $login connected")
            loadMessages()
            while (true) {
                message = reader.readLine() ?: return
                clients.forEach { if (it != this) it.send(message, login) }
                DBConnector.saveMessage(login, message, java.sql.Date(Date().time))
            }
        } catch (e: SocketException) {
            println("User $login disconnected")
        } finally { reader.close(); writer.close() }
    }

    fun send(message: String, login: String = "Server") {
        writer.write("<$login>: $message\n")
        writer.flush()
    }

    private fun checkLogin(): Boolean {
        send("Enter login: ")
        login = reader.readLine() ?: return false
        val exists: ResultSet = DBConnector.hasUser(login).get()
        val res = exists.next() && exists.getInt(1) > 0
        exists.close()
        return res
    }
    private fun checkPassword(): Boolean {
        send("Enter password: ")
        val password = reader.readLine() ?: return false
        val correct = DBConnector.checkUser(login, password).get()
        val res = correct.next()
        correct.close()
        return res
    }
    private fun register(): Boolean {
        send("Do you want to register? (Y/N)")
        if (reader.readLine().toUpperCase() == "Y") {
            send("Enter password:")
            val password = reader.readLine() ?: return false
            val resultSet = DBConnector.userCount().get()
            val id = if (resultSet.next()) resultSet.getInt(1) else return false
            resultSet.close()
            DBConnector.addUser(id, login, password)
            return true
        }
        return false
    }

    private fun loadMessages() {
        val messages: ResultSet = DBConnector.loadMessages().get()
        while (messages.next()) {
            val user = messages.getString(1)
            val message = messages.getString(2)
            send(message, user)
        }
    }
}
