import java.net.Socket

const val port = 6666
val socket = Socket("localhost", port)

fun main() {
    Sender.start()
    Getter.start()
}