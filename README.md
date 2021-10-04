
# AmqpMathToolIntegration

## License

**MIT license. Please make sure to read and understand the file [LICENSE](./LICENSE)!**


## Author

Petri Kannisto, Tampere University, Finland


## Introduction

This application enables you to communicate via AMQP/RabbitMQ with Matlab.
It can be included as such in Matlab, because it is a Java library.
The reception of messages is synchronous, that is, you must poll for new messages.
Whenever one or more messages appear, they remain in a message queue in the
AMQP broker until you retrieve them.

If you want to retrieve messages as events as soon as they arrive in the queue, please look at [https://github.com/kannisto/Cocop.AmqpMathToolConnector](https://github.com/kannisto/Cocop.AmqpMathToolConnector).

While AMQP provides multiple messaging modes, this application only uses topic-based communication.

This repository contains an entire Eclipse workspace. The included applications are as follows:

* AmqpMathToolIntegration: the actual connector application
* AmqpMathToolIntegrationTest: console application to test connecting with the message bus
* AmqpPropsManagerUnitTest: JUnit unit test for the AmqpPropsManager class


## Environment and Libraries

For execution, the following should do it:

* Java: JDK/JRE 8 or newer
* Matlab: 2017 or newer

The following libraries were utilised in development:

* amqp-client-4.2.2.jar
    * see https://www.rabbitmq.com/download.html
* amqp-client-4.2.2-javadoc.jar
* commons-logging-1.2.jar
* slf4j-api-1.7.25.jar
* slf4j-nop-1.7.25.jar


## Known Limitations

The software always assumes '/' as the vhost on AMQP server, which is the
default.
Therefore, you cannot presumably connect to CloudAMQP that forces you to use
another vhost.


## Usage in Matlab

To utilise the AMQP connector in Matlab, you can follow these instructions.

* If you are new to RabbitMQ, you should first try it without Matlab
    * Matlab adds more difficulty to the first experiments
    * For tutorials, see https://www.rabbitmq.com/getstarted.html
* To debug your code, it is advisable to implement a simple publisher and subscriber in another environment

Once you have established a connection, you communicate in Matlab as follows:

* To publish (or send) a message, you simply call the respective function
* To receive messages
    * you listen to one or more topics
    * you call the dedicated function to check if any messages have arrived


### Adding JAR libraries to classpath

Matlab must have an access to the required JAR libraries. Steps:

1. Retrieve "AmqpMathToolIntegration" as a JAR file
    * alternatively, you can build your own JAR from source code
2. Retrieve the following libraries as JAR files (it is unclear if the versions can be different):
    * amqp-client-4.2.2.jar
        * see https://www.rabbitmq.com/download.html
    * commons-logging-1.2.jar
    * slf4j-api-1.7.25.jar
    * slf4j-nop-1.7.25.jar
3. Copy the JAR files to whatever folder you want to (such as 'C:\\myclasspath')
    * please avoid a filepath with spaces
4. In your Matlab preferences folder, create a file called 'javaclasspath.txt'
    * to locate this folder, use the 'prefdir' command
        * see https://se.mathworks.com/help/matlab/ref/prefdir.html
    * in the classpath file, add the full path of each JAR file
        * e.g., ```C:\myclasspath\procemplus_amqpmathtoolintegration.jar```


### Receiving messages with message queue

The following code creates an object that will receive messages from the specified topic to a message queue.
The message queue is located in the broker server, and any received messages remain there until you ask
to receive them.

* You can listen to as many topics as needed
    * you must specify all topics as a constructor parameter
* Replace the parameters ('myhost.com', etc.) with the ones relevant to you

```
% Specifying topics to listen to
topicIn1 = 'topic.in.1';
topicIn2 = 'topic.in.2';

% Specify AMQP properties
amqpProps = fi.procemplus.amqp2math.AmqpPropsManager('myhost.com', 'my.exchange', 'user-x', 'my-password');

% If using a non-secure connection (i.e., no encryption with TLS):
amqpProps.setSecure(false);

% If you need to enable "durable" and "auto delete" flags for the exchange:
amqpProps.setExchangeDurable(true);
amqpProps.setExchangeAutoDelete(true);
 
% Specify topics to listen to
topicsIn = javaArray('java.lang.String', 2);
topicsIn(1) = java.lang.String(topicIn1);
topicsIn(2) = java.lang.String(topicIn2);
 
% Set up AMQP connector
amqpConnector = fi.procemplus.amqp2math.AmqpTopicConnectorSync(amqpProps, topicsIn);

% Check if any messages have been received from the topics.
% This will return null/empty if there is currently nothing in the queue.
% If there are multiple messages, these are returned with First In First Out (FIFO).
message = amqpConnector.getMessage();
routingKey = message.getRoutingKey();
messageBodyBytes = message.getBody();
```


### Publishing (sending) to AMQP

The following code sends a string encoded in UTF-8.

```
myStringOut = java.lang.String('Hello 5');
myBytesOut = myStringOut.getBytes(java.nio.charset.Charset.forName('UTF-8'));
amqpConnector.sendMessage('my.topic.Out', myBytesOut);
```


### Cleanup

It is important to clean up resources after use. Call this when you end execution:

```
amqpConnector.close();
```
        