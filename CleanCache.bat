@echo off

@echo=
:clean
set /p o=�����Ŀ(%~dp0)�еĻ����ļ���ȷ��ִ���� (Y/N):
if /i "%o%"=="y" goto yes
if /i "%o%"=="n" goto no
goto clean
:yes

@echo=
@echo ���������Ŀ¼.gradle��.idea��build
for /f "delims=" %%i in ('dir /ad/b/a "%dir%"') do (
if %~dp0%%i==%~dp0.gradle (
@echo ɾ��%~dp0%%i
rd /S/Q %%i
)
if %~dp0%%i==%~dp0.idea (
@echo ɾ��%~dp0%%i
rd /S/Q %%i
)
if %~dp0%%i==%~dp0build (
@echo ɾ��%~dp0%%i
rd /S/Q %%i
)
)

@echo=
@echo �����������Module�е�buildĿ¼
set dir=build
for /f "delims=" %%i in ('dir /ad/b/a/s "%dir%"') do (
@echo ɾ��%%i
rd /S/Q %%i
)

@echo=
@echo �������debugĿ¼
set dir=debug
for /f "delims=" %%i in ('dir /ad/b/a/s "%dir%"') do (
@echo ɾ��%%i
rd /S/Q %%i
)

@echo=
@echo �������releaseĿ¼
set dir=release
for /f "delims=" %%i in ('dir /ad/b/a/s "%dir%"') do (
@echo ɾ��%%i
rd /S/Q %%i
)

@echo=
@echo �����������Ŀ¼�е�*iml�ļ�
for /f "delims=" %%i in ('dir /a/s/b "*.iml"') do (
del %%i
)
pause 

:no
exit