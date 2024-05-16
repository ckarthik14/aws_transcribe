#!/bin/sh

./gradlew clean buildZip --rerun-tasks

aws s3 cp ./build/distributions/aws_transcribe-1.0-SNAPSHOT.zip s3://aws-real-time-translation/aws_transcribe-1.0-SNAPSHOT.zip

aws lambda update-function-code --function-name from_customer_full_translation_lambda --s3-bucket aws-real-time-translation --s3-key aws_transcribe-1.0-SNAPSHOT.zip