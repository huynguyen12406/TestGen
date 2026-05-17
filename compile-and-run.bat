@echo off
echo.
echo ========================================
echo    Compiling TestGen...
echo ========================================
echo.

javac -version >nul 2>&1
if errorlevel 1 goto :no_java

if exist out rmdir /s /q out
mkdir out

set CP=lib\okhttp-4.12.0.jar;lib\okio-jvm-3.6.0.jar;lib\jackson-databind-2.15.2.jar;lib\jackson-core-2.15.2.jar;lib\jackson-annotations-2.15.2.jar;lib\kotlin-stdlib-1.9.10.jar

echo [INFO] Compiling Java files...
echo.

javac -encoding UTF-8 -d out -cp "%CP%" -sourcepath src\main\java src\main\java\vn\testgen\Main.java src\main\java\vn\testgen\ui\*.java src\main\java\vn\testgen\backend\*.java src\main\java\vn\testgen\model\*.java src\main\java\vn\testgen\util\*.java src\main\java\com\competitive\ai\*.java src\main\java\com\competitive\model\*.java src\main\java\com\competitive\service\*.java

if errorlevel 1 goto :compile_error

echo.
echo [INFO] Compilation successful!
echo.
echo ========================================
echo    Running TestGen...
echo ========================================
echo.

java -cp "out;%CP%" vn.testgen.Main

if errorlevel 1 goto :run_error

echo.
echo [INFO] Application closed.
pause
exit /b 0

:no_java
echo [ERROR] Java compiler (javac) not found!
echo Please install JDK 11 or higher
pause
exit /b 1

:compile_error
echo.
echo [ERROR] Compilation failed!
echo Please check the error messages above.
pause
exit /b 1

:run_error
echo.
echo [ERROR] Application exited with error!
pause
exit /b 1
