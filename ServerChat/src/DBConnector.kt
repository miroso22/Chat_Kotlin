import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object DBConnector {
    val queries: ExecutorService = Executors.newSingleThreadExecutor()
    private val hasUser: PreparedStatement
    private val checkUser: PreparedStatement
    private val addUser: PreparedStatement
    private val userCount: PreparedStatement
    init {
        val props = Properties()
        props.load(Files.newInputStream(Paths.get("database.properties")))
        val con: Connection = with(props) {
            DriverManager.getConnection(getProperty("url"), getProperty("user"), getProperty("password"))
        }
        props.load(Files.newInputStream(Paths.get("queries.properties")))
        val loadStatement = { name: String -> con.prepareStatement(props.getProperty(name)) }
        hasUser = loadStatement("hasUser")
        checkUser = loadStatement("checkUser")
        addUser = loadStatement("addUser")
        userCount = loadStatement("userCount")
    }
    val HasUser = { login: String ->
        with(hasUser) {
            setString(1, login)
            executeQuery()
        }
    }
    val CheckUser = { login: String, password: String ->
        with(checkUser) {
            setString(1, login)
            setString(2, password)
            executeQuery()
        }
    }
    val AddUser = { id: Int, login: String, password: String ->
        with(addUser) {
            setInt(1, id)
            setString(2, login)
            setString(3, password)
            executeUpdate()
        }
    }
    val UserCount = { userCount.executeQuery() }
}