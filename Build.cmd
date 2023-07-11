@echo off

echo Set build information...
set PROJECT=TorPanel
set PACKAGE=torpanel
set ENTRY=Main

if exist Release (
	echo Cleaning up old release files...
	rd /s /q Release
)

set JV=8
call :Build

set JV=17
call :Build

echo Build complete
exit /b

:Build
echo Compiling source files to Java %JV%...
if %JV% leq 8 (javac -d Release\%JV% src\%PACKAGE%\*.java --release %JV%
) else javac -d Release\%JV% src\%PACKAGE%\*.java src\module-info.java --release %JV%

echo Archiving to Java %JV% jar file...
cd Release\%JV%
if %JV% leq 8 (jar cfe %PROJECT%-%JV%.jar %PACKAGE%.%ENTRY% %PACKAGE%\*.class
) else jar cfe %PROJECT%-%JV%.jar %PACKAGE%.%ENTRY% %PACKAGE%\*.class module-info.class
cd ..\..

exit /b
