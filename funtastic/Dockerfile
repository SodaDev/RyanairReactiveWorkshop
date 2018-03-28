FROM openjdk:8-jdk-alpine

EXPOSE 8080

ENV CONCERT_DELAY "0s"
ENV EXHIBITION_DELAY "0s"
ENV SPORT_DELAY "0s"

ENV CONCERT_FAILURE "0.0"
ENV EXHIBITION_FAILURE "0.0"
ENV SPORT_FAILURE "0.0"

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/myservice/funtastic.jar"]
CMD ["--delay.concert=${CONCERT_DELAY}", "--delay.exhibition=${EXHIBITION_DELAY}",  "--delay.sport=${SPORT_DELAY}", "--failure.concert=${CONCERT_FAILURE}", "--failure.exhibition=${EXHIBITION_FAILURE}",  "--failure.sport=${SPORT_FAILURE}"]
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/myservice/funtastic.jar
