#!/usr/bin/env bash
set -euo pipefail
git config user.name common-develop-bot
git config user.email common-develop-bot@users.noreply.github.com
exec bash .common-develop-r05-plan.sh
