#!/usr/bin/env bash
# Creates the Play upload key and keystore.properties on YOUR computer.
# Run once from the project root:  bash tools/release/create-upload-key.sh
# Keep the .jks file and passwords safe (password manager + an offline backup).
# With Play App Signing, a lost upload key can be reset through Play Console support.
set -euo pipefail
KEYSTORE="${1:-vocabsolution-upload.jks}"
ALIAS="upload"
if [ -e "$KEYSTORE" ]; then echo "$KEYSTORE already exists; not overwriting." >&2; exit 1; fi
read -r -s -p "Keystore password (min 6 chars): " PASS; echo
read -r -s -p "Repeat password: " PASS2; echo
[ "$PASS" = "$PASS2" ] || { echo "Passwords differ." >&2; exit 1; }
keytool -genkeypair -v -keystore "$KEYSTORE" -alias "$ALIAS" -keyalg RSA -keysize 4096 -validity 10000 \
  -storepass "$PASS" -keypass "$PASS" \
  -dname "CN=Shahjalal Sultanate, OU=English Doctor, O=English Doctor, C=BD"
cat > keystore.properties <<PROPS
storeFile=$KEYSTORE
storePassword=$PASS
keyAlias=$ALIAS
keyPassword=$PASS
PROPS
chmod 600 keystore.properties "$KEYSTORE"
echo
echo "Done. Created $KEYSTORE and keystore.properties (both are git-ignored)."
echo "Upload certificate SHA-256 (Play Console shows this too):"
keytool -list -v -keystore "$KEYSTORE" -alias "$ALIAS" -storepass "$PASS" | grep 'SHA256:'
echo
echo "Build the bundle:  ./gradlew :app:bundleRelease"
echo "For GitHub Actions secrets, base64 the keystore:  base64 -w0 $KEYSTORE"
