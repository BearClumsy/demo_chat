#!/usr/bin/env bash
#
# End-to-end smoke test for the RAG chat path: create a chat, send one message, and confirm the
# assistant actually replied.
#
# Written to verify the fix for the offline-profile Ollama OOM
# ("ggml_aligned_malloc: insufficient memory" at QueryNormalizationService, stage 1 of the
# pipeline): once the Colima VM is large enough (`make colima-offline`), llama3.1 loads and the
# first turn succeeds instead of throwing a TransientAiException.
#
# Assumes the backend is already running (`make up-offline && make run-offline`) and the local
# Flyway seed has been applied (`make migrate` creates the schema and the `testuser` login).
#
# Overridable via the environment:
#   BASE_URL   backend base URL           (default http://localhost:8080)
#   LOGIN      HTTP Basic user            (default testuser)
#   PASSWORD   HTTP Basic password        (default password)
#   USER_ID    that user's UUID           (default 00000000-0000-0000-0000-000000000001)
#   MESSAGE    the message to send        (default "How do I reset my password?")
#
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
LOGIN="${LOGIN:-testuser}"
PASSWORD="${PASSWORD:-password}"
USER_ID="${USER_ID:-00000000-0000-0000-0000-000000000001}"
MESSAGE="${MESSAGE:-How do I reset my password?}"

fail() { echo "verify-chat: $*" >&2; exit 1; }

command -v curl >/dev/null 2>&1 || fail "curl is required"
command -v python3 >/dev/null 2>&1 || fail "python3 is required (JSON parsing)"

auth=(-u "${LOGIN}:${PASSWORD}")
json=(-H 'Content-Type: application/json')

# 1. Backend reachable and healthy?
health="$(curl -fsS "${BASE_URL}/actuator/health" 2>/dev/null || true)"
case "${health}" in
  *'"status":"UP"'*) : ;;
  '') fail "backend not reachable at ${BASE_URL} — start it with 'make up-offline && make run-offline'" ;;
  *)  fail "backend not healthy: ${health}" ;;
esac
echo "verify-chat: backend UP at ${BASE_URL}"

msg_json="$(printf '%s' "${MESSAGE}" | python3 -c 'import json,sys; print(json.dumps(sys.stdin.read()))')"

# 2. Create a chat (assistant only: empty participantIds).
now="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
create_body="$(printf '{"currentUserId":"%s","participantIds":[],"title":"verify-chat smoke","message":{"userId":"%s","message":%s,"datetime":"%s"}}' \
  "${USER_ID}" "${USER_ID}" "${msg_json}" "${now}")"

resp="$(curl -sS -w $'\n%{http_code}' "${auth[@]}" "${json[@]}" \
  -X POST "${BASE_URL}/api/chats" -d "${create_body}")" \
  || fail "request to ${BASE_URL}/api/chats failed"
code="${resp##*$'\n'}"; body="${resp%$'\n'*}"
[ "${code}" = "200" ] || fail "POST /api/chats -> HTTP ${code}: ${body}"
chat_id="$(printf '%s' "${body}" | python3 -c 'import json,sys; print(json.load(sys.stdin))')"
echo "verify-chat: created chat ${chat_id}"

# 3. Send a message through the RAG pipeline.
resp="$(curl -sS -w $'\n%{http_code}' "${auth[@]}" "${json[@]}" \
  -X POST "${BASE_URL}/api/chats/${chat_id}/messages" -d "{\"message\":${msg_json}}")" \
  || fail "request to ${BASE_URL}/api/chats/${chat_id}/messages failed"
code="${resp##*$'\n'}"; body="${resp%$'\n'*}"
[ "${code}" = "200" ] || fail "POST /api/chats/${chat_id}/messages -> HTTP ${code}: ${body}"

# 4. The reply must be non-empty (the OOM path returns an error, not a reply).
printf '%s' "${body}" | python3 -c '
import json, sys
data = json.load(sys.stdin)
reply = (data.get("reply") or "").strip()
status = data.get("status") or "?"
if not reply:
    sys.exit(f"verify-chat: empty reply (status={status}) — body was {json.dumps(data)}")
print(f"verify-chat: OK - status={status}")
print(f"verify-chat: reply: {reply[:500]}")
'
