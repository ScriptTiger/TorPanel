@echo off

echo Set build information...
set PROJECT=TorPanel
set PACKAGE=torpanel
set ENTRY=Main

echo Clean up old release files...
if exist Release rd /s /q Release

if not exist Proguard.cfg (
	echo Writing Proguard.cfg...
	(
		echo -ignorewarnings
		echo -keep class %PACKAGE%.%ENTRY% {public static void main^(java.lang.String[]^);}
	) > Proguard.cfg
)

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

echo Archive to Java debug jar file...
cd Release\%JV%
if %JV% leq 8 (jar cfe %PROJECT%-%JV%-debug.jar %PACKAGE%.%ENTRY% %PACKAGE%\*.class
) else jar cfe %PROJECT%-%JV%-debug.jar %PACKAGE%.%ENTRY% %PACKAGE%\*.class module-info.class
cd ..\..

echo Shrink to release jar file...
java -cp %R8% com.android.tools.r8.R8 --release --classfile --output Release\%JV%\%PROJECT%-%JV%.jar --pg-conf Proguard.cfg --lib %JAVA_HOME% Release\%JV%\%PROJECT%-%JV%-debug.jar

exit /b
