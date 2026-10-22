#!/usr/bin/env bash
# End-to-end smoke test against the API gateway. Exits non-zero on any failure.
# Requires: curl (jq not required). Usage: bash scripts/smoke-test.sh
set -uo pipefail

GATEWAY="${GATEWAY:-http://localhost:8080}"
MAILPIT="${MAILPIT:-http://localhost:8025}"
TS=$(date +%s)
PASS=0
FAIL=0

# Minimal JSON field extractors (no jq dependency).
jstr() { echo "$1" | grep -o "\"$2\":\"[^\"]*\"" | head -1 | sed "s/.*:\"//;s/\"$//"; }
jnum() { echo "$1" | grep -o "\"$2\":[0-9][0-9]*" | head -1 | grep -o '[0-9][0-9]*'; }

say()  { printf '\n\033[1;34m== %s ==\033[0m\n' "$1"; }
ok()   { printf '  \033[0;32mPASS\033[0m %s\n' "$1"; PASS=$((PASS+1)); }
bad()  { printf '  \033[0;31mFAIL\033[0m %s\n' "$1"; FAIL=$((FAIL+1)); }
check(){ if [ "$1" = "$2" ]; then ok "$3 ($1)"; else bad "$3 (expected $2, got $1)"; fi; }

# api METHOD PATH [TOKEN] [BODY] -> sets $BODY and $CODE
api() {
  local method="$1" path="$2" token="${3:-}" body="${4:-}"
  local args=(-s -o /tmp/sm_body -w '%{http_code}' -m 30 -X "$method" "$GATEWAY$path" -H 'Content-Type: application/json')
  [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
  [ -n "$body" ]  && args+=(-d "$body")
  CODE=$(curl "${args[@]}")
  BODY=$(cat /tmp/sm_body)
}

say "Auth: register + login"
api POST /api/auth/register "" "{\"email\":\"emp$TS@hs.local\",\"password\":\"Password@1\",\"fullName\":\"Smoke Employer\",\"role\":\"EMPLOYER\"}"
check "$CODE" 201 "register employer"
api POST /api/auth/register "" "{\"email\":\"seeker$TS@hs.local\",\"password\":\"Password@1\",\"fullName\":\"Smoke Seeker\",\"role\":\"JOB_SEEKER\"}"
check "$CODE" 201 "register seeker"

api POST /api/auth/login "" "{\"email\":\"emp$TS@hs.local\",\"password\":\"Password@1\"}"
EMP=$(jstr "$BODY" accessToken)
check "$CODE" 200 "login employer"
api POST /api/auth/login "" "{\"email\":\"seeker$TS@hs.local\",\"password\":\"Password@1\"}"
SEEKER=$(jstr "$BODY" accessToken)
check "$CODE" 200 "login seeker"
api POST /api/auth/login "" "{\"email\":\"admin@hiresphere.local\",\"password\":\"Admin@12345\"}"
ADMIN=$(jstr "$BODY" accessToken)
check "$CODE" 200 "login admin (seeded)"

say "Company + AI job description"
api POST /api/companies "$EMP" "{\"name\":\"Smoke Co $TS\",\"industry\":\"Software\"}"
check "$CODE" 201 "create company"
api POST /api/ai/jobs/description "$EMP" "{\"title\":\"Backend Engineer\",\"skills\":[\"Java\",\"Spring Boot\"]}"
check "$CODE" 200 "AI job description"

say "Create + search jobs"
api POST /api/jobs "$EMP" "{\"title\":\"Backend Engineer $TS\",\"description\":\"Build APIs with Java and Spring.\",\"jobType\":\"FULL_TIME\",\"workMode\":\"REMOTE\",\"experienceLevel\":\"MID\",\"skills\":[\"Java\",\"Spring Boot\"],\"publish\":true}"
JOB_ID=$(jnum "$BODY" id)
check "$CODE" 201 "create + publish job"
api GET "/api/jobs?q=Backend" ""
check "$CODE" 200 "search jobs"
api POST /api/ai/search "$SEEKER" "{\"query\":\"remote java backend roles\"}"
check "$CODE" 200 "AI natural-language search"

say "Resume + AI cover letter"
api POST /api/resumes "$SEEKER" "{\"title\":\"My CV\"}"
RESUME_ID=$(jnum "$BODY" id)
check "$CODE" 201 "create resume"
api PUT "/api/resumes/$RESUME_ID/personal-info" "$SEEKER" "{\"fullName\":\"Smoke Seeker\",\"email\":\"seeker$TS@hs.local\",\"phone\":\"123\"}"
check "$CODE" 200 "resume personal info"
api POST "/api/resumes/$RESUME_ID/skills" "$SEEKER" "{\"name\":\"Java\",\"level\":\"ADVANCED\"}"
check "$CODE" 200 "resume add skill"
api POST /api/ai/applications/cover-letter "$SEEKER" "{\"resumeId\":$RESUME_ID,\"jobId\":$JOB_ID}"
check "$CODE" 200 "AI cover letter"

say "Apply + duplicate guard"
api POST /api/applications "$SEEKER" "{\"jobId\":$JOB_ID,\"resumeId\":$RESUME_ID,\"coverLetter\":\"Hello\"}"
APP_ID=$(jnum "$BODY" id)
check "$CODE" 201 "apply to job"
api POST /api/applications "$SEEKER" "{\"jobId\":$JOB_ID,\"resumeId\":$RESUME_ID}"
check "$CODE" 409 "duplicate application rejected"

say "Employer review + AI screening + status"
api GET "/api/applications/job/$JOB_ID" "$EMP"
check "$CODE" 200 "employer lists applications"
api POST "/api/ai/applications/$APP_ID/screen" "$EMP"
check "$CODE" 200 "AI candidate screening"
api PATCH "/api/applications/$APP_ID/status" "$EMP" "{\"status\":\"UNDER_REVIEW\",\"note\":\"Reviewing\"}"
check "$CODE" 200 "status change -> UNDER_REVIEW"

say "Saved jobs + notifications"
api POST "/api/saved-jobs/$JOB_ID" "$SEEKER"
check "$CODE" 200 "save job"
sleep 4
api GET /api/notifications/unread-count "$SEEKER"
UNREAD=$(jnum "$BODY" count)
check "$CODE" 200 "notifications unread-count"
[ "${UNREAD:-0}" -ge 1 ] && ok "seeker has notifications ($UNREAD)" || bad "expected >=1 notification, got ${UNREAD:-0}"

say "Payments + analytics"
api GET /api/plans ""
check "$CODE" 200 "list plans"
api GET /api/analytics/employer/dashboard "$EMP"
check "$CODE" 200 "employer dashboard"
api GET /api/analytics/admin/dashboard "$ADMIN"
check "$CODE" 200 "admin dashboard"

say "Email delivery (Mailpit)"
MSGS=$(jnum "$(curl -s -m 10 "$MAILPIT/api/v1/messages")" total)
[ "${MSGS:-0}" -ge 1 ] && ok "emails delivered to Mailpit ($MSGS)" || bad "no emails in Mailpit"

printf '\n\033[1m== Summary: %d passed, %d failed ==\033[0m\n' "$PASS" "$FAIL"
[ "$FAIL" -eq 0 ]
