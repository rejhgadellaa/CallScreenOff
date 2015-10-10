# CallScreenOff
The app that keeps your screen turned off when it's in your pocket during calls via bluetooth

If you, like me, have been having issues with your phone unlocking in your pocket when you accept and hang up a call via a bluetooth headset or Android Wear device: this app tries to solve that!

The idea is as follows:
  1. You accept a call or hang up: goto 3.
  2. The screen unlocks and you are in a call: goto 3.
  3. CallScreenOff checks if you have a BT headset connected: if yes goto 4, else do nothing
  4. CallScreenOff uses the proximity sensor to determine if your device is in your pocket: if yes goto 5, else do nothing
  5. Use Device Manager to lock your screen
  6. Done!
