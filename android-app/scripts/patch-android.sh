#!/usr/bin/env bash
# 在 `npx cap add android` 之后运行：加相机权限、用项目图标替换默认图标
set -euo pipefail
cd "$(dirname "$0")/.."
MANIFEST=android/app/src/main/AndroidManifest.xml
grep -q 'android.permission.CAMERA' "$MANIFEST" || \
  sed -i 's#<uses-permission android:name="android.permission.INTERNET" />#&\n    <uses-permission android:name="android.permission.CAMERA" />#' "$MANIFEST"

grep -q 'android.hardware.camera' "$MANIFEST" || \
  sed -i 's#<uses-permission android:name="android.permission.CAMERA" />#&\n    <uses-feature android:name="android.hardware.camera" android:required="false" />#' "$MANIFEST"

# 自定义 MainActivity：上传图片时可选“拍照”或“相册”
cp native/MainActivity.java android/app/src/main/java/com/yeyhome/wrongnotebook/MainActivity.java

RES=android/app/src/main/res
rm -rf "$RES/mipmap-anydpi-v26"
# 图标已按各密度预先缩放好，放在 icons/ 下（源图 ../public/icons/icon.png）
for d in mdpi hdpi xhdpi xxhdpi xxxhdpi; do
  mkdir -p "$RES/mipmap-$d"
  cp "icons/ic_launcher-$d.png" "$RES/mipmap-$d/ic_launcher.png"
  cp "icons/ic_launcher-$d.png" "$RES/mipmap-$d/ic_launcher_round.png"
  rm -f "$RES/mipmap-$d/ic_launcher_foreground.png"
done
