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
      A lightweight Android container on Android
    </b>
  </p>
  <p>

[![contributions welcome](https://img.shields.io/badge/Contributions-welcome-brightgreen?logo=github)](CODE_OF_CONDUCT.md) [![Website](https://img.shields.io/badge/Website-available-brightgreen?logo=e)](https://twoyi.io)
  </p>
  <p>
    <sub>
      Built with ❤︎ by
      <a href="https://twoyi.io">
        Twoyi
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

## Introduction

twoyi is a lightweight Android container. It can run a relatively complete Android system as a normal app (no ROOT required) on Android. And it supports Android 8.1 ~ 12.

## Capability

1. Using Taiji-Yang without unlock bootloader; Xposed, EdXposed, and LSPosed can also be supported later(no ROOT required).
2. Using ROOT function on Rootless devices.
3. Magisk module will be partially supported.
4. The HAL layer of twoyi is virtualizable; e.g. virtual camera, etc.
5. twoyi can also be used for security research; e.g. shelling, etc.

## Features

1. twoyi is a rootless Android system-level container; it runs a relatively complete Android system inside and can be isolated to a certain extent from the external system.
2. The internal Android version is Android 8.1; Android 10 will be supported later.
3. The boot time of twoyi is very fast. With the exception of the first initialization, the system can be booted in about three seconds.
4. twoyi will be an open source project.
5. The internal system of twoyi will be fully customizable. Because its system is open source, you can pull a branch to compile AOSP; do custom development of system components, such as framework, HAL; implement virtual cameras, virtual sensors and other special features.

## How to Build

twoyi is divided into two parts:

1. the twoyi App, which is actually a UI rendering engine.
2. the internal ROM of twoyi.

This repository is the twoyi App, and the twoyi ROM is opensource-ing; therefore, at this stage, the rootfs of the ROM cannot be compiled from the source code yet.

In order to compile your own twoyi App, you need to extract the rootfs.7z from the official release and put it in the `app/assets` directory of this project.

## Discussion

[Telegram Group](https://t.me/twoyi)

## Contacts

twsxtd@gmail.com
