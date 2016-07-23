# voicerecord
语音录音和播放（下载方式采用单文件多线程方式下载）

###概述
主要实现了android录音和语音播放功能，其中语音播放使用了下载网络语音并缓存到本地方式的播放模式，下次播放查询数据库，找到缓存位置，若文件存在直接播放本地文件，不需重新下载，而且下载使用了单文件多线程下载，将一份文件拆分成多个文件（线程）下载

录音和播放，都使用了动画（其中录音可以根据声音大小显示不同图片来标识声音大小）

####前提配置

1. [annonations配置](https://centmeng.github.io/2016/07/23/studio%E9%85%8D%E7%BD%AEAndroidAnnotations/)

2. litepal配置（数据库框架） 




---

####关键类####

1. 语音录音关键类

	[VoiceRecorder.class](https://github.com/CentMeng/voicerecord/blob/master/app/src/main/java/com/luoteng/voicerecord/utils/VoiceRecorder.java)
	
2. 语音播放关键类

	[VoicePlayClickListener.class](https://github.com/CentMeng/voicerecord/blob/master/app/src/main/java/com/luoteng/voicerecord/utils/VoicePlayClickListener.java)
	
3. 单文件多线程下载关键类
	
	[FileDownloadThread.class](https://github.com/CentMeng/voicerecord/blob/master/app/src/main/java/com/luoteng/voicerecord/utils/FileDownloadThread.java)
	
	[DownloadTask.class](https://github.com/CentMeng/voicerecord/blob/master/app/src/main/java/com/luoteng/voicerecord/utils/DownloadTask.java)