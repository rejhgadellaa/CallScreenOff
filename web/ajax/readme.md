
<h1 id="introduction">Introduction</h1>

<p>If you, like me, have been having issues with your phone unlocking in your pocket when you accept and hang up a call via a bluetooth headset or Android Wear device: this app tries to solve that!</p>

<p>The idea is as follows:</p>
<ol>
  <li>You accept/make a call or hang up: goto 3</li>
  <li>The screen unlocks and you are in a call: goto 3</li>
  <li>CallScreenOff checks if you have a BT headset connected: if yes goto 4, else do nothing</li>
  <li>CallScreenOff uses the proximity sensor to determine if your device is in your pocket: if yes goto 5, else do nothing</li>
  <li>Use Device Manager to lock your screen</li>
  <li>Done!</li>
</ol>

<h2 id="screenshots">Screenshots</h2>

<p>
<div class="screenshots"><img src="http://static.rejh.nl/rgt/rgt.php?w=128&h=320&src=http://storage.rejh.nl/_stored/res/callscreenoff/48f6b0cb-4afe-4f7a-a415-d8e508c31e93.png"><div class="space"></div><img src="http://static.rejh.nl/rgt/rgt.php?w=128&h=320&src=http://storage.rejh.nl/_stored/res/callscreenoff/70d3e439-1bc5-44d3-9483-8e09310cb518.png"></div>
</p>

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
    <li><strong>Modify or delete the contents of your USB storage</strong><br>
        Not really required but Android Studio keeps adding this permission..?
    </li>
</ul>

<h2 id="permissions">Support &amp; troubleshooting</h2>

<h3>Android 6.0 Marshmallow</h3>

<p>The latest version of Android introduced two new features: Doze and Inactive Apps (<a href="http://goo.gl/cYwmhM" target="_blank">learn more</a>). The latter of these can interfere with CallScreenOff by shutting down its background service. You should be prompted to make an exception for CallScreenOff - please make sure you do.</p>

<h3>Uninstalling</h3>

<p>When you grant an app Device Administrator permissions, you can't just uninstall it. You need to disable these permissions first: Head into Settings > Security > Device Administrators and disable CallScreenOff.</p>

<h3>Need more help?</h3>

<p>Send me an email: <a href="mailto:droidapps@rejh.nl?subject=CallScreenOff Support Request">droidapps@rejh.nl</a></p>

<h2 id="open-source">Find the project..</h2>

<p><a href="https://github.com/rejhgadellaa/CallScreenOff/" target="_blank">...on GitHub</a></p>

<h2 id="get-the-app">Get the app!</h2>

<p>CallScreenOff is available on Google Play:</p>

<p><center>
<a href="https://goo.gl/vnfh9j" target="_blank">
  <img alt="Get it on Google Play" src="img/googleplay60.png" />
</a>
</center></p>


<p>&nbsp;</p>

</div>
