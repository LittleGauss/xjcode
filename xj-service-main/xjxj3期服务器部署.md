xjxj3期服务器部署



安装docker

`docker run -p 3307:3306 --name mysql3 -e MYSQL_ROOT_PASSWORD=123456 -d mysql:5.7`

进入容器mysql

`docker exec -it 258fac319f07 /bin/bash`



查看正在运行的容器

`docker ps `  

查看所有容器

`docker ps -a`  

停止容器

`docker stop [容器id]`

启动容器

docker start [容器id]



maven跳过测试打包

`mvn clean package -DskipTests`



给jar包授权root

`chmod 777 xjxj_test.jar`



mysql修改密码

`ALTER USER 'toor'@'%' IDENTIFIED BY '123456';`

 `flush privileges;`



后台启动jar

```
cd /home/files
nohup java -jar xjxj_test.jar > xjxj_test.log 2>&1 &
```

```
查找进程 ID
ps aux | grep xjxj_test.jar
关闭进程
kill -9 [进程id]
```



