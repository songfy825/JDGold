# 功能介绍

## 目前实现：

1. 动态读取API获取金价，目前只有民生(停盘前忘了还没去拿另外两个银行的API) 
2. 一键录入历史数据(代码只采集文本数据，不会对你的账户有任何影响)
3. 录入后，可以根据自己的操作按单价和克数自动售出最低单价，也就是说的单笔交易

## 使用方法：
### 注意！！！ 
下载了2/3/4 直接跳到步骤6 ，其他方式下载请看下面
### 步骤：


1. 1. 下载[🔗仅包含程序](https://github.com/user-attachments/files/19936455/JDGoldAPP.zip)
   2. [🔗程序jdk21,123云盘链接1](https://www.123912.com/s/bGrRTd-vEpQH?) 提取码:TjOs
   3. [🔗程序jdk21,123云盘链接1](https://www.123865.com/s/bGrRTd-vEpQH?)提取码:TjOs
   4. [🔗程序jdk21,百度网盘](https://pan.baidu.com/s/16Zm1rTG-W9Q1s4MBfrjc8Q?pwd=ye4a)
      
2. 下载[jdk21](https://download.oracle.com/java/21/latest/jdk-21_windows-x64_bin.exe)(目前只测试了这个版本可行，我打包是在jdk17打包的，但是运行不了，所以只能用jdk21)\
2.1 安装jdk21,配置教程可看[这里](https://blog.csdn.net/qq_65771647/article/details/147144541)
3. windows win+R 输入cmd，回车。mac ⌘+空格 输入terminal，回车。
4. 启动命令：java -jar **JDGoldAPP.jar**
5. 一键启动已经写在release里了，Windows的写了，MAC电脑不在身边，暂未写。
6. 使用电脑登录京东金融黄金页面 [🔗链接](https://m.jdjygold.com/finance-gold/newgold/home/?orderSource=hzh_msyhhangqing&utm_source=Android_url_1745221370595&utm_medium=jrappshare&utm_term=wxfriends) \
      2.1 关于第二步的链接,如果对我提供的链接不放心，你自行打开京东金融黄金页面，右上角三个点分享给微信助手，复制链接在电脑中打开。 \
      2.2 登录后，点击下面的交易明细，就是图标下面那个进入查看你买入卖出的那个界面。\
      2.3 将页面拉到底，一直滚动，直到页面加载完毕。
      2.4 保存网页，保存为html文件，文件名随意，保存到任意位置。win 是ctrl+S / mac 是⌘+S
7. 软件下方有一个上传交易记录，把你刚刚保存的html上传，运行软件，软件会自动读取html文件，并自动录入数据。\
7.1 **注意** 对应不同的银行上传不同的html文件。上传错了，自己重新上传一遍就行了。
8. 数据库文件存放在/users/home/GoldData 下面，删除这个软件的时候记得去删了。\
比如说 Windows 就在C:\Users\Administrator\GoldData 下面 ，MAC同理
## TODO：

远期目标,写前端html界面，加入更多的图表(应该不会去做这件事了)

## 更多：
html2csv.py 文件是将html 转为csv 的py脚本,用csv打开
