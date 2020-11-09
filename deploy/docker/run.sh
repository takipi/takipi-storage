#!/bin/bash

# delete existing stack
echo "Deleting cloudformation stack (if exists)..."
aws cloudformation delete-stack --stack-name overops-storage

# wait for delete
echo "Waiting for delete-stack to finish..."
aws cloudformation wait stack-delete-complete --stack-name overops-storage

# package stack
sam package --template-file sam.yaml --output-template-file output-sam.yaml --s3-bucket $S3_BUCKET --s3-prefix $S3_PREFIX

# deploy stack
sam deploy --template-file output-sam.yaml --capabilities CAPABILITY_IAM --parameter-overrides ParameterKey=S3BUCKET,ParameterValue=$S3_BUCKET ParameterKey=S3PREFIX,ParameterValue=$S3_PREFIX --stack-name overops-storage
