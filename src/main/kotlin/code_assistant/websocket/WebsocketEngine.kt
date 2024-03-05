package code_assistant.websocket

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI;
import org.json.JSONObject
import java.lang.Exception

class WebsocketEngine(serverUri: URI) : WebSocketClient(serverUri) {
    override fun onOpen(handshakedata: ServerHandshake?) {
        println("WebSocket connection opened")

        val messagesObject = JSONObject()
        messagesObject.put("jsonrpc", "2.0")
        messagesObject.put("id", 1)
        messagesObject.put("method", "ai/conversation")

        val paramsData = JSONObject()
        paramsData.put("sessionId", "Sx5McJOccMAR3MfU")
        paramsData.put("parentMessageId", "MO7J6rmDba0KOvuX")

        val contentData = JSONObject()
        contentData.put("type", "text")
        contentData.put("message", "帮我写一个排序")
        contentData.put("language", "NodeJS")
        paramsData.put("content", contentData)

        val senderData = JSONObject()
        senderData.put("type", "chat")
        paramsData.put("sender", senderData)
        paramsData.put("model", "gpt-35-turbo")
        paramsData.put("temperature", 0)
        paramsData.put("maxToken", 4096)

        messagesObject.put("params", paramsData)
        println(messagesObject.toString())
        // send(messagesObject.toString())
    }

    override fun onMessage(message: String?) {
        println("Received message: $message")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("WebSocket connection closed：$code: $reason")
    }

    override fun onError(ex: Exception?) {
        println("WebSocket error occurred")
    }

}