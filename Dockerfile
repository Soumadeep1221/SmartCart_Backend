FROM eclipse-temurin:25-jre
COPY target/smartcart.jar smartcart.jar
CMD ["java", "-jar", "smartcart.jar"]