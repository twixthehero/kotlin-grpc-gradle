package hello.client

import hello.HelloGrpc
import hello.HelloReply
import hello.HelloRequest
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class HelloClient internal constructor(private val channel: ManagedChannel) {
    private val blockingStub = HelloGrpc.newBlockingStub(channel)

    constructor(host: String, port: Int) : this(
        ManagedChannelBuilder.forAddress(
            host,
            port
        ).usePlaintext().build()
    )

    @Throws(InterruptedException::class)
    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    fun sayHello(name: String) {
        logger.info("Will try to say hello to $name")
        val request = HelloRequest.newBuilder().setName(name).build()
        val response: HelloReply = try {
            blockingStub.sayHello(request)
        } catch (e: StatusRuntimeException) {
            logger.warning("RPC failed: ${e.status}")
            return
        }

        logger.info("response: ${response.message}")
    }

    companion object {
        private val logger = Logger.getLogger(HelloClient::class.java.name)

        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val client = HelloClient("localhost", 5000)
            try {
                val user = if (args.isNotEmpty()) args[0] else "world"
                client.sayHello(user)
            } finally {
                client.shutdown()
            }
        }
    }
}