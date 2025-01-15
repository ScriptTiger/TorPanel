[![Say Thanks!](https://img.shields.io/badge/Say%20Thanks-!-1EAEDB.svg)](https://docs.google.com/forms/d/e/1FAIpQLSfBEe5B_zo69OBk19l3hzvBmz3cOV6ol1ufjh0ER1q3-xd2Rg/viewform)

[![ScriptTiger/TorPanel](https://scripttiger.github.io/images/TorPanel-Interface.png)](https://github.com/ScriptTiger/TorPanel)

# TorPanel
A small Java Tor controller widget that stays conveniently on top and out of the way with a minimal footprint while you go about your activities.

**TorPanel-8.jar**  
Supported by JDK/JRE 8+ (suitable for most users).

**TorPanel-17.jar**  
Supported by JDK/JRE 17+.

By default, TorPanel will connect to 127.0.0.1:9051, attempt to authenticate to Tor without a password, compare the connected version of Tor to the latest stable version of Tor (Tor Expert Bundle, not Tor Browser), and open a warning dialog if the versions are different. To change the default behavior, you can create a `torpanel.conf` file in the working directory and include key-value pairs for `host`, `port`, and `secret`, delimited by a `=` (i.e. `secret=password`). The key-value pair for `check` is also available, with possible values of `true` or `false` to enable or disable version checking, respectively.

# More About ScriptTiger

For more ScriptTiger scripts and goodies, check out ScriptTiger's GitHub Pages website:  
https://scripttiger.github.io/
