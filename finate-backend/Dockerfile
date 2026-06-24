#build the project :

FROM maven:3.9.6-eclipse-temurin-21 AS builder

# working directory inside the container :

WORKDIR /app

# copy the pom.xml before the source code :
# so that it won't redownload the dependencies on the next build
COPY pom.xml .

#Download all dependencies while offline(-B => batch mode, no interactive prompts) :

RUN mvn dependency:go-offline -B

# copy the source code :

COPY src ./src

# build the jar with skip tests, cause if we test they will fail without the db config:

RUN mvn clean package -DskipTests

# now the stage to is to run as the build is completed till now :

FROM eclipse-temurin:21-jre-alpine AS runtime

# here alpine is lightweigth linux (linux = 70 mb, alpine = 5 mb)

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar
#now as we built jar files from the build phase, we use that builder we used in the first line
#and then copy all the jars files of the project in a single app.jar file

# install the curl for health check : 
RUN apk add --no-cache curl

EXPOSE 8080
# documents that this container listens to is 8080 (backend)

ENTRYPOINT ["java", "-jar", "app.jar"]
# this runs the container
