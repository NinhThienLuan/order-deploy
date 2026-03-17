# bin bash. Run build backend
cd backend
mvn clean package -DskipTests
mvn spring-boot:run