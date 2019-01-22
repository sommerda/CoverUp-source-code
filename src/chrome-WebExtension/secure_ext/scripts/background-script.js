'use strict';


var SECRET_KEY = "shahsheiphoon8voobahdeeWaLohgh";

// Is there a javascript running in one of the tabs that was code-verified?
var isSecure = false;
var secureTabId = 0;

function setSecure(flag, tabId=0)
{
   if(flag)
   {
      isSecure = true;

      // Set extension icon to green
      //browser.browserAction.setIcon({path : "images/secure-128.png"});
   }
   else
   {
      isSecure = false;

      // Set icon to red
      //browser.browserAction.setIcon({path : "images/insecure-128.png"});
   }

   secureTabId = tabId;
}


chrome.webRequest.onBeforeRequest.addListener(
   function(details)
   {
      // If we already have a javascript running inside one of the tabs, do nothing.
      if(secureTabId)
         return;

      return {redirectUrl : "http://192.168.3.1:3000/UndergroundServer/MainServer?flag=testframe"}
   }
   ,
   {
      urls: ["https://tildem.inf.ethz.ch:8443/UndergroundServer/MainServer?flag=testframe"],
      types: ["sub_frame"]
   },
   ["blocking"]
);

function setIcon(iconPath)
{
   chrome.browserAction.setIcon({path: iconPath, tabId: secureTabId});
}

// On clicking the icon, open a new tab and point it to our site
chrome.browserAction.onClicked.addListener(function(tabId) {
   chrome.tabs.create({url: chrome.extension.getURL("html/example.html")});
});

// Track if the tab currently running our script was refreshed/url changed
chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab) {
   // If the tab was refreshed, or if the tab's address was changed
   if("status" in changeInfo && changeInfo["status"] == "loading" && tabId == secureTabId)
   {
      setSecure(false)
   }
});

// Track if the tab currently running our script was closed
chrome.tabs.onRemoved.addListener(function(tabId)
{
   // The tab running the script was closed
   if(tabId == secureTabId)
   {
      //console.log("Removing");
      setSecure(false)
   }
});

// This extension <- content script
chrome.runtime.onMessage.addListener(function(request, sender, sendResponse) {
   if(request.type == "submit_key")
   {
      //console.log(request.data);

      // Is the javascript code-verified?
      //if(request.data == SECRET_KEY && !secureTabId) I removed the key check, its URL based now 
      if (!secureTabId)
      {
         //console.log("Content Script Request")
         //console.log("Code verified!");

         // In case our javascript isn't already running in another tab, designate this tab as the one
         setSecure(true, sender.tab.id)
      }
   }
   if(request.type == "data")
	 {
		  port.postMessage(request.data);
	 }
}); 

// This extension -> content script
function sendDataToContentScript(data)
{
   if(isSecure && secureTabId)
   {
      chrome.tabs.sendMessage(secureTabId, {type: "submit_data", data: data});
   }
}

// This extension <- other extension
/*
chrome.runtime.onMessageExternal.addListener(function(request, sender, sendResponse) {
   if(sender.id != "dinadondlhndojkchjoohmannmacgppl")
      return;

   if(isSecure)
   {
      console.log(request);
      sendDataToContentScript(request.data);
   
      //setSecure(false);  
   }
});*/

// One extension -> other extension
/*
function sendMessageToOtherExtension()
{
   // The ID of the extension we want to talk to.
   var extensionId = "dinadondlhndojkchjoohmannmacgppl";

   // Make a simple request:
   chrome.runtime.sendMessage(extensionId, {});
}*/

//setInterval(sendMessageToOtherExtension, 1000);


var port = chrome.runtime.connectNative("native_comm");


port.onMessage.addListener((response) => {
   //console.log("Received: " + JSON.stringify(response));
   sendDataToContentScript(response.data);
   
});





