@echo off
setlocal ENABLEDELAYEDEXPANSION

rem BUILD REQUIREMENTS
rem Go must be installed and in your path, as it is used to build the app image binary launchers
rem The jmods directories for both the JDK and JavaFX SDK both need to include windows, linux, and mac subdirectories containing the jmods for those respective operating systems
rem The following environmental variables need to be set either system-wide, for your user, or you can set them here
rem set JAVA_HOME=C:\path-to-JDK-17
rem set JAVAFX_HOME=C:\path-to-javafx-sdk-17
rem set PATH=%JAVAFX_HOME%\bin;%JAVA_HOME%\bin;%PATH%

echo Generate class path including all JavaFX libraries...
for /f %%0 in ('dir /b "%JAVAFX_HOME%\lib" ^| findstr /e .jar') do set JAVAFX_CLASSES=%JAVAFX_HOME%\lib\%%0;!JAVAFX_CLASSES!

echo Set build information...
set PROJECT=TorPanel
set PACKAGE=torpanel
set ENTRY=Main
set DEPS=javafx.fxml,javafx.controls

echo Clean up old release files...
if exist Release rd /s /q Release

echo Set Java version to build...
set JV=8

echo Compile package source files...
javac.exe -cp "%JAVAFX_CLASSES%" -d Release\%JV% src\%PACKAGE%\*.java --release %JV%

echo Copy fxml gui to compiled package directory...
md Release\%JV%\%PACKAGE%\gui
copy src\torpanel\gui Release\%JV%\%PACKAGE%\gui > nul

echo Archive package to jar file...
cd Release\%JV%
jar.exe cfe %PROJECT%.jar %PACKAGE%.%ENTRY% %PACKAGE%\*.class %PACKAGE%\gui\*.fxml
cd ..\..

echo Set Java version to build...
set JV=17

echo Compile module source files...
javac.exe -p "%JAVAFX_HOME%\lib" --add-modules %DEPS% -d Release\%JV% src\%PACKAGE%\*.java --release %JV%

echo Copy fxml gui to compiled module directory...
md Release\%JV%\%PACKAGE%\gui
copy src\%PACKAGE%\gui Release\%JV%\%PACKAGE%\gui > nul

echo Archive module to jar file...
cd Release\%JV%
jar.exe cfe %PROJECT%.jar %PACKAGE%.%ENTRY% %PACKAGE%\*.class %PACKAGE%\gui\*.fxml
cd ..\..

echo Compile module-info source file...
javac.exe -p "Release\%JV%\%PROJECT%.jar;%JAVAFX_HOME%\lib" --add-modules %DEPS%,%PACKAGE% -d Release\%JV% src\module-info.java

echo Update jar file with compiled module-info...
cd  Release\%JV%
jar.exe uf %PROJECT%.jar module-info.class
cd ..\..

echo Build app image for Windows...
jlink.exe -p "Release\%JV%\%PROJECT%.jar;%JAVAFX_HOME%\jmods\windows;%JAVA_HOME%\jmods\windows" --add-modules %DEPS%,%PACKAGE% --compress=2 --no-header-files --no-man-pages --strip-debug --output Release\%JV%\%PROJECT%-Windows
copy README.md Release\%JV%\%PROJECT%-Windows > nul
copy LICENSE Release\%JV%\%PROJECT%-Windows > nul

echo Build launcher for Windows...
set GOOS=windows
go build -ldflags="-s -w -H=windowsgui" -o Release\%JV%\%PROJECT%-Windows\%PROJECT%.exe src\torpanel.go src\include_windows.go

echo Build app image for Linux...
jlink.exe -p "Release\%JV%\%PACKAGE%.jar;%JAVAFX_HOME%\jmods\linux;%JAVA_HOME%\jmods\linux" --add-modules %DEPS%,%PACKAGE% --compress=2 --no-header-files --no-man-pages --strip-debug --output Release\%JV%\%PROJECT%-Linux
copy README.md Release\%JV%\%PROJECT%-Linux > nul
copy LICENSE Release\%JV%\%PROJECT%-Linux > nul

echo Build launcher for Linux...
set GOOS=linux
go build -ldflags="-s -w" -o Release\%JV%\%PROJECT%-Linux\%PROJECT% src\torpanel.go src\include_other.go

echo Build app image for Mac...
jlink.exe -p "Release\%JV%\%PROJECT%.jar;%JAVAFX_HOME%\jmods\mac;%JAVA_HOME%\jmods\mac" --add-modules %DEPS%,%PACKAGE% --compress=2 --no-header-files --no-man-pages --strip-debug --output Release\%JV%\%PROJECT%-Mac
copy README.md Release\%JV%\%PROJECT%-Mac > nul
copy LICENSE Release\%JV%\%PROJECT%-Mac > nul

echo Build launcher for Mac...
set GOOS=darwin
go build -ldflags="-s -w" -o Release\%JV%\%PROJECT%-Mac\%PROJECT%.app src\torpanel.go src\include_other.go

echo Build complete
pause