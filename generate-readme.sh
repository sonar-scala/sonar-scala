#!/usr/bin/env bash
set -e
mustache vars.js README.tpl.md > README.md
