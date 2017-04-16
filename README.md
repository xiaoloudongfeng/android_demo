基于树莓派室内温湿度监控服务的Android Demo
==========================
## 1.简介
这是一个简单的Android Demo，依赖[树莓派室内温湿度监控服务](https://github.com/xiaoloudongfeng/raspberrypi_service)

## 2.功能
每秒访问一次服务端，获得json响应包并解析，成功后将信息实时展示<br>
有两个自定义的折线图控件，每隔一分钟绘制一次温湿度的折线图，点击折线图会显示该点具体的温湿度值<br>
支持设置服务端的ip及端口号，应用做的并不是很美观，仅是作演示用<br>

## 3.截图

* 主页

![截屏][1]

* 设置

![设置][2]

[1]: https://raw.githubusercontent.com/xiaoloudongfeng/android_demo/master/Screenshots/Screenshot1.png
[2]: https://raw.githubusercontent.com/xiaoloudongfeng/android_demo/master/Screenshots/Screenshot2.png
