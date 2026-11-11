#!/usr/bin/env bash
# 在 `npx cap add android` 之后运行：加相机权限、用项目图标替换默认图标
set -euo pipefail
cd "$(dirname "$0")/.."
MANIFEST=android/app/src/main/AndroidManifest.xml
grep -q 'android.permission.CAMERA' "$MANIFEST" || \
  sed -i 's#<uses-permission android:name="android.permission.INTERNET" />#&\n    <uses-permission android:name="android.permission.CAMERA" />#' "$MANIFEST"

ICON=../public/icons/icon.png
RES=android/app/src/main/res
rm -rf "$RES/mipmap-anydpi-v26"
for pair in mdpi:48 hdpi:72 xhdpi:96 xxhdpi:144 xxxhdpi:192; do
  d=${pair%%:*}; s=${pair##*:}
  mkdir -p "$RES/mipmap-$d"
  convert "$ICON" -resize ${s}x${s} "$RES/mipmap-$d/ic_launcher.png"
  cp "$RES/mipmap-$d/ic_launcher.png" "$RES/mipmap-$d/ic_launcher_round.png"
  rm -f "$RES/mipmap-$d/ic_launcher_foreground.png"
done
