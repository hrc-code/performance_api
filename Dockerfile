# 指定基础镜像
FROM m.daocloud.io/docker.io/openjdk:17-jdk-alpine as Builder

#标签
LABEL authors="hrc"

# 设置代码存储目录--容器中的位置
WORKDIR /app

# 将本机文件复制到容器工作目录中
COPY  jar/my-project-1.0.0-SNAPSHOT.jar  ./jar/

COPY  ./src/main/resources/application.yml   ./config/

#声明容器运行时监听的端口
EXPOSE 9090

### 数据卷
VOLUME /app/jar

VOLUME /app/config



# 容器启动时执行的命令
CMD ["java", "-jar","/app/jar/my-project-1.0.0-SNAPSHOT.jar","--server.port=9090"]
#CMD 会被覆盖