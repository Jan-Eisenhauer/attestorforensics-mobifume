; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define name "MOBIfume"
#define version "2.3.3"
#define verName name + "-" + version
#define jarName name + ".jar"
#define iconName name + ".ico"
#define iconSrc "..\src\main\resources\images\" + iconName
#define pngSrc "..\src\main\resources\images\" + name + "_Icon.png"
#define jarSrc "..\target\" + name + ".jar"
#define jre "jre-8u221"
#define jreSrc jre + "\*"
#define java jre + "\bin\javaw.exe"
#define installer  verName + "_Installer"
#define task name + "Task.xml"

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{D317186B-B671-45D7-ACBE-353FE8AF97C0}
AppName={#name}
AppVersion={#version}
AppVerName={#verName}
DefaultDirName={commonpf}\{#name}
DisableProgramGroupPage=yes
SetupIconFile={#iconSrc}
Compression=lzma
SolidCompression=yes
OutputBaseFilename={#installer}

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[InstallDelete]
Type: filesandordirs; Name: "{app}"
Type: filesandordirs; Name: "{localappdata}\{#name}\language"

[Files]
Source: {#jarSrc}; DestDir: "{app}"; Flags: ignoreversion
Source: {#iconSrc}; DestDir: "{app}"; Flags: ignoreversion
Source: {#jreSrc}; DestDir: "{app}\{#jre}"; Flags: ignoreversion recursesubdirs
Source: {#task}; DestDir: "{app}"; Flags: ignoreversion deleteafterinstall
Source: {#pngSrc}; DestDir: "C:\ProgramData\Microsoft\User Account Pictures"; DestName: "user.png"; Flags: ignoreversion
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{commonprograms}\{#name}"; Filename: "{app}\{#java}"; Parameters: "-jar  {%|%22}{app}\{#jarName}{%|%22}"; IconFilename: {app}\{#iconName}
Name: "{commonstartup}\{#name}"; Filename: "{app}\{#java}"; Parameters: "-jar {%|%22}{app}\{#jarName}{%|%22}"; IconFilename: {app}\{#iconName}
Name: "{commonstartmenu}\{#name}"; Filename: "{app}\{#java}"; Parameters: "-jar {%|%22}{app}\{#jarName}{%|%22}"; IconFilename: {app}\{#iconName}
Name: "{commondesktop}\{#name}"; Filename: "{app}\{#java}"; Parameters: "-jar {%|%22}{app}\{#jarName}{%|%22}"; IconFilename: {app}\{#iconName}

[Registry]
Root: "HKLM"; Subkey: "SOFTWARE\Microsoft\Windows NT\CurrentVersion\AppCompatFlags\Layers"; ValueType: String; ValueName: "{app}\{#java}"; ValueData: "RUNASADMIN"; Flags: uninsdeletekeyifempty uninsdeletevalue;

[Run]
; Scheduled task for auto start
Filename: "C:\Windows\System32\schtasks.exe"; Parameters: "/delete /f /tn *"; Flags: shellexec
Filename: "C:\Windows\System32\schtasks.exe"; Parameters: "/create /xml {%|%22}{app}\{#task}{%|%22} /tn {%|%22}{#name}{%|%22}"; Flags: shellexec

; Run after install
Filename: "{app}\{#java}"; Parameters: "-jar {%|%22}{app}\{#jarName}{%|%22}"; Description: "{cm:LaunchProgram,{#StringChange(Name, '&', '&&')}}"; Flags: shellexec postinstall skipifsilent

[UninstallDelete]
Type: filesandordirs; Name: "{localappdata}\{#name}"

[UninstallRun]
Filename: "C:\Windows\System32\schtasks.exe"; Parameters: "/delete /f /tn {%|%22}{#name}{%|%22}"; Flags: shellexec
