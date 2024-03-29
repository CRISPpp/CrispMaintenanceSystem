# 课题方向：故障维修调度系统开发

# 课题背景介绍

维护部门在接到用户故障申报后，需要根据用户报障地点及时派单给附近的维护工程师进行上门维修，以快速调度人员进行故障处理和排查。
为了有效进行派单调度，首先要及时掌握各位维护工程师当前所在位置，可以在综合调度平台的地图中看到当前维护工程师的位置分布情况。维护工程师通过 APP 及时上报其最新工作位置信息，调度中心根据当前维护工单中报障用户的位置信息，将工单分配到当前空闲且距离最近的维护工程师进行处理，如果当前没有合适的工程师可以调度则进入队列排队等待后续处理，维护工程师通过 APP 进行接单，接单后自动将其状态修改为工作中，维修成功后将其状态修改为空闲。

# 配置版本 & 组件列表

## JAVA

java17

## springboot

springboot 2.7.3

## springcloud

springcloud 2021.0.6

## nacos

nacos 2.1.1

## redis

redis 7.0.10

## mysql

mysql 8

## ORM

mybatisplus 3.5.1

## 数据库连接池

druid

## rpc

openfeign

## 搜索框架

elasticsearch 7.8.0

## 日志

log4j

## json 序列化工具

jackson 2.13.2

fastjson 2.0.7

## 分布式文件系统

minio 8.4.2

## 测试

junit 4.13.2

## 监控

mysqld_exporter 0.15.0

prometheus 2.44.0

grafana 9.5.1

# 分布式锁

redisson 3.21.0
