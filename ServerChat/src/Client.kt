import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketException
import java.sql.ResultSet
import java.util.*

class Client(client: Socket) : Thread() {
    private val reader = BufferedReader(InputStreamReader(client.getInputStream()))
    private val writer = BufferedWriter(OutputStreamWriter(client.getOutputStream()))
    private var login = ""
    init { start() }

    override fun run() {
        var message: String
        try {
            val loginChecked = checkLogin()
            if (!(loginChecked && checkPassword() || !loginChecked && register())) return
            println("User $login connected")
            loadMessages()
            while (true) {
                message = reader.readLine() ?: return
                clients.forEach { if (it != this) it.send(message, login) }
                saveMessage(message)
            }
        }
        catch (e: SocketException) { println("User $login disconnected") }
        finally { reader.close(); writer.close() }
    }

    private fun send(message: String, login: String = "Server") {
        writer.write("<$login>: $message\n")
        writer.flush()
    }

    private fun checkLogin(): Boolean {
        send("Enter login: ")
        login = reader.readLine() ?: return false
        val task = { DBConnector.HasUser(login) }
        val future = DBConnector.queries.submit(task)
        val exists: ResultSet = future.get()
        val res = exists.next() && exists.getInt(1) > 0
        exists.close()
        return res
    }
    private fun checkPassword(): Boolean {
        send("Enter password: ")
        val password = reader.readLine() ?: return false
        val task = { DBConnector.CheckUser(login, password) }
        val future = DBConnector.queries.submit(task)
        val correct = future.get()
        val res = correct.next()
        correct.close()
        return res
    }
    private fun register(): Boolean {
        send("Do you want to register? (Y/N)")
        if (reader.readLine().toUpperCase() == "Y") {
            send("Enter password:")
            val password = reader.readLine() ?: return false
            val task1 = { DBConnector.UserCount() }
            val future1 = DBConnector.queries.submit(task1)
            val resultSet = future1.get()
            val id = if (resultSet.next()) resultSet.getInt(1) else return false
            val task2 = { DBConnector.AddUser(id, login, password) }
            resultSet.close()
            DBConnector.queries.submit(task2)
            return true
        }
        return false
    }

    private fun saveMessage(message: String) {
        val task = { DBConnector.SaveMessage(login, message, java.sql.Date(Date().time)) }
        DBConnector.queries.submit(task)
    }
    private fun loadMessages() {
        val task = { DBConnector.LoadMessages() }
        val future = DBConnector.queries.submit(task)
        val messages = future.get()
        while (messages.next()) {
            val user = messages.getString(1)
            val message = messages.getString(2)
            send(message, user)
        }
    }
}