<div align="center">
    <p>
    <h3>
      <b>
        Twoyi Platform
      </b>
    </h3>
  </p>
  <p>
    <b>
      A lightweight Android container
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

[README 中文版](README_CN.md)

## Introduction

Twoyi is a lightweight Android container. It runs a nearly complete Android system as a normal app (no root required) on Android. Additionally, it supports Android 8.1 ~ 12.

## Capability

1. Use Taichi·Yang without unlocking the bootloader. Xposed, EdXposed and LSPosed will be supported.
2. Use root on non-rooted devices.
3. Use a few Magisk modules.
4. Implement additional system components such as virtual camera by virtualizing the HAL layer.
5. Do security research such as shelling.

## Features

1. Twoyi is a rootless Android system-level container, which runs a nearly complete Android system as a normal app and is mostly isolated from the main system.
2. The internal Android version is Android 8.1 and Android 10 will be supported.
3. Booting up twoyi is very fast (within three seconds) except for the initialization process.
4. Twoyi is an open source project.
5. The internal system of twoyi will be fully customizable. Because its system is open source, you can fork the project to compile your own system. You can also customize the system components, such as the HAL layer to implement virtual cameras, virtual sensors and other special features.

## Building

Twoyi contains two parts:

1. The twoyi app, which is actually a UI rendering engine.
2. The internal ROM of twoyi.

This repository contains the twoyi app, and the twoyi ROM is currently being turned into open-source.  Therefore, at this moment, the ROM cannot be compiled from source yet.

### Build the App manually

#### Install Rust

Twoyi is partially written in Rust, so it's nessesary to [install Rust and Cargo](https://www.rust-lang.org/tools/install) first.

#### Install cargo-xdk

Please refer to [cargo-xdk](https://github.com/tiann/cargo-xdk).

You can check if it is installed by running `./gradlew cargoBuild`. If it succeeded, you will see libtwoyi.so in `app/src/main/jniLibs/arm64-v8a`.

PS. Please use ndk v22 or lower, otherwise it may fail.

#### Integrating rootfs

Currently you cannot build the ROM yourself, instead you can use the prebuilt ROM.
To do that, extract rootfs.7z from the official release apk and copy it to `app/src/main/assets`.

### Build the app with Android Studio

Build it with Android Studio normally.

### Build the ROM

WIP

## Discussion

[Telegram Group](https://t.me/twoyi)

## Contact Me

twsxtd@gmail.com
