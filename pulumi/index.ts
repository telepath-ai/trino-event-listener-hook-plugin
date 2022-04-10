import * as pulumi from '@pulumi/pulumi';
import * as aws from '@pulumi/aws';
import * as github from '@pulumi/github';

/*
----------------------------------------------------------------------------------------------------------------
 Config
----------------------------------------------------------------------------------------------------------------
 */
const stackName = pulumi.getStack();
const projectName = pulumi.getProject();
const awsConfig = new pulumi.Config('aws');
const awsRegion = awsConfig.require('region');
const projectConfig = new pulumi.Config(projectName);
const githubRepo = projectConfig.require('githubRepo');

const { GITHUB_TOKEN } = process.env;
if (!GITHUB_TOKEN) {
    throw new Error('Missing GITHUB_TOKEN environmental variable. Required to publish GitHub Action secrets.');
}

const tags = {
    project: projectName,
    stack: stackName
};

/*
----------------------------------------------------------------------------------------------------------------
 S3 Bucket
----------------------------------------------------------------------------------------------------------------
 */

// S3 Bucket to hold the compiled .jar artifact
const bucket = new aws.s3.Bucket('s3-bucket-plugin-jar', {
    bucketPrefix: 'trino-event-listener-hook-plugin-',
    forceDestroy: true,
    tags
});

/*
----------------------------------------------------------------------------------------------------------------
 GitHub IAM User
----------------------------------------------------------------------------------------------------------------
 */

const iamUser = new aws.iam.User('github-iam-user', {
    name: `github-trino-event-listener-hook-plugin-${stackName}`,
    path: '/',
    forceDestroy: true,
    tags
});

const accessKey = new aws.iam.AccessKey('github-iam-access-key', {
    user: iamUser.name
});

// Give the IAM user permission to write to the S3 Bucket
const policy = new aws.iam.Policy('github-iam-policy', {
    name: `github-trino-event-listener-hook-plugin-${stackName}`,
    policy: {
        Version: '2012-10-17',
        Statement: [
            {
                Effect: 'Allow',
                Action: [
                    's3:ListBucket',
                    's3:GetBucketLocation'
                ],
                Resource: [
                    bucket.arn
                ]
            },
            {
                Effect: 'Allow',
                Action: [
                    's3:ListBucket',
                    's3:GetObject',
                    's3:GetObjectAcl',
                    's3:PutObject',
                    's3:PutObjectAcl'
                ],
                Resource: [
                    pulumi.interpolate`${bucket.arn}/*`
                ]
            }
        ]
    }
});

new aws.iam.UserPolicyAttachment('github-iam-user-policy-attachment', {
    user: iamUser,
    policyArn: policy.arn
});

/*
----------------------------------------------------------------------------------------------------------------
 Add Secrets to GitHub Account
----------------------------------------------------------------------------------------------------------------
 */

new github.ActionsSecret('github-secret-aws-key', {
    repository: githubRepo,
    secretName: 'AWS_ACCESS_KEY_ID',
    plaintextValue: accessKey.id,
});

new github.ActionsSecret('github-secret-aws-secret', {
    repository: githubRepo,
    secretName: 'AWS_SECRET_ACCESS_KEY',
    plaintextValue: accessKey.secret,
});

new github.ActionsSecret('github-secret-aws-region', {
    repository: githubRepo,
    secretName: 'AWS_REGION',
    plaintextValue: awsRegion
});

new github.ActionsSecret('github-secret-s3-bucket', {
    repository: githubRepo,
    secretName: 'S3_BUCKET',
    plaintextValue: bucket.id
});

// Export references to the resources
export const bucketName = bucket.id;
