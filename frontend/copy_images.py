import shutil
import os

# 소스와 타겟 경로
source_images = r"C:\OnAndHome\src\main\resources\static\images"
target_images = r"C:\onandhomefront\public\images"

source_product = r"C:\OnAndHome\src\main\resources\static\product_img"
target_product = r"C:\onandhomefront\public\product_img"

# 이미지 폴더 복사
try:
    if os.path.exists(source_images):
        for item in os.listdir(source_images):
            s = os.path.join(source_images, item)
            d = os.path.join(target_images, item)
            if os.path.isfile(s):
                shutil.copy2(s, d)
                print(f"Copied: {item}")
    print("Images copied successfully!")
except Exception as e:
    print(f"Error copying images: {e}")

# 제품 이미지 복사
try:
    if os.path.exists(source_product):
        for item in os.listdir(source_product):
            s = os.path.join(source_product, item)
            d = os.path.join(target_product, item)
            if os.path.isfile(s):
                shutil.copy2(s, d)
                print(f"Copied: {item}")
    print("Product images copied successfully!")
except Exception as e:
    print(f"Error copying product images: {e}")
