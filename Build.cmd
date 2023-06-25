@echo off

echo Set build information...
set PROJECT=TorPanel
set PACKAGE=torpanel
set ENTRY=%PROJECT%Kt
set JV=1.8

if exist Release (
	echo Cleaning up old release files...
	rd /s /q Release
)

if not exist Proguard.cfg (
	echo Writing Proguard.cfg...
	(
		echo -ignorewarnings
		echo -keep class %PACKAGE%.%ENTRY% {public static void main^(java.lang.String[]^);}
	) > Proguard.cfg
)

echo Building Release\%PROJECT%-debug.jar...
call kotlinc -d Release\%PROJECT%-debug.jar -include-runtime -jvm-target %JV% %PROJECT%.kt
echo Shrinking...
java -cp %R8% com.android.tools.r8.R8 --release --classfile --output Release\TorPanel.jar --pg-conf Proguard.cfg --lib %JAVA_HOME% Release\TorPanel-debug.jar
echo Build complete
pause
exit /b