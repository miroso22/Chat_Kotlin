import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.Date
import java.sql.DriverManager
import java.sql.Statement

internal class DBConnectorTest {
    private var userCountBefore: Int

    companion object {
        private val con: Connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatDB?useUnicode=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true", "root", "pass")
        val statement: Statement = con.createStatement()

        @JvmStatic
        @AfterAll
        internal fun clearDB() {
            statement.executeUpdate("delete from users where login = 'test' and password = ''")
            statement.close()
            con.close()
            println(1)
        }
    }

    init {
        val resultSet = DBConnector.userCount().get()
        resultSet.next()
        userCountBefore = resultSet.getInt(1)
        statement.executeUpdate("insert into users(id, login, password) values(-1, 'test', '')")
    }

    @Test
    fun hasUser() {
        val expected = DBConnector.hasUser("test").get()
        assertTrue(expected.next() && expected.getInt(1) > 0)
    }

    @Test
    fun checkUser() {
        val expected = DBConnector.checkUser("test", "").get()
        assertTrue(expected.next(), "No user found")
    }

    @Test
    fun userCount() {
        val expected = DBConnector.userCount().get()
        expected.next()
        assertEquals(expected.getInt(1), userCountBefore + 1)
    }

    @Test
    fun loadMessages() {
        statement.executeUpdate("insert into messages(user, content, date) values('test', 'hello', null)")
        val resSet = DBConnector.loadMessages().get()
        var contains = false
        while (resSet.next()) {
            val user = resSet.getString(1)
            val message = resSet.getString(2)
            if (user == "test" && message == "hello") {
                contains = true
                break
            }
        }
        assertTrue(contains)
        statement.executeUpdate("delete from messages where user = 'test' and content = 'hello'")
    }

    @Test
    fun addUser() {
        DBConnector.addUser(-2, "1", "2")
        Thread.sleep(100)
        val resSet = statement.executeQuery("select * from users where id = -2 and login = '1' and password = '2'")
        assertTrue(resSet.next())
        statement.executeUpdate("delete from users where id = -2 and login = '1' and password = '2'")
    }

    @Test
    fun saveMessage() {
        DBConnector.saveMessage("test", "message", Date(100))
        val resSet = statement.executeQuery("select * from messages where user='test' and content='message'")
        assertTrue(resSet.next())
        statement.executeUpdate("delete from messages where user = 'test'")
    }
}
