# LingoRecorder

LingoRecord is a better recorder for Android, you can easily process pcm data from it.



# Features

1. The multi process mechanism guarantees the integrity of PCM data.

2. Abstract `AudioProcessor` which can be injected into Recorder to support the separation of recording and processing.

3. Provide `WavFileRecorder` to support audio file as a record source.

4. Provide aidl interfaces to support the AudioProcessor run in a separate process.

# Sample

## The usage of record

Use recorder is very simple:

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

Sample effect:

![image](https://raw.githubusercontent.com/lingochamp/LingoRecorder/develop/demo/images/record.gif)

## The usage of custom processors

You can custom your own processors by implementing AudioProcessor:

```
public interface AudioProcessor {

    void start() throws Exception;

    void flow(byte[] bytes, int size) throws Exception;

    boolean needExit();

    void end() throws Exception;

    void release();

}
```

Sample effect:

![image](https://raw.githubusercontent.com/lingochamp/LingoRecorder/develop/demo/images/custom_processors.gif)

## Run processor in a separate process.

We provide aidl interfaces so that you can run a processor in a separate process. In the sample, we custom a `LocalScorerProcessor`
to run in "scorer" process. Of course, that scorer is not reality, if you want, you can our another open project [OnlineScorer-Android](https://github.com/lingochamp/OnlineScorer-Android).

## Flac encoder

We also demonstrate a flac encoder processor to show how to encode flac data by MediaCodec in the sample. But there are not all devices can support it, so use it carefully.

# Download

Gradle:

```

compile 'com.liulishuo.engzo:lingo-recorder:1.2.1-SNAPSHOT'

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
