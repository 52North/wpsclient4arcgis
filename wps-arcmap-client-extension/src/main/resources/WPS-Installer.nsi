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

  SetOutPath $INSTDIR\java\lib\ext

  File /r .\dependency\*.* 

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
  WriteUninstaller "$APPDATA\52North\WPS ArcMap Client\uninstall.exe"
  CreateShortCut "$DESKTOP\Uninstall the 52 North Extensible WPS ArcMap Client.lnk" "$APPDATA\52North\WPS ArcMap Client\uninstall.exe" ""
SectionEnd

Section "Uninstall"

  delete $INSTDIR\bin\addins\AddWPSServerAddin.esriAddIn  
  delete $INSTDIR\bin\addins\ArcMapWPSClient.esriaddin
  delete $INSTDIR\java\lib\ext\52n-wps-arcmap-client-logback-xml.jar
  delete $INSTDIR\java\lib\ext\52n-wps-client-lib-3.2.0.jar
  delete $INSTDIR\java\lib\ext\52n-wps-commons-3.2.0.jar
  delete $INSTDIR\java\lib\ext\52n-wps-config-1.2.1.jar
  delete $INSTDIR\java\lib\ext\52n-wps-io-3.2.0.jar
  delete $INSTDIR\java\lib\ext\52n-wps-io-impl-3.2.0.jar
  delete $INSTDIR\java\lib\ext\52n-xml-ows-v110-1.1.0.jar
  delete $INSTDIR\java\lib\ext\52n-xml-wps-v100-1.1.0.jar
  delete $INSTDIR\java\lib\ext\52n-xml-xlink-v110-1.0.0.jar
  delete $INSTDIR\java\lib\ext\commons-codec-1.8.jar
  delete $INSTDIR\java\lib\ext\commons-io-2.4.jar
  delete $INSTDIR\java\lib\ext\jcl-over-slf4j-1.7.5.jar
  delete $INSTDIR\java\lib\ext\jul-to-slf4j-1.7.5.jar
  delete $INSTDIR\java\lib\ext\log4j-over-slf4j-1.7.5.jar
  delete $INSTDIR\java\lib\ext\logback-classic-1.0.13.jar
  delete $INSTDIR\java\lib\ext\logback-core-1.0.13.jar
  delete $INSTDIR\java\lib\ext\slf4j-api-1.7.5.jar
  delete $INSTDIR\java\lib\ext\slf4j-simple-1.7.5.jar
  delete $INSTDIR\java\lib\ext\wps-base64-conversion-tool-${project.version}.jar
  delete $INSTDIR\java\lib\ext\wps-communication-tool-${project.version}.jar
  delete $INSTDIR\java\lib\ext\wps-zipped-shp-export-tool-${project.version}.jar
  delete $INSTDIR\java\lib\ext\wps-zipped-shp-unzip-tool-${project.version}.jar
  delete $INSTDIR\java\lib\ext\xmlbeans-2.4.0.jar
  delete "$DESKTOP\Uninstall the 52 North Extensible WPS ArcMap Client.lnk"
  RMDir /r "$APPDATA\52North\"

  WriteRegStr HKLM "SOFTWARE\ESRI\ArcGIS Java Extensions" 'Logging' '0'
  
SectionEnd 