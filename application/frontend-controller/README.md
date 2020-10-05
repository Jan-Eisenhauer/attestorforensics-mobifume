# MOBIfume

The all new mobile cyanoacrylate fuming system for the development of latent fingerprints at the scene, in fuming tents or fuming rooms.
This application is responsible for the MOBIfume controller of the system on a tablet.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development purposes.

### Prerequisites

- [Java SE Development Kit 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (oracle JDK)
- [Eclipse](https://www.eclipse.org/downloads/packages/release/2019-06/r/eclipse-ide-java-developers) (IDE)
- [Lombok](https://projectlombok.org/download) (Java Library with useful functions, must be installed to run preprocessor)
- [Inno Setup 6.0.2](http://www.jrsoftware.org/isdl.php) (Creates an .exe installer for windows)

### Installing and Building (Linux)

1. Install JDK 8
    - `sudo apt install openjdk-8-jdk`
2. Install Eclipse for Java Developers:
    - Download [Eclipse](https://www.eclipse.org/downloads/packages/release/2019-06/r/eclipse-ide-java-developers) for Linux
    - Unpack downloaded file with `tar xfz eclipse-*.tar.gz`
    - Start eclipse with `eclipse/eclipse`
3. Install Lombok
    - Download lombok.jar
    - Execute lombok.jar with `java -jar lombok.jar`
    - If program doesn't find location of IDE: Specify location -> browse to eclipse root directory
    - Install / Update
    - Restart eclipse
4. Install JavaFX library (OpenJDK-8 for Linux doesn't include JavaFX)
    - `sudo apt install openjfx=8u161-b12-1ubuntu2 libopenjfx-java=8u161-b12-1ubuntu2 libopenjfx-jni=8u161-b12-1ubuntu2`
5. Import the project with maven in eclipse:
    - Click on File -> Import
    - Select Maven -> Existing Maven Projects -> Next
    - Browse to root directory of the project -> Finish
6. Install Inno Setup
    - `sudo apt install wine-stable`
    - `wget http://files.jrsoftware.org/is/6/innosetup-6.0.2.exe`
    - `wine innosetup-6.0.2.exe`
    - Create file named `iscc` in `/bin/` with content:
      ```
      #!/bin/sh  
      unset DISPLAY  
      scriptname=$1  
      [ -f "$scriptname" ] && scriptname=$(winepath -w "$scriptname")  
      wine "C:\Program Files (x86)\Inno Setup 6\ISCC.exe" "$scriptname"
      ```
    - Grant permission to users with: `sudo chmod 655 iscc`
    
## Building    
- Build the application in eclipse
    - Run as -> Maven build
    - The jar file can be found in `target/`
    - The installer .exe file can be found in `setup/Output`

## Running (Windows)

1. Execute the installer on the windows tablet.
2. Go through the installation process. The target directory must be `C:\Program Files\MOBIfume\` otherwise the autostart won't work.

## Dependencies

- [JavaFX](https://openjfx.io/) - Library for client application (UI)
- [Lombok](https://projectlombok.org/download) - Library with useful functions (eg. getter/setter annotation)
- [GSON](https://mvnrepository.com/artifact/com.google.code.gson/gson) - Library to work with JSON
- [log4j](https://mvnrepository.com/artifact/log4j/log4j) - Library for Logging in Java
- [Eclipse Paho](https://www.eclipse.org/paho/) - MQTT Client

## Build With

- [Maven](https://maven.apache.org/) - Build Management Tool / Dependency Management
- [Inno Setup](http://www.jrsoftware.org/isinfo.php) - Compiler to build exe file

## Versioning

For versioning [SemVer](https://semver.org/) is used. Version is set in [pom.xml](pom.xml).

## File Structure and Directory Layout
```
    └───mobifume
        ├───pom.xml
        ├───setup
        │   ├───MOBIfumeTask.xml
        │   ├───Setup.iss
        │   └───jre-8u221
        └───src
            ├───main
            │   ├───java
            │   │   └───com.attestorforensics.mobifume
            │   │       ├───Mobifume.java
            │   │       ├───controller
            │   │       ├───model
            │   │       ├───util
            │   │       └───view
            │   │           └───MobiApplication.java
            │   └───resources
            │       ├───project.properties
            │       ├───settings.properties
            │       ├───font
            │       ├───images
            │       ├───localization
            │       ├───sounds
            │       └───view
            └───test
                ├───java
                └───resources
```

| File / Directory | Description |
| --- | --- |
| `pom.xml` | maven file defines dependencies and build instructions |
| `setup/MOBIfumeTask.xml` | Task for windows task scheduler for autostart |
| `setup/Setup.iss` | Inno Setup script defines instructions to build the exe installer file |
| `setup/jre-8u221/` | Java SE Runtime Environment (JRE) which is included in the installer to provide jre on the target platform (windows tablet) |
| `src/` | Contains all source files  |
| `src/main/java/..mobifume/` | Contains all .java source files |
| `src/main/java/..mobifume/Mobifume.java` | Entry point of the application (main-method) |
| `src/main/java/..mobifume/controller/` | Contains the controllers which react on user inputs and connects the view with the model |
| `src/main/java/..mobifume/model/` | Contains all logic of the program (establish connection to broker, create/delete groups, start/stop processes, ...) |
| `src/main/java/..mobifume/util/` | Contains util classes (file manager, logger, localization, setting) |
| `src/main/java/..mobifume/view/` | Contains view related classes (outsourced to `src/main/resources/view` with fxml files) |
| `src/main/java/..mobifume/view/MobiApplication.java` | JavaFX Application main class which initializes the window and loads the main fxml file |
| `src/main/resources/` | Contains all resources |
| `src/main/resources/project.properties` | Contains project properties which will be filtered by maven |
| `src/main/resources/settings.properties` | Contains settings (broker connection credentials, mqtt channels, filter prefix) |
| `src/main/resources/font/` | Contains additional fonts |
| `src/main/resources/images/` | Contains all images |
| `src/main/resources/localization/` | Contains resource bundles to translate the application to other languages (`src/main/java/..mobifume/util/localization/LocaleFileHandler#copyResources` copies each file individual) |
| `src/main/resources/sounds/` | Contains all sounds |
| `src/main/resources/view/` | Contains all .fxml files (JavaFX) which defines the structure of the UI |
| `src/test/` | Contains tests (no test coverage yet) |

## Notes

- The program must be executed as administrator to automatically show/hide the on-screen keyboard TabTip.
- The program must be installed in `C:\Program Files\MOBIfume\` otherwise the autostart won't work.
- For autostart, windows task scheduler is used because this allows the program to start as admin.
- The installer overwrites the default user account picture in `C:\ProgramData\Microsoft\User Account Pictures\user.png` to set the mobifume icon as account picture.
- Program files are stored in `%localappdata%/MOBIfume`.
    It contains:
    - `languagesettings` stores the selected language.
    - `settings` stores the global settings.
    - `filter/` stores all filters for filter management. Files are manually editable (json format).
    - `language/` stores the localization files as properties. A new language can be added by simple add a file named `MOBIfume_xx_XX.properties` (replace xx_XX with country code) and copy/modify the key-value pairs from the other language files.
    - `paho/` contains files related to eclipse paho for the mqtt client connection
All files are deleted when the program is reinstalled or uninstalled.
- Log files are stored in `%userprofile%/documents/MOBIfume`. These files will never be deleted.
