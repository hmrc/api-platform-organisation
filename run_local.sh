#!/bin/bash

sbt "~run -Drun.mode=Dev -Dhttp.port=15504 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes -Dcompanies-house.api-key=${COMPANIES_HOUSE_KEY} $*"
