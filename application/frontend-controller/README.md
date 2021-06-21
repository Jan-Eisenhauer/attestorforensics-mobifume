# MOBIfume

The all new mobile cyanoacrylate fuming system for the development of latent fingerprints at the scene, in fuming tents or fuming rooms.
This application is responsible for the MOBIfume controller of the system on a tablet.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development purposes.

### Prerequisites

- [Java SE Development Kit 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (oracle JDK)
- [Eclipse](https://www.eclipse.org/downloads/packages/release/2019-06/r/eclipse-ide-java-developers) (IDE)
- [Inno Setup 6.0.2](http://www.jrsoftware.org/isdl.php) (Creates an .exe installer for windows)

### Installing and Building (Linux)

1. Install JDK 8
    - `sudo apt install openjdk-8-jdk`
2. Install Eclipse for Java Developers:
    - Download [Eclipse](https://www.eclipse.org/downloads/packages/release/2019-06/r/eclipse-ide-java-developers) for Linux
    - Unpack downloaded file with `tar xfz eclipse-*.tar.gz`
    - Start eclipse with `eclipse/eclipse`
3. Install JavaFX library (OpenJDK-8 for Linux doesn't include JavaFX)
    - `sudo apt install openjfx=8u161-b12-1ubuntu2 libopenjfx-java=8u161-b12-1ubuntu2 libopenjfx-jni=8u161-b12-1ubuntu2`
4. Import the project with maven in eclipse:
    - Click on File -> Import
    - Select Maven -> Existing Maven Projects -> Next
    - Browse to root directory of the project -> Finish
5. Install Inno Setup
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
    - The jar file can be found in `mobifumecore/target/`
    - The installer .exe file can be found in `mobifumeinstaller/target/`

## Running (Windows)

1. Execute the installer on the windows tablet.
2. Go through the installation process. The target directory must be `C:\Program Files\MOBIfume\` otherwise the autostart won't work.

## Dependencies

- [JavaFX](https://openjfx.io/) - Library for client application (UI)
- [GSON](https://mvnrepository.com/artifact/com.google.code.gson/gson) - Library to work with JSON
- [log4j](https://mvnrepository.com/artifact/log4j/log4j) - Library for Logging in Java
- [Eclipse Paho](https://www.eclipse.org/paho/) - MQTT Client

## Build With

- [Maven](https://maven.apache.org/) - Build Management Tool / Dependency Management
- [Inno Setup](http://www.jrsoftware.org/isinfo.php) - Compiler to build exe file

## Versioning

For versioning [SemVer](https://semver.org/) is used. Version is set in [pom.xml](pom.xml).

## File Structure and Directory Layout

The project is divided in three modules: core, installer, updater

```
    └───frontend-controller
        ├───pom.xml (parent)
        ├───mobifumecore
        │   ├───pom.xml
        │   └───src
        │       └───main
        │           ├───java
        │           │   └───com.attestorforensics.mobifumecore
        │           │       ├───Mobifume.java (entrypoint)
        │           │       ├───controller
        │           │       ├───model
        │           │       ├───util
        │           │       └───view
        │           │           └───MobiApplication.java (Java FX application)
        │           └───resources
        │               ├───project.properties
        │               ├───config.properties
        │               ├───font
        │               ├───i18n
        │               ├───images
        │               ├───sounds
        │               └───view
        ├───mobifumeinstaller
        │   ├───pom.xml
        │   └───src
        │       └───iscc
        │           ├───MOBIfumeTask.xml
        │           ├───Setup.iss
        │           ├───images
        │           └───jre-8u221
        └───mobifumeupdater
            ├───pom.xml
            └───src
                ├───assembly
                │   └───update.xml
                └───main
                    └───java
                        └───com.attestorforensics.mobifumeupdater
                            └───MobifumeUpdater.java (entrypoint)
```

| File / Directory | Description |
| --- | --- |
| `pom.xml` | Parent maven file defines modules |
| `mobifumecore` | Core mobifume application |
| `mobifumeinstaller` | Installer for windows using inno setup |
| `mobifumeupdater` | Updater for mobifume |
| `mobifumecore/pom.xml` | Maven file for the core project defines dependencies and build instructions |
| `mobifumecore/src` | Contains all source files of the core project |
| `mobifumecore/src/main/java/..mobifumecore/` | Contains all .java source files |
| `mobifumecore/src/main/java/..mobifumecore/Mobifume.java` | Entry point of the application (main-method) |
| `mobifumecore/src/main/java/..mobifumecore/controller/` | Contains the controllers which react on user inputs and connects the view with the model |
| `mobifumecore/src/main/java/..mobifumecore/model/` | Contains all logic of the program (establish connection to broker, create/delete groups, start/stop processes, ...) |
| `mobifumecore/src/main/java/..mobifumecore/util/` | Contains util classes (file manager, logger, localization, setting) |
| `mobifumecore/src/main/java/..mobifumecore/view/` | Contains view related classes (outsourced to `src/main/resources/view` with fxml files) |
| `mobifumecore/src/main/java/..mobifumecore/view/MobiApplication.java` | JavaFX Application main class which initializes the window and loads the main fxml file |
| `mobifumecore/src/main/resources/` | Contains all resources |
| `mobifumecore/src/main/resources/project.properties` | Contains project properties which will be filtered by maven |
| `mobifumecore/src/main/resources/config.properties` | Contains settings (broker connection credentials, mqtt channels, filter prefix) |
| `mobifumecore/src/main/resources/font/` | Contains additional fonts |
| `mobifumecore/src/main/resources/i18n/` | Contains resource bundles to translate the application to other languages (`src/main/java/..mobifume/util/localization/LocaleFileHandler#copyResources` copies each file individual) |
| `mobifumecore/src/main/resources/images/` | Contains all images |
| `mobifumecore/src/main/resources/sounds/` | Contains all sounds |
| `mobifumecore/src/main/resources/view/` | Contains all .fxml files (JavaFX) which defines the structure of the UI |
| `mobifumeinstaller/pom.xml` | Maven file for the installer project uses inno setup to create installer |
| `mobifumeinstaller/src/iscc/MOBIfumeTask.xml` | Task for windows task scheduler for autostart |
| `mobifumeinstaller/src/iscc/Setup.iss` | Inno Setup script defines instructions to build the exe installer file |
| `mobifumeinstaller/src/iscc/images/` | Contains the logo of mobifume for the installer |
| `mobifumeinstaller/src/iscc/jre-8u221` | Java SE Runtime Environment (JRE) which is included in the installer to provide jre on the target platform (windows tablet) |
| `mobifumeupdater/pom.xml` | Maven file for the updater project |
| `mobifumeupdater/assembly/update.xml` | Assembly instruction to create a compressed update file including the core project |
| `mobifumeupdater/main/java/..mobifumeupdater/MobifumeUpdater` | Entry point of the updater application |

## Notes

- The program must be executed as administrator to automatically show/hide the on-screen keyboard TabTip.
- The program must be installed in `C:\Program Files\MOBIfume\` otherwise the autostart won't work.
- For autostart, windows task scheduler is used because this allows the program to start as admin.
- The installer overwrites the default user account picture in `C:\ProgramData\Microsoft\User Account Pictures\user.png` to set the mobifume icon as account picture.
- Program files are stored in `%localappdata%/MOBIfume`.
    It contains:
    - `settings` stores the global settings.
    - `filter/` stores all filters for filter management. Files are manually editable (json format).
    - `paho/` contain files related to eclipse paho for the mqtt client connection
- All files except log files are deleted when the program is reinstalled or uninstalled.
- Log files are stored in `%userprofile%/documents/MOBIfume`. These files will never be deleted.
