@echo off

echo Set build information...
set PROJECT=TorPanel
set PACKAGE=torpanel
set ENTRY=Main

echo Clean up old release files...
if exist Release rd /s /q Release

set JV=8
call :Build

set JV=17
call :Build

echo Build complete
pause
exit /b

:Build
echo Compile source files to Java %JV%...
if %JV% leq 8 (javac -d Release\%JV% src\%PACKAGE%\*.java --release %JV%
) else javac -d Release\%JV% src\%PACKAGE%\*.java src\module-info.java --release %JV%

echo Archive to Java %JV% jar file...
cd Release\%JV%
if %JV% leq 8 (jar cfe %PROJECT%-%JV%.jar %PACKAGE%.%ENTRY% %PACKAGE%\*.class
) else jar cfe %PROJECT%-%JV%.jar %PACKAGE%.%ENTRY% %PACKAGE%\*.class module-info.class
cd ..\..

exit /b
