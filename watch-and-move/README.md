# watch-and-move

watches a directory and copies any new zip files to an S3 bucket.

intended usage in the demo is to watch the directory that the streaming-message-archiver is writing to and copy/move those files to S3.  


directory to watch is passed in on the command line.  
currently the bucket is hardcoded to "bulk-delivery".  

build:  
`mvn package`  

configure:  
aws configuration is currently at the profile level which means the aws cli tools are installed and `aws configure` has  
been run locally on the machine this is running on.  this approach prevents the credentials from being part of the source code.

run:
`java -jar target/watch-and-move-1.0-SNAPSHOT.jar <dir to watch>`  


