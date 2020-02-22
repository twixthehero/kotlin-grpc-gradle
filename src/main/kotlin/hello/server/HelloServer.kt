package hello.server

import hello.HelloGrpc
import hello.HelloReply
import hello.HelloRequest
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import java.io.IOException
import java.util.logging.Logger

class HelloServer {
    private var _server: Server? = null
    private var server: Server
        get() = _server ?: throw AssertionError("server was null")
        set(value) {
            _server = value
        }

    @Throws(IOException::class)
    private fun start() {
        val port = 5000
        server = ServerBuilder.forPort(port).addService(HelloImpl()).build().start()
        logger.info("Server listening on port $port")
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down")
                this@HelloServer.stop()
                System.err.println("*** server shut down")
            }
        })
    }

    private fun stop() {
        server.shutdown()
    }

    @Throws(InterruptedException::class)
    private fun blockUntilShutdown() {
        server.awaitTermination()
    }

    internal class HelloImpl : HelloGrpc.HelloImplBase() {
        override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
            val reply = HelloReply.newBuilder().setMessage("Hello ${request?.name}").build()
            responseObserver?.onNext(reply)
            responseObserver?.onCompleted()
        }
    }

    companion object {
        private val logger = Logger.getLogger(HelloServer::class.java.name)

        @Throws(IOException::class, InterruptedException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val server = HelloServer()
            server.start()
            server.blockUntilShutdown()
        }
    }
}