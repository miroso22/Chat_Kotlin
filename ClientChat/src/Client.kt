import java.net.Socket
import java.net.SocketException

const val port = 6667
var socket: Socket = Socket()

fun main() {
    try {
        socket = Socket("localhost", port)
    } catch (e: SocketException) {
        println("Cannot connect to server!")
        return
    }

    Sender.start()
    Getter.start()
}
