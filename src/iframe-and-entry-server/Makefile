all:
	$(info )
	$(info ********************************************************************)
	$(info ********** Have you adapted the hostname in coverup_host.txt? ******)
	$(info ********************************************************************)
	$(info )
	$(info )

	sed "s&COVERUP_SERVER_PLACEHOLDER_&$$( cat ../coverup_host.txt)&g" iframe.html.RAW > iframe.html
	sed "s&COVERUP_SERVER_PLACEHOLDER_&$$( cat ../coverup_host.txt)&g" example-entry-server.html.RAW > example-entry-server.html
