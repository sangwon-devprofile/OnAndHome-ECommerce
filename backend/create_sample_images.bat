@echo off
REM =============================================
REM OnAndHome 상품 이미지 파일 생성 스크립트 (Windows)
REM =============================================
REM
REM 실제 이미지가 없을 경우, 기존 샘플 이미지를 복사해서 
REM DB에서 참조하는 파일명으로 생성합니다.
REM
REM 실행 방법: 이 배치 파일을 더블클릭
REM =============================================

cd /d C:\OnAndHome\src\main\resources\static\product_img\

SET SAMPLE_IMAGE=thum_01.jpg

echo 이미지 파일 생성 중...

REM TV 카테고리 이미지
for %%i in (01 02 03 04 05 06) do (
    copy "%SAMPLE_IMAGE%" "tv_%%i.jpg" >nul
    copy "%SAMPLE_IMAGE%" "tv_%%i_d.jpg" >nul
)

REM 주방가전 (kit) 이미지
for %%i in (01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18) do (
    copy "%SAMPLE_IMAGE%" "kit_%%i.jpg" >nul
    copy "%SAMPLE_IMAGE%" "kit_%%i_d.jpg" >nul
)

REM 생활가전 (life) 이미지
for %%i in (01 02 03 04 05 06 07 08 09 10 11 12) do (
    copy "%SAMPLE_IMAGE%" "life_%%i.jpg" >nul
    copy "%SAMPLE_IMAGE%" "life_%%i_d.jpg" >nul
)

REM 에어컨/공기청정기 (air) 이미지
for %%i in (01 02 03 04 05 06 07 08 09 10 11 12) do (
    copy "%SAMPLE_IMAGE%" "air_%%i.jpg" >nul
    copy "%SAMPLE_IMAGE%" "air_%%i_d.jpg" >nul
)

REM 오디오 (au) 이미지
for %%i in (01 02 03 04 05 06) do (
    copy "%SAMPLE_IMAGE%" "au_%%i.jpg" >nul
    copy "%SAMPLE_IMAGE%" "au_%%i_d.jpg" >nul
)

REM 기타 (etc) 이미지
for %%i in (01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18) do (
    copy "%SAMPLE_IMAGE%" "etc_%%i.jpg" >nul
    copy "%SAMPLE_IMAGE%" "etc_%%i_d.jpg" >nul
)

echo.
echo 이미지 파일 생성 완료!
dir *.jpg | find /c ".jpg"
echo 개의 이미지 파일이 생성되었습니다.
echo.
pause
