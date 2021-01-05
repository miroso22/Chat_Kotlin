import java.net.Socket

const val port = 6667
val socket = Socket("localhost", port)

fun main() {
    Sender.start()
    Getter.start()
}