!include LogicLib.nsh
!include x64.nsh
!include "MUI2.nsh"

Function .onInit
${If} $InstDir == ""

	${If} ${FileExists} `$PROGRAMFILES\ArcGIS\Desktop10.0`

		StrCpy $InstDir $PROGRAMFILES\ArcGIS\Desktop10.0

	${ElseIf} ${FileExists} `$PROGRAMFILES\ArcGIS\Desktop10.1`

		StrCpy $InstDir $PROGRAMFILES\ArcGIS\Desktop10.1

	${ElseIf} ${FileExists} `$PROGRAMFILES\ArcGIS\Desktop10.2`

		StrCpy $InstDir $PROGRAMFILES\ArcGIS\Desktop10.2

	${ElseIf} ${FileExists} `$PROGRAMFILES\ArcGIS\Desktop10.3`

		StrCpy $InstDir $PROGRAMFILES\ArcGIS\Desktop10.3
		
	${ElseIf} ${FileExists} `$PROGRAMFILES\ArcGIS\Desktop10.4`

		StrCpy $InstDir $PROGRAMFILES\ArcGIS\Desktop10.4
		
	${ElseIf} ${FileExists} `$PROGRAMFILES\ArcGIS\Desktop10.5`

		StrCpy $InstDir $PROGRAMFILES\ArcGIS\Desktop10.5	

	${ElseIf} ${FileExists} `$PROGRAMFILES\ArcGIS\Desktop10.6`

		StrCpy $InstDir $PROGRAMFILES\ArcGIS\Desktop10.6	

	${EndIf}
	
	
${EndIf}
FunctionEnd

Function un.onInit

	${If} ${FileExists} `$PROGRAMFILES\ArcGIS\Desktop10.0`

		StrCpy $InstDir $PROGRAMFILES\ArcGIS\Desktop10.0
		
	${ElseIf} ${FileExists} `$PROGRAMFILES\ArcGIS\Desktop10.1`

		StrCpy $InstDir $PROGRAMFILES\ArcGIS\Desktop10.1
		
	${ElseIf} ${FileExists} `$PROGRAMFILES\ArcGIS\Desktop10.2`

		StrCpy $InstDir $PROGRAMFILES\ArcGIS\Desktop10.2

	${ElseIf} ${FileExists} `$PROGRAMFILES\ArcGIS\Desktop10.3`

		StrCpy $InstDir $PROGRAMFILES\ArcGIS\Desktop10.3
		
	${ElseIf} ${FileExists} `$PROGRAMFILES\ArcGIS\Desktop10.4`

		StrCpy $InstDir $PROGRAMFILES\ArcGIS\Desktop10.4
		
	${ElseIf} ${FileExists} `$PROGRAMFILES\ArcGIS\Desktop10.5`

		StrCpy $InstDir $PROGRAMFILES\ArcGIS\Desktop10.5
		
	${ElseIf} ${FileExists} `$PROGRAMFILES\ArcGIS\Desktop10.6`

		StrCpy $InstDir $PROGRAMFILES\ArcGIS\Desktop10.6
	${EndIf}

FunctionEnd

Name "52 North Extensible WPS ArcMap Client ${project.version}"

OutFile "52n-Extensible-WPS-ArcMap-Client-${project.version}-Setup.exe"

;----------------------------------------------------------------------------------------------------------------------------

!define MUI_ABORTWARNING
!define MUI_ICON ".\Installer-Files\logo52n-48x48.ico"
!define MUI_UNICON ".\Installer-Files\logo52n-48x48.ico"
!define MUI_WELCOMEPAGE_TITLE_3LINES
!define MUI_WELCOMEFINISHPAGE_BITMAP ".\Installer-Files\logo52nclaim.bmp"
!define MUI_UNWELCOMEFINISHPAGE_BITMAP ".\Installer-Files\logo52nclaim.bmp"
!define MUI_FINISHPAGE_TITLE_3LINES

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE ".\Installer-Files\LICENSE.txt"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES

!define MUI_WELCOMEPAGE_TITLE_3LINES
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

;These indented statements modify settings for MUI_PAGE_FINISH
!define MUI_FINISHPAGE_NOAUTOCLOSE
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_LANGUAGE "English"

Section ""

  SetOutPath $INSTDIR\java\jre\lib\ext

  File .\dependency\52n-wps-arcmap-client-log4j2-xml.jar
  File .\dependency\commons-codec-1.8.jar
  File .\dependency\commons-configuration2-2.0.jar
  File .\dependency\commons-io-2.4.jar
  File .\dependency\commons-lang3-3.3.2.jar
  File .\dependency\guava-23.4-jre.jar
  File .\dependency\httpclient-4.5.6.jar
  File .\dependency\httpcore-4.4.10.jar
  File .\dependency\jackson-annotations-${jackson.version}.jar
  File .\dependency\jackson-core-${jackson.version}.jar
  File .\dependency\jackson-databind-${jackson.version}.jar
  File .\dependency\jcl-over-slf4j-${slf4j.version}.jar
  File .\dependency\slf4j-api-${slf4j.version}.jar
  File .\dependency\log4j-over-slf4j-${slf4j.version}.jar
  File .\dependency\jul-to-slf4j-${slf4j.version}.jar
  File .\dependency\joda-time-2.9.9.jar
  File .\dependency\log4j-api-2.0.2.jar
  File .\dependency\log4j-core-2.0.2.jar
  File .\dependency\log4j-slf4j-impl-2.0.2.jar
  File .\dependency\javax.inject-1.jar
  File .\dependency\shetland-${arctic-sea.version}.jar
  File .\dependency\janmayen-${arctic-sea.version}.jar
  File .\dependency\svalbard-${arctic-sea.version}.jar
  File .\dependency\iceland-${arctic-sea.version}.jar
  File .\dependency\stax-api-1.0.1.jar
  File .\dependency\wps-client-lib-${wps.client.lib.version}.jar
  File .\dependency\xml-${javaps.version}.jar
  File .\dependency\iceland-stream-${javaps.version}.jar
  File .\dependency\engine-${javaps.version}.jar

SectionEnd 

Section ""

  SetOutPath $INSTDIR\java\lib\ext

  File .\dependency\wps-base64-conversion-tool-${project.version}.jar
  File .\dependency\wps-communication-tool-${project.version}.jar
  File .\dependency\wps-zipped-shp-export-tool-${project.version}.jar
  File .\dependency\wps-zipped-shp-unzip-tool-${project.version}.jar

SectionEnd 

Section ""

  SetOutPath $INSTDIR\bin\addins

  File /r .\addins\*.*
  
SectionEnd

Section ""

  SetOutPath "$APPDATA\52North\WPS ArcMap Client"

  File /r .\wpsurls\*.*
  
SectionEnd

Section ""

  WriteRegStr HKLM "SOFTWARE\ESRI\ArcGIS Java Extensions" 'Logging' '1'
  
SectionEnd 

Section 
  SetOutPath "$APPDATA\52North\WPS ArcMap Client" 
  File ".\Installer-Files\logo52n-48x48.ico" 
  WriteUninstaller "$APPDATA\52North\WPS ArcMap Client\uninstall.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\WPSClient4ArcGIS" \
                 "DisplayName" "WPS Client for ArcGIS"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\WPSClient4ArcGIS" \
                 "DisplayIcon" "$APPDATA\52North\WPS ArcMap Client\logo52n-48x48.ico"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\WPSClient4ArcGIS" \
                 "Publisher" "52 North"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\WPSClient4ArcGIS" \
                 "DisplayVersion" "${project.version}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\WPSClient4ArcGIS" \
                 "UninstallString" "$APPDATA\52North\WPS ArcMap Client\uninstall.exe"
SectionEnd

Section "Uninstall"

  delete $INSTDIR\bin\addins\AddWPSServerAddin.esriAddIn  
  delete $INSTDIR\bin\addins\ArcMapWPSClient.esriaddin
  delete $INSTDIR\java\jre\lib\ext\52n-wps-arcmap-client-log4j2-xml.jar
  delete $INSTDIR\java\jre\lib\ext\commons-codec-1.8.jar
  delete $INSTDIR\java\jre\lib\ext\commons-configuration2-2.0.jar
  delete $INSTDIR\java\jre\lib\ext\commons-io-2.4.jar
  delete $INSTDIR\java\jre\lib\ext\commons-lang3-3.3.2.jar
  delete $INSTDIR\java\jre\lib\ext\guava-23.4-jre.jar
  delete $INSTDIR\java\jre\lib\ext\httpclient-4.5.6.jar
  delete $INSTDIR\java\jre\lib\ext\httpcore-4.4.10.jar
  delete $INSTDIR\java\jre\lib\ext\jackson-annotations-${jackson.version}.jar
  delete $INSTDIR\java\jre\lib\ext\jackson-core-${jackson.version}.jar
  delete $INSTDIR\java\jre\lib\ext\jackson-databind-${jackson.version}.jar
  delete $INSTDIR\java\jre\lib\ext\jcl-over-slf4j-${slf4j.version}.jar
  delete $INSTDIR\java\jre\lib\ext\slf4j-api-${slf4j.version}.jar
  delete $INSTDIR\java\jre\lib\ext\jul-to-slf4j-${slf4j.version}.jar
  delete $INSTDIR\java\jre\lib\ext\log4j-over-slf4j-${slf4j.version}.jar
  delete $INSTDIR\java\jre\lib\ext\joda-time-2.9.9.jar
  delete $INSTDIR\java\jre\lib\ext\log4j-api-2.0.2.jar
  delete $INSTDIR\java\jre\lib\ext\log4j-core-2.0.2.jar
  delete $INSTDIR\java\jre\lib\ext\log4j-slf4j-impl-2.0.2.jar
  delete $INSTDIR\java\jre\lib\ext\javax.inject-1.jar
  delete $INSTDIR\java\jre\lib\ext\shetland-${arctic-sea.version}.jar
  delete $INSTDIR\java\jre\lib\ext\janmayen-${arctic-sea.version}.jar
  delete $INSTDIR\java\jre\lib\ext\svalbard-${arctic-sea.version}.jar
  delete $INSTDIR\java\jre\lib\ext\iceland-${arctic-sea.version}.jar
  delete $INSTDIR\java\jre\lib\ext\stax-api-1.0.1.jar
  delete $INSTDIR\java\jre\lib\ext\wps-client-lib-${wps.client.lib.version}.jar
  delete $INSTDIR\java\jre\lib\ext\xml-${javaps.version}.jar
  delete $INSTDIR\java\jre\lib\ext\iceland-stream-${javaps.version}.jar
  delete $INSTDIR\java\jre\lib\ext\engine-${javaps.version}.jar
  delete $INSTDIR\java\lib\ext\wps-base64-conversion-tool-${project.version}.jar
  delete $INSTDIR\java\lib\ext\wps-communication-tool-${project.version}.jar
  delete $INSTDIR\java\lib\ext\wps-zipped-shp-export-tool-${project.version}.jar
  delete $INSTDIR\java\lib\ext\wps-zipped-shp-unzip-tool-${project.version}.jar
  RMDir /r "$APPDATA\52North\"

  WriteRegStr HKLM "SOFTWARE\ESRI\ArcGIS Java Extensions" 'Logging' '0'
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\WPSClient4ArcGIS"
  
SectionEnd 