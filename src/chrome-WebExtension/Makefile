all:
	$(info )
	$(info ********************************************************************)
	$(info ********** Have you adapted the hostname in coverup_host.txt? ******)
	$(info ********************************************************************)
	$(info )
	$(info )

	sed "s&COVERUP_SERVER_PLACEHOLDER_&$$( cat ../coverup_host.txt)&g" manifest.json.RAW > secure_ext/manifest.json
	
	# be careful with the key (*.pem). If changed, the native messaging applciation 
	# has to be changed to the new key accordingly. It is a mess. 
	google-chrome --pack-extension=$$(pwd)/secure_ext --pack-extension-key=secure_ext.pem


