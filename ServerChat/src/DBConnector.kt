import java.nio.file.Files
import java.nio.file.Paths
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
        val input = Files.newInputStream(Paths.get("database.properties"))
        val props = Properties()
        props.load(input)
        val url = props.getProperty("url")
        val user = props.getProperty("user")
        val password = props.getProperty("password")
        val con = DriverManager.getConnection(url, user, password)

        hasUser = con.prepareStatement("select count(*) from users where login = ?;")
        checkUser = con.prepareStatement("select * from users where login = ? && password = ?;")
        addUser = con.prepareStatement("insert into users (id, login, password) values (?, ?, ?);")
        userCount = con.prepareStatement("select count(*) from users;")
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