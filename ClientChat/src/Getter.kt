import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.SocketException
import kotlin.system.exitProcess

object Getter : Thread() {
    private val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

    override fun run() {
        var message: String
        while (true) {
            try {
                message = reader.readLine() ?: return
            } catch (e: SocketException) {
                println("Server stopped!")
                exitProcess(1)
            }
            println(message)
        }
    }
}
