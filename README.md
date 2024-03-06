# Graffiti
Graffiti keyboard input for android similar to the palm graffiti input method.  
The strokes are minimized and compressed during the input to minimize memory usage.  

The recognition engine is in the **Graffiti4Engine.java** and is following the   
implementation described in https://jackschaedler.github.io/handwriting-recognition/   

The decoding of characters is made in **Graffiti4Decode.java** based on the position  
in the canvas: left area for letters, right area for numbers, middle area for capital letters.  
The special symbols are after a dot on the screen.  

The strokes for each character are in the **gestures.txt** file.

![screenshot](https://github.com/GeorgeRadev/graffiti_android/blob/main/graffiti_screenshot.png?raw=true)