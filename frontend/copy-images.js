const fs = require('fs');
const path = require('path');

// 소스와 타겟 경로
const sourceImagesDir = 'C:\\OnAndHome\\src\\main\\resources\\static\\images';
const targetImagesDir = 'C:\\onandhomefront\\public\\images';

const sourceProductDir = 'C:\\OnAndHome\\src\\main\\resources\\static\\product_img';
const targetProductDir = 'C:\\onandhomefront\\public\\product_img';

// 디렉토리 생성 함수
function ensureDirectoryExists(directory) {
  if (!fs.existsSync(directory)) {
    fs.mkdirSync(directory, { recursive: true });
    console.log(`Created directory: ${directory}`);
  }
}

// 파일 복사 함수
function copyFiles(sourceDir, targetDir) {
  if (!fs.existsSync(sourceDir)) {
    console.log(`Source directory does not exist: ${sourceDir}`);
    return 0;
  }

  ensureDirectoryExists(targetDir);

  const files = fs.readdirSync(sourceDir);
  let copiedCount = 0;

  files.forEach(file => {
    const sourcePath = path.join(sourceDir, file);
    const targetPath = path.join(targetDir, file);

    if (fs.lstatSync(sourcePath).isFile()) {
      try {
        fs.copyFileSync(sourcePath, targetPath);
        console.log(`Copied: ${file}`);
        copiedCount++;
      } catch (error) {
        console.error(`Error copying ${file}:`, error.message);
      }
    }
  });

  return copiedCount;
}

// 메인 실행
console.log('Starting image copy process...\n');

console.log('Copying images...');
const imagesCopied = copyFiles(sourceImagesDir, targetImagesDir);
console.log(`\nImages copied: ${imagesCopied}`);

console.log('\nCopying product images...');
const productsCopied = copyFiles(sourceProductDir, targetProductDir);
console.log(`\nProduct images copied: ${productsCopied}`);

console.log('\n✅ Image copy process completed!');
console.log(`Total files copied: ${imagesCopied + productsCopied}`);
