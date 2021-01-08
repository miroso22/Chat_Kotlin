import java.nio.file.Files
import java.nio.file.Paths
import java.sql.*
import java.sql.Date
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.collections.HashMap
import kotlin.concurrent.fixedRateTimer

object DBConnector {
    private val queries: ExecutorService = Executors.newSingleThreadExecutor()
    private val statements = HashMap<String, PreparedStatement>()

    init {
        val props = Properties()
        props.load(Files.newInputStream(Paths.get("ServerChat/database.properties")))
        val con: Connection = with(props) {
            DriverManager.getConnection(getProperty("url"), getProperty("user"), getProperty("password"))
        }

        fun loadStatement(name: String) = con.prepareStatement(props.getProperty(name))
        props.load(Files.newInputStream(Paths.get("ServerChat/queries.properties")))
        for (name in props.keys)
            statements[name as String] = loadStatement(name)

        fixedRateTimer(initialDelay = 10000, period = 10000, action = { clearOldMessages() })
    }

    fun hasUser(login: String): Future<ResultSet> {
        return getFuture {
            with(statements["hasUser"]!!) {
                setString(1, login)
                executeQuery()
            }
        }
    }

    fun checkUser(login: String, password: String): Future<ResultSet> {
        return getFuture {
            with(statements["checkUser"]!!) {
                setString(1, login)
                setString(2, password)
                executeQuery()
            }
        }
    }
    fun userCount(): Future<ResultSet> =
        getFuture { statements["userCount"]!!.executeQuery() }

    fun loadMessages(): Future<ResultSet> =
        getFuture { statements["loadMessages"]!!.executeQuery() }

    fun addUser(id: Int, login: String, password: String) {
        queries.execute {
            with(statements["addUser"]!!) {
                setInt(1, id)
                setString(2, login)
                setString(3, password)
                executeUpdate()
            }
        }
    }
    fun saveMessage(user: String, content: String, date: Date) {
        queries.execute {
            with(statements["saveMessage"]!!) {
                setString(1, user)
                setString(2, content)
                setDate(3, date)
                executeUpdate()
            }
        }
    }

    private fun getMessageCount(): Future<ResultSet> =
        getFuture { statements["messageCount"]!!.executeQuery() }

    private fun clearOldMessages() {
        println(1)
        val resultSet = getMessageCount().get()
        if (resultSet.next() && resultSet.getInt(1) > 80) {
            queries.submit {
                statements["clearOldestMessages"]!!.executeUpdate()
            }
            println("Deleted!")
        }
    }

    private fun getFuture(fn: () -> ResultSet): Future<ResultSet> = queries.submit(fn)
}
