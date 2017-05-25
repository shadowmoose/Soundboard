# Soundboard
Basic, lightweight Java soundboard implementation.

Builds a visual Soundboard of all files listed in the subdirectory ./Sounds, which can be customized via the INI generated on launch.
Uses FFMpeg (in local directory) to convert any files located within the sounds directory to .wav format, which is required by Java for playback.

This Soundboard requires a Virtual Audio Cable interface, which you must install.
For Windows, I suggest this one: http://vbaudio.jcedeveloppement.com/Download_CABLE/VBCABLE_Driver_Pack43.zip

Simply set the Virtual Cable as your default microphone, and when prompted by the App select the Virtual Speakers created (listed as "CABLE Input" with the above driver).

This program is built to work on multiple systems, but only has confirmed support for Windows 7+. Differing audio systems on alternate systems are subject to breaking any of the audio processing.

This program also makes use of the library [JNativeHook](https://github.com/kwhat/jnativehook). View their repository for further information, credits, and licensing.
