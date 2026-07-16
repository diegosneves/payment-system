FROM eclipse-temurin:25-jdk-ubi10-minimal

ENV LANGUAGE='en_US:en'

# Criar usuário se não existir e Criar diretório de deploy
USER root
RUN id -u 185 &>/dev/null || useradd -u 185 -r -g 0 -d /home/jboss -s /sbin/nologin -c "JBoss user" jboss \
    && mkdir -p /deployments && chown 185 /deployments

# Copiar o fat JAR gerado pelo Maven
COPY --chown=185 target/*.jar /deployments/app.jar

EXPOSE 8080
USER 185

# Opções da JVM para Spring Boot
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"

# Executar o fat JAR do Spring Boot
ENTRYPOINT ["java"]
CMD ["-jar", "/deployments/app.jar"]