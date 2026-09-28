#!/bin/bash
SERVER="admin@music.stellortus.top"
REMOTE_DIR="/srv/Stellar-Music-Server/apks"

echo ">>> 正在打包..."
./gradlew assembleRelease

echo ">>> 正在上传 APK..."
APK_FILE=$(ls -t app/build/outputs/apk/release/*.apk | head -n 1).
FILE_NAME=$(basename "$APK_FILE")

echo ">>> 找到 APK: $FILE_NAME"
scp "$APK_FILE" "${SERVER}:${REMOTE_DIR}/"

REMOTE_MARK="${REMOTE_DIR}/latest_file_name"
ssh $SERVER "touch $REMOTE_MARK && echo $FILE_NAME > $REMOTE_MARK"

echo ">>> 部署完成。"
exit