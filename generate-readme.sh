#!/usr/bin/env bash
set -e
mustache vars.json README.tpl.md > README.md
