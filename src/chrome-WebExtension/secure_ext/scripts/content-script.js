setTimeout(function(){
   function sendMessageToJavascript(data)
   {
      //console.log("[Content Script] Sending content->javascript");
      window.postMessage({
         direction: "contentscript->javascript",
         payload: {
            type: "submitData",
            data: data
         }
      }, "*");
   };
   // Content script <- extension
	 isActive = false;
   chrome.runtime.onMessage.addListener(function(request, sender, sendResponse) {
      if(request.type == "submit_data")
				//console.log("drop");
         sendMessageToJavascript(request.data);
	
	 		if(!isActive){
					 // add intensive listener method
					 window.addEventListener("message", function(event) {
						 if (event.source != window)
							return;
							if (event.data.type == "jstocs"){
								chrome.runtime.sendMessage({data: event.data.data, type: "data"});
						}
					 }, false);

					 isActive = true;
			 }
			
   });

   // Content script -> extension
   chrome.runtime.sendMessage({type: "submit_key", data: null});
	
}, 500);
