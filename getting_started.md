# Setup instructions
- Install Postgres
- Open database labeled 'postgres' (default db postgres should always be available)
- You should be redirected to a psql console, issue the following command
  - do not forget the semicolon...
        
          create database messageboard;
- you may exit the current console, navigate back to Postgres Desktop app and open created database 'messageboard'
- once back in the psql console, copy all commands from $PROJECT_DIR$/queries/create_messageboard_tables.sql
- paste into psql console, database setup is complete, you may close the Postgres console and app
  (make sure it is still running in the background)
- using your IDE, load the pom.xml in current project into maven, 
  this should begin fetching all project dependencies. 
  After this step is complete you may proceed with setup
- Install AWS Toolkit to your IDE and login using granted key
- if you modified the default database settings, edit local configuration in $PROJECT_DIR$/src/main/resources/application.yml
- open $PROJECT_DIR$/src/main/java/org/morriswa/messageboard/MessageboardServiceRunner.java in your IDE, 
  ensure in run configurations that the Java Environment Variable APPCONFIG_ENV_ID=local is set.
- Good luck!