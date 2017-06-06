
<h1 id="introduction">Introduction</h1>

<p>If you, like me, have been having issues with your phone unlocking in your pocket when you accept and hang up a call via a bluetooth headset or Android Wear device: this app tries to solve that!</p>

<p>The idea is as follows:</p>
<ol>
  <li>You accept/make a call or hang up: goto 3</li>
  <li>The screen unlocks and you are in a call: goto 3</li>
  <li>CallScreenOff checks if you have a headset connected: if yes goto 4, else do nothing</li>
  <li>CallScreenOff uses the proximity sensor to determine if your device is in your pocket: if yes goto 5, else do nothing</li>
  <li>Use Device Manager to lock your screen</li>
  <li>Done!</li>
</ol>

<h2 id="permissions">Permissions</h2>

<ul>
    <li><strong>Read phone status and identity</strong><br>
        This one should be obvious: CSO needs to know when you're in a call.<br>&nbsp;
    </li>
    <li><strong>Device Administrator: Lock your device</strong><br>
        Required to lock your screen. This permission needs to be granted by you after installation.<br>&nbsp;
    </li>
    <li><strong>Run at startup</strong><br>
        CSO runs a background service that registers a bunch of event listeners (like phone state events, screen unlocks, etc). This permission allows the service to automatically start when you (re)boot your device.<br>&nbsp;
    </li>
    <li><strong>Read/write storage</strong><br>
        Required to write a log and attach it when sending feedback.
    </li>
</ul>

<h2 id="support">Support &amp; troubleshooting</h2>

<h3>Persistent notification</h3>

<p>As you may or may not know, the Android system will sometimes kill one or more background services when the device is low on available memory. The only way to prevent this is running the service "in the foreground". To do this, the service needs to show a notification so the user is aware that the service is doing something that prevents the system from killing it. So this is why CallScreenOff shows a persistent notification whenever the service is active.</p>

<p>However, there is a "trick" of sorts the user still can perform: block notifications from the app. To do this you need to longpress the Notification (Android 5.0 Lollipop and higher) and press the small information icon that appears. In the screen that pops up you have the option to block notifications. Since CallScreenOff does not show any other notifications that are important for you to see, you can safely block it and still have the service run in the foreground.</p>

<h3>Android 6.0 Marshmallow</h3>

<p>The latest version of Android introduced two new features: Doze and Inactive Apps (<a href="http://goo.gl/cYwmhM" target="_blank">learn more</a>). The latter of these can interfere with CallScreenOff by shutting down its background service. You should be prompted to make an exception for CallScreenOff - please make sure you do.</p>

<p>In case you are worried that this service may impact your battery life: it shouldn't. The service registers a passive listener for when a headset is (dis)connected. If one is connected, it also registers a listener for phone call events. Only if both conditions are true it may become more active.</p>

<h3>Uninstalling</h3>

<p>When you grant an app Device Administrator permissions, you can't just uninstall it. You need to disable these permissions first: Head into Settings > Security > Device Administrators and disable CallScreenOff.</p>

<h3>Need more help?</h3>

<p>Send me an email: <a href="mailto:droidapps@rejh.nl?subject=CallScreenOff Support Request">droidapps@rejh.nl</a></p>

<h2 id="open-source">Find the project..</h2>

<p><a href="https://github.com/rejhgadellaa/CallScreenOff/" target="_blank">...on GitHub</a></p>

<h2 id="get-the-app">Get the app!</h2>

<p>CallScreenOff is unfortunately no longer available on Google Play. You can, however, download the APK below:</p>

<div class="download round4" onClick="window.open('https://stor4ge.rejh.nl/_stored/dev/android/callscreenoff/callscreenoff-latest.apk');">
    <img class="icon" src="img/ic_download_w_48.png" />
    <div class="text">DOWNLOAD</div>
</div>

<p>&nbsp;</p>

</div>
