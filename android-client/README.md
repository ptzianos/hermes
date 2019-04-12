## Setup

In order to build the application you need to have both Android SDK and NDK setup in your system. Go 
[here](https://developer.android.com/ndk/guides/) for information about how to do that. Make sure you also
have Gradle and Open JDK 8 installed. If you have multiple versions of OpenJDK installed there might be a 
problem with the Dagger libraries.

### Android SDK

In order to setup, you need to have Open JDK, Kotlin, Groovy, Gradle and the Android SDK installed.
Add a `local.properties` file to the root of the Android client module with the following line:

```text
sdk.dir = /home/USERNAME/Android/Sdk
```

if you are a Linux distro user, otherwise if you use Mac do the following:

```text
sdk.dir = /Users/USERNAME/Library/Android/sdk
```

It is suggested that you add the path to your environment in a variable named `ANDROID_HOME`.

Finally, if you are a Windows user, then remove Windows, install Linux and then use the first solution.

### Android NDK

Once NDK is installed, there should be a library somewhere in your system. For example, in Linux systems
the path will be `/usr/lib/android-ndk`.

As previously you need to go to the `local.properties` file and add the following line:

```text
ndk.dir = /usr/lib/android-ndk
```

You should also add the path in a variable in your environment called `ANDROID_NDK_HOME`.
