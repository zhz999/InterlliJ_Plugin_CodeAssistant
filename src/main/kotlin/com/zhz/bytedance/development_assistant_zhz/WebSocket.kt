package com.zhz.bytedance.development_assistant_zhz

//import javax.websocket.client.WebSocketClient
//import javax.websocket.extensions.WebSocketExtension
//import javax.websocket.message.TextMessage
//
//fun main() {
//    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzZXJ2aWNlIjoiZG9yYWRvLWNvcGlsb3QiLCJ0ZW5hbnRJZCI6MCwiZXhwIjoxNzA5MTk5NTkzLCJ1c2VyIjoiemhhbmdob25nemhvbmcifQ.sR2Cv-PyMttc712e5OIiOcICKkxsGV9zJeiTJRi_N1BDtlk6VI6LYDivNaI4OPdBNhtpI61N4lkZKPUtkRnVRo_b1u0vFxaAmLwUapNgJbNRmXuF2e3rKlcK2-waZ3W_K0Yh7EfIs9Nm7WkqLE2prAtWqrlWQP-USnXuQzcWmTN1DtXTxz1kQ7bSSCEDSaELCSp4KQF_KBccRciwtFchHYCQ8ww6JRTsw_pj7JVkbkwbxJlITy61N4P1K7XVsJZIzt78gag7qRGfVbVy3zny5hFpysqsvx9nx1Z4Qqm92pL7HZ-rznnJvUPxJpaVjlADq0pF7D4IyUPn-VtyVzYOoQ"
//    val url = "wss://data.bytedance.net/socket-dorado/copilot/v1/socket?token=${token}"
//    val wsClient = WebSocketClient.Builder().build()
//
//    try {
//        wsClient.connect(url) { ws ->
//            println("Connected to WebSocket endpoint: $url")
//
//            // Send a message to the server
//            ws.sendMessage(TextMessage("Hello, world!"))
//
//            // Listen for messages from the server
//            ws.onMessage { message ->
//                println("Received message: ${message.text}")
//            }
//
//            // Close the connection
//            ws.close()
//            println("Disconnected from WebSocket endpoint")
//        }
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//}