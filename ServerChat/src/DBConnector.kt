import java.nio.file.Files
import java.nio.file.Paths
import java.sql.*
import java.sql.Date
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.collections.HashMap

object DBConnector {
    private val queries: ExecutorService = Executors.newSingleThreadExecutor()
    private val statements = HashMap<String, PreparedStatement>()
    init {
        val props = Properties()
        props.load(Files.newInputStream(Paths.get("database.properties")))
        val con: Connection = with(props) {
            DriverManager.getConnection(getProperty("url"), getProperty("user"), getProperty("password"))
        }

        val loadStatement = { name: String -> con.prepareStatement(props.getProperty(name)) }
        props.load(Files.newInputStream(Paths.get("queries.properties")))
        for (name in props.keys)
            statements[name as String] = loadStatement(name)
    }
    val hasUser: (String) -> Future<ResultSet> = { login ->
        getFuture {
            with(statements["hasUser"]!!) {
                setString(1, login)
                executeQuery()
            }
        }
    }
    val checkUser: (String, String) -> Future<ResultSet> = { login, password ->
        getFuture {
            with(statements["checkUser"]!!) {
                setString(1, login)
                setString(2, password)
                executeQuery()
            }
        }
    }
    val addUser: (Int, String, String) -> Unit = { id, login, password ->
        queries.submit {
            with(statements["addUser"]!!) {
                setInt(1, id)
                setString(2, login)
                setString(3, password)
                executeUpdate()
            }
        }
    }
    val userCount: () -> Future<ResultSet> = { getFuture { statements["userCount"]!!.executeQuery() } }
    val saveMessage: (String, String, Date) -> Unit = { user, content, date ->
        queries.submit {
            with(statements["saveMessages"]!!) {
                setString(1, user)
                setString(2, content)
                setDate(3, date)
                executeUpdate()
            }
        }
    }
    val loadMessages: () -> Future<ResultSet> = { getFuture { statements["loadMessages"]!!.executeQuery() } }

    private fun getFuture(fn: () -> ResultSet): Future<ResultSet> = queries.submit(fn)
}