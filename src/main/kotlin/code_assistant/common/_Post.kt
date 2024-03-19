package code_assistant.common

import code_assistant.settings.CodeAssistantSettingsState
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


/**
 *
 * 发送HTTP-POST,获取 Ws Token
 */
class DoradoCommon {

    companion object {

        val settings = CodeAssistantSettingsState.getInstance()

        fun getToken(): String {

            try {

                val url = URL("https://data.bytedance.net/socket-dorado/copilot/token")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json, text/plain, */*")
                connection.setRequestProperty(
                    "Cookie",
                    settings.token
                )

                // 发送POST数据
                val outputStream = connection.outputStream
                outputStream.flush()
                outputStream.close()

                // 获取响应数据
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    var token: String = ""
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream, "utf-8"))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val gson = Gson()
                        val jsonObject = gson.fromJson(line, JsonObject::class.java)
                        println(jsonObject.get("data").asString)
                        token = jsonObject.get("data").asString
                    }
                    reader.close()
                    return token
                } else {
                    Message.Error("Request Failed: errorCode:$responseCode")
                }
                return ""
            } catch (ex: Exception) {
                println(ex.localizedMessage)
                Message.Error("Request Exception：${ex.localizedMessage}")
                return ""
            }
        }

        fun sendGet(Url: String): String {
            val url = URL(Url)
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"
                // 设置请求头
                setRequestProperty("User-Agent", "Mozilla/5.0")
                val responseCode = responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    println(response.toString())
                    return response.toString()
                } else {
                    println("GET request failed. Response Code: $responseCode")
                    return responseCode.toString()
                }
            }
        }

        fun sendPost() {
            val url = URL("https://example.com/api/endpoint")
            val connection = url.openConnection() as HttpURLConnection
            // 设置请求方法为 POST
            connection.requestMethod = "POST"
            // 设置请求头部信息
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            // 启用输出流，用于发送请求体数据
            connection.doOutput = true
            // 构建请求体数据
            val requestBody = "{\"key1\":\"value1\", \"key2\":\"value2\"}"
            // 发送请求体数据
            val outputStream = DataOutputStream(connection.outputStream)
            outputStream.writeBytes(requestBody)
            outputStream.flush()
            outputStream.close()
            // 获取响应结果
            val responseCode = connection.responseCode
            val inputStream = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var inputLine: String?
            while (inputStream.readLine().also { inputLine = it } != null) {
                response.append(inputLine)
            }
            inputStream.close()
            // 打印响应结果
            println("Response Code: $responseCode")
            println("Response Body: $response")
        }
        
    }

}
