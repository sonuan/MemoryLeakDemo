# 内存泄漏之Android Studio工具分析

---千里之堤毁于蚁穴

### 什么是内存泄漏？

内存泄漏是程序在运行过程中，无法释放不再使用的内存空间的情况，简单的说，本应该回收的内存,不能被回收。一次内存泄漏的危害可以忽略，但要是不断堆积就会使程序内存吃紧，不断地申请新内存，不断GC（垃圾回收），使程序变得缓慢，卡顿等。

在Android系统中，一个App应用能申请到的内存是有限的，当超过某个数值（每个android版本不一样，没有明显的界限）时，甚至出现Force Close，既OOM（内存溢出）的情况。

### 什么是内存溢出？

内存溢出是程序申请的内存，超出系统所能分配的范围，从而发生内存溢出。

### Why?

而在我们开发过程中，时而疏忽，一不小心就出现内存泄漏的情况，内存泄漏最终会导致OOM。由于内存泄漏，在撸码过程中，工具和程序都不会报错提示，以至不能及时地发现，往往都是在发生OOM的情况，才会去找出内存泄漏的地方。然后检测内存泄漏又是一件头疼，一般需要借助第三方工具分析内存情况，才能得出大概的泄漏位置，常用的工具有MAT等。

本文将介绍如何用Android Studio自带的Android Monitor工具检测内存泄漏。

### Android Studio & Android Monitor

Android Studio是为Android开发的集成开发环境，基于IntelliJ IDEA。

Android Monitor是Android Studio里的一个工具，主要有Logcat、Memory、CPU、GPU、NetWork几大块功能。如下图所示。

![1BA81459-DE3C-472C-89EC-BFAFD85A8708](http://7xrth8.com1.z0.glb.clouddn.com/2016-03-16-1BA81459-DE3C-472C-89EC-BFAFD85A8708.png-webpwater)

其本文内存泄漏检测用到的是Android Monitor里的Memory这个功能。

![AB5F80CD-0310-41E1-B7FE-3C19F4E0D02E](http://7xrth8.com1.z0.glb.clouddn.com/2016-03-16-AB5F80CD-0310-41E1-B7FE-3C19F4E0D02E.png-webpwater)

Memory功能介绍如下:

![CEDFCB17-B6FD-4A9C-9D77-76A064098B](http://7xrth8.com1.z0.glb.clouddn.com/2016-03-16-CEDFCB17-B6FD-4A9C-9D77-76A064098BC1.jpeg-webpwater)

* 停止，即停止检测内存使用情况
* GC，发起一次垃圾回收，回收不再使用的内存。
* Dump Java Heap，在当前时间，记录内存使用情况，并生成.hprof文件，生成好后工具会自动打开.hprof文件。
* 内存跟踪，了解内存分配更详细的情况

### How?

* 运行App
* Android Monitor下选择对应设备，对应进程，监控内存
* 操作App到所需检测内存泄漏的页面，然后finish当前页面
* GC，这一步极为重要，一定要GC,最好多GC几次，如果内存回收不明显，说明这个页面极有可能发生了内存泄漏。
* Dump Java Heap，生成.hprof文件（这个文件可以导出至其他第三方如MAT工具使用），自动打开如下图界面。

![9247D12C-F653-4B7E-8ADC-E51D91FA2307](http://7xrth8.com1.z0.glb.clouddn.com/2016-03-16-9247D12C-F653-4B7E-8ADC-E51D91FA2307.jpeg-webpwater)

![CAD5347F-75A7-4F47-B2A4-308504757BB9](http://7xrth8.com1.z0.glb.clouddn.com/2016-03-16-CAD5347F-75A7-4F47-B2A4-308504757BB9.jpeg-webpwater)

### 分析

上图分为4大块，分别标注1、2、3、4区块。

* 1区块为内存中所有的类
* 2区块为内存中类的实例对象
* 3区块为内存中类的实例对象引用到的对象
* 4区块为分析任务，如图所示，主要检查内存泄漏的Activity、查找重复的字符串。

#### 具体使用

下面是以Handler的使用为例子，具体看代码。由于不是每次使用Handler都会引发内存泄漏，这里面有一定的几率，需要满足特定条件才会引起泄漏，所以特定设置延迟时间比较长，以模拟内存泄漏。

```js
public class HandlerActivity extends AppCompatActivity {

    private TextView mTVLeak;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle);
        mTVLeak = (TextView) findViewById(R.id.tv_leak);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mTVLeak.setText("内存泄漏");
            }
        }, 5000000);//延迟5000秒后执行，为了检测内存泄漏比较明显，故意设置这么大，实际开发中，应该不会出现设置如此大

    }
}
```

当Handle延迟5000s才调用Runnable对象，在这时间内，Activity执行finish()或者屏幕旋转（重新创建Activity的情况）等，由于Hanler还一直引用这个Activity，所以在GC的时候，并不能回收这个Activity，下面以这个模拟的例子，简单地应用Android Studio自带的内存检测工具分析内存泄漏的情况。

#####  第一种方法

* **第一步，按个人喜好筛选。**1区块左上方有两个筛选项，一个是App heap、Image Heap、Zygote heap，一个是Class List View、Package Tree View，可以根据个人喜好选择某种方式查看，图中所选是按包名树形显示。

* **第二步，按需要查看实例。**1区块根据包名查找到HandlerActivity选中后，2区块会列出HandlerActivity所有实例对象。

* **第三步，分析详细。**2区块选中一个HandlerActivity的实例对象，3区块会列出这个实例对象所有引用的对象以及内存分配情况。

* **第四步，结论。**分析3区块的内存使用情况，可以大概得出HandlerActivity的内存泄漏在callback，然后回代码查看哪里有callback没有结束，一直占用着关闭了的HandleActivity实例，从而分析出那块代码出现内存泄漏。

##### 第二种方法

* **第一步，直接分析出泄漏的Activity。**4区块集成分析内存泄漏的功能，直接点击开始分析即会在Analysis Results中列出泄漏的Activity。

* **第二步，分析详细。**再选中Leaked Activities中的一个实例对象，1、2、3区块都会显示相应的信息

* **第三步，结论。**最后分析跟第一种方法最后的分析一样，只需要关注3区块就好

### 其他

以上就是关于Android Studio内存泄漏检测的简单介绍，只是一个初步的分析的过程，实际问题可能还需借助第三方工具，比如MAT、LeakCanary等，才能完全定位内存泄漏的位置、原因以及解决问题。

Demo地址:[https://github.com/sonuan/MemoryLeakDemo](https://github.com/sonuan/MemoryLeakDemo)




