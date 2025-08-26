# 1. stage: Build aşaması: kaynak kodu, Maven wrapper ile derleyip fat-jar üretiyoruz
FROM maven:3.9.2-eclipse-temurin-17 AS builder
WORKDIR /app

# Önce pom.xml'i kopyala ve bağımlılıkları indir (dependency caching için)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Kodları kopyala ve jar'ı paketle
COPY src ./src
RUN mvn clean package -DskipTests \
    -Dproject.build.sourceEncoding=UTF-8 \
    -Dfile.encoding=UTF-8 \
    -B

# 2. stage: Runtime aşaması: sadece JRE içeren küçük bir imaj
FROM eclipse-temurin:17-jre
WORKDIR /app

# Non-root user oluştur (security best practice)
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Builder'dan çıkan jar'ı al
COPY --from=builder /app/target/shop-0.0.1-SNAPSHOT.jar ./app.jar

# Dosya sahipliğini appuser'a ver
RUN chown -R appuser:appuser /app

# Uygulama 8080 portunu kullanıyor
EXPOSE 8080

# Non-root user olarak çalıştır
USER appuser

# Logback file appender için dizini oluştur (container içinde)
RUN mkdir -p /app/logs

# Health check ekle
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Başlatma komutu
ENTRYPOINT ["java","-jar","app.jar"]

