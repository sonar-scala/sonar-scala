#!/usr/bin/env bash
set -eu

export SONARQUBE_URL=http://localhost
export SONARQUBE_ACCESS_TOKEN=

exec ./scan.sh
