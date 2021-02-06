package com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.data


data class SocketPacket(
    val operation: String? = null,
    val username: String? = null,
    val password: String? = null,
    val token: String? = null,
    val error: String? = null,
    val success: String? = null
)


/*

        Executors.newSingleThreadExecutor().execute {
            val socket = Socket("10.0.2.2", 3000)


            val send = SocketPacket(
                user = "user",
                key = "key"
            )

            val json = Gson().toJson(send)

            socket.outputStream.write(json.encodeToByteArray())
            socket.outputStream.write("\n".encodeToByteArray())
            socket.outputStream.flush()

        }


 */