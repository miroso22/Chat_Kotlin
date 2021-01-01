import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketException

class Client(client: Socket) : Thread() {
    private val reader : BufferedReader = BufferedReader(InputStreamReader(client.getInputStream()))
    private val writer : BufferedWriter = BufferedWriter(OutputStreamWriter(client.getOutputStream()))

    init { start() }

    override fun run() {
        var message: String
        try {
            while (true) {
                message = reader.readLine() ?: return
                clients.forEach { it.send(message) }
            }
        }
        catch (e: SocketException) { println("User disconnected") }
        finally { reader.close(); writer.close() }
    }

    private fun send(vararg messages: String) {
        messages.forEach { writer.write("$it\n") }
        writer.flush()
    }
}