import java.net.Socket
import java.net.SocketException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val executor: ExecutorService = Executors.newCachedThreadPool()

fun main(args: Array<String>) {
    connectToServer()
}

fun connectToServer() {
    val socket = Socket("localhost", DEFAULT_SERVER_PORT)

    // Reading from server in separate thread
    startListeningServer(socket)

    val outputStream = SocketWriter(socket.outputStream)

    val scanner = Scanner(System.`in`)

    println("What is your name?")
    val clientName = scanner.nextLine()
    sendMessage(clientName, outputStream)

    outputStream.write(clientName)

    while (!socket.isClosed) {
        val messageToServer = scanner.nextLine()

        when (messageToServer?.toLowerCase()) {
            "exit" -> socket.close()
            else -> sendMessage(messageToServer, outputStream)
        }
    }

    executor.shutdown()
}

private fun startListeningServer(socket: Socket) {
    startThread { readServer(socket) }
}

private fun sendMessage(messageToServer: String, outputStream: SocketWriter) {
    executor.execute { sendMessageToServer(messageToServer, outputStream) }
}

fun sendMessageToServer(messageToServer: String, outputStream: SocketWriter) {
    outputStream.write(messageToServer)
}

fun readServer(socket: Socket) {
    val serverReader = SocketReader(socket.inputStream)
    while (!socket.isClosed) {
        try {
            val messageFromServer = serverReader.readLine()
            println(messageFromServer)
        } catch (e: SocketException) {
            println(e.message)
        }
    }
}
