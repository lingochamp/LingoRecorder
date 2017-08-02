# LingoRecorder

LingoRecord is a better recorder for Android, you can easily process pcm data from it.



# Feature

1. 在另一线程处理 pcm 数据，以避免来不及读取 AudioRecorder 导致数据丢失。

2. 抽象出 AudioProcessor 来注入 Recorder 中以支持录音和处理的分离。

3. 提供 WavFileRecorder 以支持以文件来替代录音器生成录音数据。

4. 提供 aidl 接口更简单的将 AudioProcessor 在另一个进程的  Service 中运行。



# Download

Gradle:

```

compile 'com.liulishuo.engzo:lingo-recorder:1.0'

```

# Pull Request
Please send your pull request to develop branch.

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
