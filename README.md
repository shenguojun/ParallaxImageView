# ParallaxImageView


跟随重力感应的视差3D效果ImageView，效果图如下：

![demo](material/demo.gif)

## 引入

## 使用

### 步骤一：

在XML或代码中创建`GravityImageView`，由于我们需要预留一些空间来进行左右晃动，其中`scale`参数表示图片放大的倍数。图片将按照这个比例进行放大，并且在上下左右晃动的时候展示出放大后被遮挡的部分。

```xml
<com.shengj.parallaximageview.GravityImageView
    android:id="@+id/image1"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:scale="1.25"/>
```

### 步骤二：

在使用的Activity或者Fragment中注册`GravitySensor`并设置回调，`GravitySensor`将根据页面生命周期对传感器进行暂停。

```kotlin
// 注册声明周期以及传感器回调
lifecycle.addObserver(GravitySensor(this).also {
    it.listener = this
})
```

### 步骤三：

将传感器的回调直接设置给`GravityImageView`，其中`x, y`是表示跟重力方向一致，`-x, -y`表示跟重力方向方法

```kotlin
override fun onGravityChange(x: Float, y: Float) {
    binding.image1.onGravityChange(x, y)
    binding.image3.onGravityChange(-x, -y)
}
```

就这样我们就完成了一个随着重力作用而晃动的视差图了！

## License

```
MIT License

Copyright (c) 2021 Lawrence/申国骏

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

