import java.net.ServerSocket

private const val port = 6667
private val server = ServerSocket(port)
val clients = ArrayList<Client>()

fun main() {
    while (true) {
        val client = server.accept()
        clients.add(Client(client))
    }
}

