import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object Sender : Thread() {
    private val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
    private val reader = BufferedReader(InputStreamReader(System.`in`))

    override fun run() {
        while (true) {
            val message = reader.readLine()
            writer.write("$message\n")
            writer.flush()
        }
    }
}
