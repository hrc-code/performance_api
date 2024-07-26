#!/bin/bash

# 更新软件包列表
sudo apt update

# 安装OpenJDK 8
sudo apt install -y openjdk-8-jdk

# 验证安装
if [ "$(java -version 2>&1 | grep 'openjdk version')" ]; then
    echo "Java 8 (OpenJDK) installed successfully."
else
    echo "There was an error installing Java 8."
fi

# 设置默认的Java版本为Java 8
sudo update-alternatives --config java

# 输出当前使用的Java版本信息
java -version