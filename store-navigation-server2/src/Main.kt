import java.io.OutputStream
import java.net.ServerSocket
import java.sql.DriverManager
import kotlin.concurrent.thread

fun main() {
    val server = ServerSocket(8080)
    println("Сервер запущен на порту 8080...")

    while (true) {
        val socket = server.accept()
        thread {
            try {
                val reader = socket.getInputStream().bufferedReader()
                val line = reader.readLine() ?: return@thread

                val path = line.split(" ")[1]
                val output: OutputStream = socket.getOutputStream()

                if (path == "/") {
                    val response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\nСервер мобильной навигации работает!"
                    output.write(response.toByteArray())
                } else if (path.startsWith("/search")) {
                    val barcode = path.substringAfter("barcode=", "")

                    if (barcode.isEmpty()) {
                        val response = "HTTP/1.1 400 Bad Request\r\n\r\nMissing barcode"
                        output.write(response.toByteArray())
                    } else {
                        // ПОДКЛЮЧЕНИЕ К БАЗЕ PostgreSQL
                        // Обязательно замени '1234' на свой пароль, который ты вводил при установке!
                        val conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/store_navigation_db", "postgres", "1234")
                        val query = conn.prepareStatement("SELECT * FROM products WHERE barcode = ?")
                        query.setString(1, barcode)
                        val resultSet = query.executeQuery()

                        if (resultSet.next()) {
                            val name = resultSet.getString("name")
                            val price = resultSet.getDouble("price")
                            val shelfId = resultSet.getInt("shelf_id")
                            val instruction = resultSet.getString("instruction")

                            val json = """{"name":"$name","barcode":"$barcode","price":$price,"shelfId":$shelfId}"""
                            val response = "HTTP/1.1 200 OK\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n$json"
                            output.write(response.toByteArray())
                        } else {
                            val response = "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\nТовар не найден"
                            output.write(response.toByteArray())
                        }
                        conn.close()
                    }
                }
                output.flush()
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}