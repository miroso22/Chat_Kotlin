import java.io.BufferedReader
import java.io.InputStreamReader

object Getter : Thread() {
    private val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

    override fun run() {
        var message: String
        while (true) {
            message = reader.readLine() ?: return
            println(message)
        }
    }
}
