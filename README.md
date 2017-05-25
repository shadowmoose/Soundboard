# Soundboard
Basic Java soundboard implementation.

Builds a visual Soundboard of all files listed in the subdirectory ./Sounds, which can be customized via the INI generated on launch.
Uses FFMpeg (in local directory) to convert any files located within the sounds directory to .wav format, which is required by Java for playback.

This Soundboard requires a Virtual Audio Cable interface, which you must install.
For Windows, I suggest this one: http://vbaudio.jcedeveloppement.com/Download_CABLE/VBCABLE_Driver_Pack43.zip

Simply set the Virtual Cable as your default microphone, and when prompted by the App select the Virtual Speakers created. (CABLE Input) with the above driver.

This program is built to work on multiple systems, but only support for Windows 7+ is tested. Differing audio systems on alternate systems are subject to breaking any of the audio processing.
