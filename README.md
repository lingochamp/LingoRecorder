# LingoRecorder

LingoRecord is a better recorder for Android, you can easily process pcm data from it.



# Features

1. 多线程机制保证处理能力和 PCM 数据的完整性。

2. 抽象出 AudioProcessor 来注入 Recorder 中以支持录音和处理的分离。

3. 提供 WavFileRecorder 以支持以文件来替代录音器生成录音数据。

4. 提供 aidl 接口方便在另一个进程中处理录音数据。

# Sample

## LingoRecorder 的使用

只需简单的三步操作即可:

```
lingoRecorder = new LingoRecorder();
lingoRecorder.setOnRecordStopListener(new LingoRecorder.OnRecordStopListener() {
    @Override
    public void onRecordStop(Throwable throwable,
        Result result) {
        //any execeptions occur during recording will be received at here.
        //you can get duration and output file from Result.
    }
});
lingoRecorder.setOnProcessStopListener(new LingoRecorder.OnProcessStopListener() {
    @Override
    public void onProcessStop(Throwable throwable, Map<String, AudioProcessor> map) {
        //any execeptions occur during processing will be received at here.
        //you can get any processors you inject into recorder.
        //the callback will be invoked after "onRecordStop".
    }
});
```

效果图:

![image](https://raw.github.com/lingochamp/LingoRecorder/fix/1.2.0/demo/images/record.gif)

## 自定义 AudioProcessor 的使用

可以实现 AudioProcessor 来自定义自己的处理器：

```
public interface AudioProcessor {

    void start() throws Exception;

    void flow(byte[] bytes, int size) throws Exception;

    boolean needExit();

    void end() throws Exception;

    void release();

}
```

效果图:

![image](https://raw.github.com/lingochamp/LingoRecorder/fix/1.2.0/demo/images/custom_processors.gif)

## 将 AudioProcessor 运行在一个独立的进程中

LingoRecorder 提供了 aidl 接口以支持在一个独立的进程中运行 AudioProcessor。示例中自定义了一个 `LocalScorerProcessor` 运行在 "score" 进程中。

## Flac encoder

示例中也演示了一个使用 `MediaCodec` 进行 Flac 编码的 AudioProcessor。此示例是为了向有硬编码需求的用户提供一个样例。

# 在项目中引用

Gradle:

```

compile 'com.github.lingochamp:LingoRecorder:1.2.1-SNAPSHOT'

```

# Pull Request  
欢迎各位基于 develop 分支进行 pull request。

License
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
