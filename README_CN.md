<div align="center">
    <p>
    <h3>
      <b>
        两仪
      </b>
    </h3>
  </p>
  <p>
    <b>
      一个轻量级的 Android 容器
    </b>
  </p>
  <p>

[![contributions welcome](https://img.shields.io/badge/Contributions-welcome-brightgreen?logo=github)](CODE_OF_CONDUCT.md) [![Website](https://img.shields.io/badge/Website-available-brightgreen?logo=e)](https://twoyi.io)
  </p>
  <p>
    <sub>
      Made with ❤︎ by
      <a href="https://github.com/tiann">
        weishu
      </a>
    </sub>
  </p>
  <br />
  <p>
    <a href="https://twoyi.io">
      <img
        src="https://github.com/twoyi/twoyi/blob/main/assets/twoyi_screen.jpg?raw=true"
        alt="Screenshot"
        width="25%"
      />
    </a>
  </p>
</div>

## 简介

两仪就是一个轻量级的 Android 容器。它可以在 Android 系统上以一个普通 App 的身份（免ROOT）来运行一个相对完整的 Android 系统。并且它支持 Android 8.1 ~ 12。

## 能做什么

1. 免 ROOT 使用太极·阳；后续也可以支持 Xposed、EdXposed 和 LSPosed 等。
2. 免 ROOT 设备上使用 ROOT 功能。
3. 部分支持 Magisk 模块。
4. 两仪的 HAL 层是可以虚拟化的；如虚拟相机等。
5. 两仪还能用作安全研究；如脱壳等。

## 特性

1. 两仪是一个免 ROOT 的 Android 系统级容器；它内部运行了一个相对完整的 Android 系统，与外部的系统可以形成一定程度的隔离。
2. 两仪内部 Android 系统版本为 Android 8.1；后续会支持 Android 10.
3. 两仪的启动速度非常快。除第一次初始化系统较慢之外，后续两仪基本上可以在三秒钟左右启动。
4. 两仪将会是一个开源项目。
5. 两仪内部的系统将是完全可定制化的。因为其系统开源，你完全可以自己拉个分支编译 AOSP；对系统组件，如 framework，HAL 做定制化开发；实现虚拟相机，虚拟传感器等特殊功能。

## 如何编译

两仪由两部分组成：

1. 两仪 App，它实际上是一个 UI 渲染引擎。
2. 两仪内部运行的 ROM。

这个仓库是两仪 App 的源码，目前两仪 ROM 正在开源中，目前无法从源码编译出 ROM。

## 编译 两仪 ROM

WIP

## 编译 两仪 App

### 安装 Rust

两仪有部分组件是用 Rust 写的，因此需要先 [安装 Rust 和 Cargo](https://www.rust-lang.org/tools/install)

### 安装 cargo-xdk

请参阅: [cargo-xdk](https://github.com/tiann/cargo-xdk)

1. 如果你安装的是cargo-xdk
```sh
# not cargo-ndk
cargo install cargo-xdk
```
那么你需要把修改下app/rs/build_rs.sh文件
```
# 修改
cargo ndk -t arm64-v8a -o ../src/main/jniLibs build $1
# 为
cargo xdk -t arm64-v8a -o ../src/main/jniLibs build $1
```

可以通过如下命令检查是否成功: `./gradlew cargoBuild`，成功的话可以在 `app/src/main/jniLibs/arm64-v8a` 看到 libtwoyi.so。

### 集成 rootfs

两仪的 ROM 正在开源中，目前还无法从源码编译；不过可以使用预编译的 ROM。

可以从官方发布的 APK 包里面提取 `rootfs.7z` 然后放置到本项目的 `app/src/main/assets` 目录下。

### 使用 Android Studio 编译

直接使用 Android Studio 编译即可。

## 讨论

[Telegram 群组](https://t.me/twoyi)

## 联系我

twsxtd@gmail.com
