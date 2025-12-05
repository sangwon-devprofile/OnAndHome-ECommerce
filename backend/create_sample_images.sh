#!/bin/bash
# =============================================
# OnAndHome 상품 이미지 파일 생성 스크립트
# =============================================
#
# 실제 이미지가 없을 경우, 기존 샘플 이미지를 복사해서 
# DB에서 참조하는 파일명으로 생성합니다.
#
# 실행 방법: Git Bash에서 이 스크립트를 실행
# =============================================

cd /c/OnAndHome/src/main/resources/static/product_img/

# 샘플 이미지 (thum_01.jpg)를 기준으로 복사
SAMPLE_IMAGE="thum_01.jpg"

# TV 카테고리 이미지
for i in {01..06}; do
    cp "$SAMPLE_IMAGE" "tv_${i}.jpg"
    cp "$SAMPLE_IMAGE" "tv_${i}_d.jpg"
done

# 주방가전 (kit) 이미지
for i in {01..18}; do
    cp "$SAMPLE_IMAGE" "kit_$(printf "%02d" $i).jpg"
    cp "$SAMPLE_IMAGE" "kit_$(printf "%02d" $i)_d.jpg"
done

# 생활가전 (life) 이미지
for i in {01..12}; do
    cp "$SAMPLE_IMAGE" "life_$(printf "%02d" $i).jpg"
    cp "$SAMPLE_IMAGE" "life_$(printf "%02d" $i)_d.jpg"
done

# 에어컨/공기청정기 (air) 이미지
for i in {01..12}; do
    cp "$SAMPLE_IMAGE" "air_$(printf "%02d" $i).jpg"
    cp "$SAMPLE_IMAGE" "air_$(printf "%02d" $i)_d.jpg"
done

# 오디오 (au) 이미지
for i in {01..06}; do
    cp "$SAMPLE_IMAGE" "au_$(printf "%02d" $i).jpg"
    cp "$SAMPLE_IMAGE" "au_$(printf "%02d" $i)_d.jpg"
done

# 기타 (etc) 이미지
for i in {01..18}; do
    cp "$SAMPLE_IMAGE" "etc_$(printf "%02d" $i).jpg"
    cp "$SAMPLE_IMAGE" "etc_$(printf "%02d" $i)_d.jpg"
done

echo "이미지 파일 생성 완료!"
ls -la *.jpg | wc -l
echo "개의 이미지 파일이 생성되었습니다."
