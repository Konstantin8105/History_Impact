rmdir /S /Q bin
mkdir bin

cd src

rem javac -target 1.5 -cp .;%CLASSPATH%;..\lib\jogl.jar;..\lib\gluegen-rt.jar;..\lib\patbinfree153.jar -O -d ..\bin j3d\*.java run\*.java gui\*.java jp\lang\*.java jp\sync\*.java uka\transport\*.java uka\util\*.java uka\patch\*.java uka\karmi\rmi\*.java uka\karmi\rmi\server\*.java uka\karo\*.java
javac -cp .;..\lib\jogl.jar;..\lib\gluegen-rt.jar;..\lib\patbinfree153.jar;..\lib_j3d\j3dcore.jar;..\lib_j3d\j3dutils.jar;..\lib_j3d\vecmath.jar;..\lib\LogDB.jar;..\lib\mysql-connector-java-5.1.11-bin.jar -O -d ..\bin run\*.java gui\*.java 
pause

copy gui\*.gif ..\bin\gui\*.gif
copy gui\*.jnlp ..\bin\gui\*.jnlp
copy gui\*.gif ..\doc\Pictures\*.gif
copy gui\*.png ..\bin\gui\*.png
copy gui\*.png ..\doc\Pictures\*.png
rem echo java -Xmx1000m -cp .;doc;bin;Impact.jar gui.ImpactGUI > ..\ImpactGUI.bat

cd ..
rem cd bin

rem jar -cf Impact.jar run\*.* Jama\*.* j3d\*.* gui\*.* jp\*.* uka\*.* util\*.*
rem copy Impact.jar ..\Impact.jar
rem del Impact.jar

rem cd ..
rem cd doc

rem jar -uf ..\Impact.jar *.*

rem cd ..

rem jar -uf Impact.jar lib examples License.txt


pause