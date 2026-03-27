#######################################################################################################################
## Script para detener, eliminar, compilar una imagen y ejecutar un contenedor
##
## Elaborado por Ing. Ricardo Laredo
#######################################################################################################################

# Para preparar la imagen docker necesitamos que la aplicació complicada esté en el mismo directorio en el que estamos parados, 
# para lograr esto crearemos una carpeta temporal y copiaremos en esta carpeta el jar.
#PROJECT_PATH="../pom.xml"
JAR_FILE="modulo_01-0.0.1-SNAPSHOT.jar"
#JAR_FILE_PATH="../pay-api/target/$JAR_FILE"
IMAGE_NAME="modulo-api:1.0.0"
REPOSITORY_IMAGE="rllayus"

#mvn clean package -P$emviroment -f $PROJECT_PATH
#mvn clean package  -f $PROJECT_PATH -Dmaven.test.skip=true

#cp -f $JAR_FILE_PATH .

Echo '<<RL. --> Compilando imagen docker>>'
docker build --build-arg="JAR_FILE=artemisia-0.0.1-SNAPSHOT.jar" --rm  -t  locky003/artemisia-api-imagen .

docker build --build-arg="JAR_FILE=artemisia-0.0.1-SNAPSHOT.jar" --rm  -t  locky003/artemisia-api-imagen --push .
#docker build --build-arg="JAR_FILE=$JAR_FILE" --rm  --platform linux/amd64 -t $REPOSITORY_IMAGE/$IMAGE_NAME --push .

#Compilación para localhost
#docker build --build-arg="JAR_FILE=$JAR_FILE" --rm  --platform linux/amd64 -t $REPOSITORY_IMAGE/$IMAGE_NAME .

Echo '<<RL. --> Eliminando imagen>>'
#docker rmi $REPOSITORY_IMAGE/$IMAGE_NAME
Echo '<<RL. --> Eliminando BACKEND>>'
#rm -f $JAR_FILE

docker run --env-file ./local.env -p 8080:8080  --name artemisia artemisia-api-imagen

docker run --env-file ./local.env -p 8081:8081  --name $IMAGE_NAME $IMAGE_NAME
docker run --env-file ./local.env -p 8081:8081 -d --name $IMAGE_NAME $IMAGE_NAME
#docker exec -it bash

