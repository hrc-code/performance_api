## 后台运行，输出到 log文件
nohup java  -Dserver.port=8081  -Dspring.profiles.active=prod -jar my-project-1.0.0-SNAPSHOT.jar > nohup-performance.log 2>&1 &