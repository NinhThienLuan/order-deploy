#!/bin/sh
# Chạy script này sau khi clone/pull về để setup git hooks
# Usage: sh setup-hooks.sh

cp hooks/commit-msg .git/hooks/commit-msg
chmod +x .git/hooks/commit-msg
echo "✅ Git hooks đã được setup thành công!"

