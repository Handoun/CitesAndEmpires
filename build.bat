@echo off
chcp 65001 >nul
echo ============================================
echo  Сборка плагина CitiesAndEmpires для 1.20.1
echo ============================================
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [ОШИБКА] Maven не найден. Установите Maven и добавьте в PATH.
    pause
    exit /b 1
)
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo [ОШИБКА] Сборка не удалась.
    pause
    exit /b 1
)
set SERVER_PLUGINS=C:\MinecraftServer\plugins
if not exist "%SERVER_PLUGINS%" (
    echo [ПРЕДУПРЕЖДЕНИЕ] Папка %SERVER_PLUGINS% не найдена. Плагин скопирован в текущую директорию.
    copy /Y target\CitiesAndEmpires-*.jar . >nul
) else (
    copy /Y target\CitiesAndEmpires-*.jar "%SERVER_PLUGINS%" >nul
    echo Плагин успешно скопирован в %SERVER_PLUGINS%
)
echo Готово! Перезапустите сервер.
pause
