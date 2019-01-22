# CoverUp Source Code 

### Abstract:
Downloading or uploading controversial information can put users at risk, making them hesitant to access or share such information. While anonymous communication networks (ACNs) are designed to hide communication meta-data, already connecting to an ACN can raise suspicion. In order to enable plausible deniability while providing or accessing controversial information, we design CoverUp: a system that enables users to asynchronously upload and download data. The key idea is to involve visitors from a collaborating website. This website serves a JavaScript snippet, which, after user's consent produces cover traffic for the controversial site / content. This cover traffic is indistinguishable from the traffic of participants interested in the controversial content; hence, they can deny that they actually up- or downloaded any data.

CoverUp provides a feed-receiver that achieves a downlink rate of 10 to 50 Kbit/s. The indistinguishability guarantee of the feed-receiver holds against strong global network-level attackers who control everything except for the user's machine. We extend CoverUp to a full upload and download system with a rate of 10 up to 50 Kbit/s. In this case, we additionally need the integrity of the JavaScript snippet, for which we introduce a trusted party. The analysis of our prototype shows a very small timing leakage, even after half a year of continual observation. Finally, as passive participation raises ethical and legal concerns for the collaborating websites and the visitors of the collaborating website, we discuss these concerns and describe how they can be addressed.

This is the source-code to our publication [1]. The technical report can be found at [https://eprint.iacr.org/2017/191](https://eprint.iacr.org/2017/191).



[1] Sommer, D., Dhar, A., Malisa, L., Mohammadi, E., Ronzani, D. and Capkun, S., 2019. "Deniable Upload and Download via Passive Participation". In 16th USENIX Symposium on Networked Systems Design and Implementation (NSDI 19).

## Example Configuration

A working configuration for the Transfer-Server domain "http://localhost:8080" can be found in the directory "bin". This example does not use TLS.


### Running it

This description is for Linux. For Windows, it works analogously. 

In bash, go to the directory 'bin/localhost_no_TLS/Transfer-Server/coverup_files' and run '../apache-tomcat-8.5.37/bin/catalina.sh run'. It is important to be in the 'coverup-files' as the Transfer-Server relies on the working directory to find certain files. Make sure that you use the oracle JRE java version. It will not run with OpenJDK.  

Next, open the file 'bin/localhost_no_TLS/iframe-and-entry-server/example-entry-server.html'. This is the part that passive participants use. 

To become a Feed-User, start the Coverup-Tool located in 'bin/localhost_no_TLS/CoverUp-Tool/'. The tool has now started to collect droplets.

To additionally become a Transfer-User, install the WebExtension found in 'bin/localhost_no_TLS/CoverUp-Tool' in Google-Chrome and start the chatting in CoverUp-Tool.

For additional information, see [doc/index.html](doc/index.html).

For a live demo, visit [https://coverup.ethz.ch](https://coverup.ethz.ch).


### How the setup works internally

When the Transfer-Server is started the first time, it reads the directory 'BROADCAST_SRC' and 'INTR_DOCUMENT' and creates the droplets from these directories. 

Then, the example-entry-server loads an iframe which consecutively requests droplets from the Transfer-Server. These are read out by CoverUp-Tool from the Browsers 'localStorage'.

The locahost_no_TLS example already contains an installed instance of the Transfer-Server. The war-file could be found under 'bin/localhost_no_TLS/Transfer-Server/coverup_files/UndergroundServer.war'

The iframe generated in 'src/iframe-and-entry-server' needs to be copied to 'bin/Transfer-Server/coverup-files'.

For further information, we refer to our publication [1]


### Using TLS

The current example setup localhost_no_TLS does not support TLS. To enable TLS, rebuilt the setup with 'https:/some.host:some_port/' in 'src/coverup_host.txt', and set up the tomcat server to accept these connections.


## Building Instructions

There are four parts: 

The WebExtension that runs in Google Chrome
The server software that distributes the content (Transfer-Server)
The clientsoftware that collects the droplets and assembles them (CoverUp-Tool)
The iframe that has to be loaded by the initial website and the entry-server that distributes it


### WebExtension
Located in 'src/chrome-WebExtension'

change the file src/coverup-host.txt to the url your Transfer-Server runs on. 

go to 'src/chrome-Webextension' and run make.

For productive environment you might want to change the key "secure_ext.pem" to a different one. Be aware that that changes the fingerprint of the extension and that the native messaging manifest has to be adapted accordingly. 


### iframe 
Located in 'src/iframe-and-entry-server'

change the file 'src/coverup-host.txt' to the url your Transfer-Server runs on. 

go to 'src/iframe-and-entry-server' and run make. Link the resulting "iframe.html" in your entry website in an iframe tag from a different domain to ensure proper sandboxing (same-origin-policy).


### CoverUp-Tool
Located in 'src/CoverUp-Tool'

This is an Eclipse Java project. Add it to eclipse and build it.


### Transfer-Server
Located in 'src/Transfer-Server'

This is an Eclipse Java project. Add it to eclipse, point it to a recent version of Apache Tomcat (we have used v8.5), and build it.

This Server needs Oracle JRE. Currently, it does not run on OpenJDK.