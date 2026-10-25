#!/bin/bash
set -e
SERVER="admin@music.stellortus.top"
REMOTE_DIR="/srv/Stellar-Music-Server/apks"

VERSION_CODE=$(awk '/^[[:space:]]*versionCode[[:space:]]*=/ { sub(/^[[:space:]]*versionCode[[:space:]]*=[[:space:]]*/, ""); sub(/[[:space:]]*$/, ""); print }' app/build.gradle.kts)
if [[ ! "$VERSION_CODE" =~ ^[1-9][0-9]*$ ]]; then
    echo ">>> 错误：无法读取唯一的正整数 versionCode。" >&2
    exit 1
fi

VERSION_NAME=$(awk '/^[[:space:]]*versionName[[:space:]]*=/ { sub(/^[[:space:]]*versionName[[:space:]]*=[[:space:]]*"/, ""); sub(/"[[:space:]]*$/, ""); print }' app/build.gradle.kts)
if [[ ! "$VERSION_NAME" =~ ^[a-zA-Z0-9][a-zA-Z0-9._+-]*$ ]]; then
    echo ">>> 错误：无法读取唯一的有效 versionName。" >&2
    exit 1
fi

echo ">>> 正在打包..."
./gradlew assembleRelease

echo ">>> 正在上传 APK..."
APK_FILE=$(ls -t app/build/outputs/apk/release/*.apk | head -n 1)
FILE_NAME=$(basename "$APK_FILE")

echo ">>> 找到 APK: $FILE_NAME (versionCode: $VERSION_CODE)"
scp "$APK_FILE" "${SERVER}:${REMOTE_DIR}/"

REMOTE_MARK="${REMOTE_DIR}/latest_file_name"
if [[ ! "${FILE_NAME:-}" =~ ^[a-zA-Z0-9][a-zA-Z0-9._+-]*\.apk$ ]]; then
    echo ">>> 错误：FILE_NAME 未设置或不是有效 APK 文件名，停止发布以免清空更新信息。" >&2
    exit 1
fi
ssh "$SERVER" "printf '%s' '$FILE_NAME' > '$REMOTE_MARK'"

REMOTE_VERSION="${REMOTE_DIR}/latest_version_code"
ssh "$SERVER" "printf '%s' '$VERSION_CODE' > '$REMOTE_VERSION'"

REMOTE_VERSION_NAME="${REMOTE_DIR}/latest_version_name"
ssh "$SERVER" "printf '%s' '$VERSION_NAME' > '$REMOTE_VERSION_NAME'"

echo ">>> 部署完成。"
exit
