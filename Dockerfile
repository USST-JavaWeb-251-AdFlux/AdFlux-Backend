# 1. 基础镜像
FROM openjdk:17-jdk-slim

# 2. 设置工作目录
WORKDIR /app

# 3. 【注意】这里删除了 apt-get install ffmpeg，因为我们要用挂载的

# 4. 复制 Jar 包
# 确保这个路径是对的
COPY target/AdFlux-Backend-0.0.1-SNAPSHOT.jar app.jar

# 5. 暴露端口
EXPOSE 8820

# 6. 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]