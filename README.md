# OtelLambda

1. Download the code.
2. CD into the directory and run "mvn pacakge" to compile it.
3. It will create a new directory called "target".
4. Upload "java-events-1.0-SNAPSHOT.jar" inside the target directory into your Lambda function.
5. Follow these instructions for additional setup. Use '/opt/otel-sqs-handler'. https://aws-otel.github.io/docs/getting-started/lambda/lambda-java
6. Create a SQS queue. Add a trigger on the Lambda function from your SQS queue.
7. Give Lambda permissions to read from the queue. https://docs.aws.amazon.com/lambda/latest/dg/with-sqs.html
8. Go to SQS and send a message to the queue from the console. https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-using-send-messages.html
9. You should see the trace in the X-Ray console.
