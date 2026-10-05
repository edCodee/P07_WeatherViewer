@echo off
set PKG=ec.edu.uta.fisei.weatherviewer
set ACT=%PKG%/.ui.forecast.MainActivity
set IMG=docs\img
if not exist %IMG% mkdir %IMG%

:: Forzamos el uso del Pixel 7
set ADB=adb -s emulator-5554

echo Configurando estado base...
%ADB% shell settings put system accelerometer_rotation 0
%ADB% shell settings put system user_rotation 0
%ADB% shell cmd uimode night no
%ADB% shell cmd connectivity airplane-mode disable

echo 1. Capturando inicio...
%ADB% shell am force-stop %PKG%
%ADB% shell am start -n %ACT% >nul
timeout /t 3 >nul
%ADB% exec-out screencap -p > %IMG%\01_inicio.png

echo 2. Escribiendo "Quito, EC"...
%ADB% shell input text "Quito,%%sEC"
echo ---------------------------------------------------
echo MANUAL: Toca el boton redondo (FAB) de busqueda
echo y espera a que aparezca la lista del clima.
echo Luego presiona ENTER aqui en la consola.
echo ---------------------------------------------------
pause >nul
%ADB% exec-out screencap -p > %IMG%\02_pronostico.png

echo 4. Cambiando a horizontal...
%ADB% shell settings put system user_rotation 1
timeout /t 4 >nul
%ADB% exec-out screencap -p > %IMG%\04_horizontal.png
%ADB% shell settings put system user_rotation 0
timeout /t 3 >nul

echo 5. Cambiando a modo oscuro...
%ADB% shell cmd uimode night yes
timeout /t 4 >nul
%ADB% exec-out screencap -p > %IMG%\05_oscuro.png
%ADB% shell cmd uimode night no
timeout /t 3 >nul

echo 6. Cambiando idioma a espanol...
%ADB% shell cmd locale set-app-locales %PKG% --locales es-EC
timeout /t 4 >nul
%ADB% exec-out screencap -p > %IMG%\06_espanol_cambio.png
echo ---------------------------------------------------
echo 7. MANUAL: Toca de nuevo el boton de busqueda,
echo espera a que carguen los datos en espanol y presiona ENTER.
echo ---------------------------------------------------
pause >nul
%ADB% exec-out screencap -p > %IMG%\07_espanol.png
%ADB% shell cmd locale set-app-locales %PKG% --locales en-US
timeout /t 3 >nul

echo 8. Probando modo sin conexion...
%ADB% shell cmd connectivity airplane-mode enable
timeout /t 3 >nul
echo ---------------------------------------------------
echo MANUAL: Toca el boton de busqueda y, APENAS aparezca 
echo el mensaje de error (Snackbar), presiona ENTER.
echo (Dura 3 seg. Si no alcanzas, tomatela manual despues).
echo ---------------------------------------------------
pause >nul
%ADB% exec-out screencap -p > %IMG%\08_sin_conexion.png
%ADB% shell cmd connectivity airplane-mode disable

echo 3. Probando ciudad inexistente...
echo ---------------------------------------------------
echo MANUAL: Borra el campo de texto en el emulador,
echo escribe "Ciudadquenoexiste123", toca el boton de busqueda
echo y APENAS salga el mensaje, presiona ENTER.
echo ---------------------------------------------------
pause >nul
%ADB% exec-out screencap -p > %IMG%\03_error_ciudad.png

echo.
echo Listo el telefono. Revisa la carpeta docs\img.