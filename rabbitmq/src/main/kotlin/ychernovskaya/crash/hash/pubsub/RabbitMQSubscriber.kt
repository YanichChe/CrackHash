package ychernovskaya.crash.hash.pubsub

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import java.lang.Exception

data class SubscriberContext(
    val queueName: String,
    val consumerTag: String,
    val routingKey: String
)

interface RabbitMQSubscriber {
    suspend fun subscribe(callBack: (message: ByteArray) -> Unit)
}

class RabbitMQSubscriberImpl(
    private val subscriberContext: SubscriberContext,
    connection: Connection
) : RabbitMQSubscriber {
    var channel = connection.createChannel()

    val autoAck = false
    override suspend fun subscribe(callBack: (message: ByteArray) -> Unit) {
        channel.basicConsume(subscriberContext.queueName, autoAck, subscriberContext.consumerTag,
            object : DefaultConsumer(channel) {
                override fun handleDelivery(
                    consumerTag: String?,
                    envelope: Envelope,
                    properties: AMQP.BasicProperties?,
                    body: ByteArray
                ) {
                    val routingKey = envelope.routingKey
                    val deliveryTag = envelope.deliveryTag

                    if (subscriberContext.routingKey == routingKey) {
                        try {
                            callBack(body)
                            channel.basicAck(deliveryTag, false)
                        } catch (_: Exception) {
                            channel.basicReject(deliveryTag, false)
                        }
                    } else {
                        channel.basicNack(deliveryTag, false, true)
                    }
                }
            }
        )
    }
}