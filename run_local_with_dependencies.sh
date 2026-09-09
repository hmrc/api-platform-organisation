#!/bin/bash

sm2 --start MONGO

sm2 --start INDIVIDUALS_MATCHING_API INTERNAL_AUTH --appendArgs '{"INTERNAL_AUTH": ["-Dapplication.router=testOnlyDoNotUseInAppConf.Routes"]}'

echo "Waiting for internal-auth on localhost:8470..."
until curl -s -o /dev/null "http://localhost:8470/ping/ping"; do sleep 2; done

# Grant this service's internal-auth token (internal-auth.token = 15504) READ on the
# individuals-matching-api resource type, so the Matching API accepts it over internal-auth.
curl -X POST "http://localhost:8470/test-only/token" \
  -H "content-type: application/json" \
  -d "{
    \"token\": \"15504\",
    \"principal\": \"api-platform-organisation-local-test\",
    \"permissions\": [{
       \"resourceType\": \"individuals-matching-api\",
       \"resourceLocation\": \"*\",
       \"actions\": [ \"READ\" ]
    }]
  }"

# Point the connector at the Matching API /sandbox route: canned data (no citizen-details /
# matching / NPS needed) while still exercising the internal-auth handshake end to end.
./run_local.sh -Dmicroservice.services.individuals-matching-api.path=/sandbox
